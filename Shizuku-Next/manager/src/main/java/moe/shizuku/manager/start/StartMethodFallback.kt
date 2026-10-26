package moe.shizuku.manager.start

import android.util.Log
import moe.shizuku.manager.AppConstants
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.utils.EnvironmentUtils
import moe.shizuku.manager.utils.RootShell
import rikka.shizuku.Shizuku

/**
 * Checks that the configured start method can actually run here.
 *
 * Root can be gone since the method was chosen an OTA that relocks the bootloader, a
 * ROM that no longer grants it, root switched off in the manager while the stored
 * setting stays as it was. Shizuku would then try to escalate on every start (boot, the
 * watchdog, the Start button), find nothing to escalate with, and quietly do nothing: the
 * app looks configured but never starts.
 *
 * So a stored Root falls back to Wireless debugging while the device has no root, and
 * [droppedRoot] says so, so the settings screen can explain the fallback. The setting itself is
 * left alone: root can come back - root switched back on, a reflash, an OTA reverted - and the
 * next start after that has to use it again without the user having to re-pick Root.
 *
 * Only Root can go missing: wireless, USB and system don't depend on the device being rooted.
 */
object StartMethodGuard {

    /**
     * Whether the last resolution fell back from Root because the device has no root.
     * In memory only the point is to explain the fallback on the screen that shows it.
     */
    @Volatile
    var droppedRoot: Boolean = false
        private set

    /**
     * The method to use: [configured] unless it is Root and this device can't run it, in
     * which case Wireless debugging is used for this start and [configured] is kept as it is.
     *
     * Call off the main thread: probing root spawns a shell.
     */
    @ShizukuSettings.StartMethod
    fun resolve(
        @ShizukuSettings.StartMethod configured: Int = ShizukuSettings.getStartMethod()
    ): Int {
        if (configured != ShizukuSettings.StartMethod.ROOT &&
            configured != ShizukuSettings.StartMethod.CUSTOM_ROOT
        ) {
            droppedRoot = false
            return configured
        }
        if (isAvailable(configured)) {
            // Root is back, or never left: the choice is Root again without anything having to
            // restore it, because it was never overwritten.
            droppedRoot = false
            return configured
        }

        Log.w(
            AppConstants.TAG,
            "Root is not available on this device; using Wireless debugging for this start"
        )
        droppedRoot = true
        return ShizukuSettings.StartMethod.WIRELESS
    }

    /**
     * True when this device can run [method]. Every method but the two root ones always
     * can, since none of the others needs privileges of its own and neither of the root
     * methods requires them to be *offered*: they are only hidden where escalating is
     * impossible. The custom root probes the launcher the user named rather than the
     * plain `su` the default starts with.
     */
    fun isAvailable(@ShizukuSettings.StartMethod method: Int): Boolean =
        when (method) {
            ShizukuSettings.StartMethod.CUSTOM_ROOT ->
                !ShizukuSettings.getRootStartCommand().isBlank() &&
                    runCatching { RootShell.isRootAvailable() }.getOrDefault(false)

            ShizukuSettings.StartMethod.ROOT ->
                runCatching { EnvironmentUtils.isRooted() }.getOrDefault(false)

            ShizukuSettings.StartMethod.SHIZUKU ->
                // This one does depend on the device state: it starts *through* an alive
                // Shizuku, wherever that binder comes from. Nothing alive, nothing to ask.
                runCatching { Shizuku.pingBinder() }.getOrDefault(false)

            else -> true
        }
}
