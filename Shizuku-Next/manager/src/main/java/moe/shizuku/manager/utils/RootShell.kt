package moe.shizuku.manager.utils

import com.topjohnwu.superuser.Shell
import moe.shizuku.manager.ShizukuSettings

/**
 * The root shell a root start runs in.
 *
 * libsu probes for `su` in PATH by default, and the whole root method stands on that
 * probe. A device can be rooted while it still fails there: the binary lives at a path
 * the app's PATH does not carry (KernelSU and APatch keep theirs under /data/adb), or
 * the manager's su is not the one a spawn from this app reaches. The probe then comes
 * back as an ordinary shell - no superuser prompt ever appears, the method reads as
 * unavailable, and the start falls back silently. That symptom is what a custom
 * command answers: the user names the launcher of a root shell, and the start runs
 * through it instead.
 *
 * Everything stays the ordinary root start otherwise; only the shell launcher differs.
 * When no custom command is configured, [obtain] hands out the same cached global shell
 * `Shell.cmd` uses everywhere else, and callers must not close it.
 */
object RootShell {

    /** True while the chosen start method is the custom-command root and a command exists. */
    fun isCustomMethod(): Boolean =
        ShizukuSettings.getStartMethod() == ShizukuSettings.StartMethod.CUSTOM_ROOT &&
            !ShizukuSettings.getRootStartCommand().isBlank()

    /** The configured launcher, or null when the start should use the default probe. */
    private fun customCommand(): String? =
        ShizukuSettings.getRootStartCommand()
            .trim()
            .takeIf { it.isNotEmpty() && ShizukuSettings.getStartMethod() == ShizukuSettings.StartMethod.CUSTOM_ROOT }

    /** The result of the last spawn of the custom launcher, so a probe does not keep doing it. */
    @Volatile
    private var customProbeResult: Boolean? = null

    /** Forget the cached probe answer. Called whenever the command is edited. */
    fun invalidate() {
        customProbeResult = null
    }

    /**
     * A shell a root start can run in, with what closes it: [closeIfDedicated] is a no-op
     * for the cached global shell and closes the one built for a custom command.
     */
    class Handle internal constructor(val shell: Shell, private val dedicated: Boolean) {
        fun closeIfDedicated() {
            if (dedicated) {
                shell.close()
            }
        }
    }

    /**
     * The shell to run the start payload in. Must be called off the main thread: it
     * spawns the custom launcher when one is configured, and starts the global shell
     * when libsu has not opened one yet.
     */
    fun obtain(): Handle {
        val custom = customCommand()
        return if (custom != null) {
            Handle(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setCommands(custom)
                    .build(),
                dedicated = true
            )
        } else {
            Handle(Shell.getShell(), dedicated = false)
        }
    }

    /**
     * Whether this method can escalate here. The default probe goes through the cached
     * global shell, which libsu keeps anyway; a custom launcher is spawned once and the
     * answer remembered, since every availability check would otherwise start another
     * root conversation - and a launcher the manager prompts for would ask again each time.
     */
    fun isRootAvailable(): Boolean {
        if (customCommand() == null) {
            return try {
                Shell.getShell().isRoot
            } catch (e: Exception) {
                false
            }
        }
        customProbeResult?.let { return it }
        val ok = runCatching {
            // Spawned and dropped just for the probe; the start itself opens its own.
            val shell = obtain().shell
            val isRoot = shell.isRoot
            shell.close()
            isRoot
        }.getOrDefault(false)
        customProbeResult = ok
        return ok
    }
}
