package rikka.shizuku.manager;

/**
 * The languages this app is translated into, and how each of them is written.
 *
 * <p>This used to be generated at build time by the auto-res-config plugin, which read the
 * translation folders and wrote both arrays. The plugin only works in an application module,
 * and the manager is a library now, so the lists are written down here instead: the first
 * entry of each is the "follow the system" choice, and the two lists differ in how Chinese
 * is written - {@code zh-CN} and {@code zh-TW} in one, {@code zh-Hans} and {@code zh-Hant} in
 * the other, because a script is what the picker shows and a region is what the settings
 * store.
 */
public final class ShizukuLocales {
    public static final String[] LOCALES = {"SYSTEM", "af", "ar", "az", "bn", "cs", "de", "el", "en", "es", "fa", "fi", "fil", "fr", "hi", "hu", "hy", "id", "it", "ja", "ka", "ko", "mr", "ms", "nl", "no", "pa", "pl", "pt-BR", "ro", "ru", "ru-BY", "sl", "sr", "sv", "ta", "te", "th", "tr", "tr-CY", "uk", "ur", "vi", "zh-CN", "zh-TW"};
    public static final String[] DISPLAY_LOCALES = {"SYSTEM", "af", "ar", "az", "bn", "cs", "de", "el", "en", "es", "fa", "fi", "fil", "fr", "hi", "hu", "hy", "id", "it", "ja", "ka", "ko", "mr", "ms", "nl", "no", "pa", "pl", "pt-BR", "ro", "ru", "ru-BY", "sl", "sr", "sv", "ta", "te", "th", "tr", "tr-CY", "uk", "ur", "vi", "zh-Hans", "zh-Hant"};

    private ShizukuLocales() {
    }
}
