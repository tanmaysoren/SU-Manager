// SPDX-License-Identifier: GPL-3.0-or-later

package io.github.muntashirakon.AppManager;

import android.app.Application;

import io.github.muntashirakon.AppManager.permission.PermissionOverrideManager;
import android.content.Context;
import android.os.Build;
import android.sun.security.provider.JavaKeyStoreProvider;

import androidx.annotation.Keep;

import com.topjohnwu.superuser.Shell;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.security.Security;

import dalvik.system.ZipPathValidator;
import io.github.muntashirakon.AppManager.misc.AMExceptionHandler;
import io.github.muntashirakon.AppManager.compat.ProcessCompat;
import io.github.muntashirakon.AppManager.utils.Utils;
import io.github.muntashirakon.AppManager.utils.appearance.AppearanceUtils;

public class AppManager extends Application {
    static {
        Shell.enableVerboseLogging = false; // BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_MOUNT_MASTER)
                .setTimeout(10));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // We don't rely on the system to detect a zip slip attack
            ZipPathValidator.clearCallback();
        }
    }

    @Keep
    @Override
    public void onCreate() {
        super.onCreate();
        if (isIsolatedProcess()) return;
        // The Shizuku manager, which is built into this app rather than installed beside
        // it, is brought up here: it has its own settings, its own watchdog and its own
        // alarms, and none of them can be set up later than this. The second argument
        // says the night mode and the language are the app's to decide - the app has
        // settings for both of its own, and setting them from the manager as well would
        // leave the two fighting over the same global setting.
        moe.shizuku.manager.ShizukuApplication.initApplication(this, false);
        PermissionOverrideManager.reconcileAll();
        Thread.setDefaultUncaughtExceptionHandler(new AMExceptionHandler(this));
        AppearanceUtils.init(this);
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.addProvider(new JavaKeyStoreProvider());
        Security.addProvider(new BouncyCastleProvider());
    }

    @Keep
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (!isIsolatedProcess() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !Utils.isRoboUnitTest()) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }
    }

    private static boolean isIsolatedProcess() {
        int uid = android.os.Process.myUid();
        return uid >= ProcessCompat.FIRST_ISOLATED_UID && uid <= ProcessCompat.LAST_ISOLATED_UID;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_RUNNING_CRITICAL) {
            StaticDataset.cleanup();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        StaticDataset.cleanup();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        StaticDataset.cleanup();
    }
}
