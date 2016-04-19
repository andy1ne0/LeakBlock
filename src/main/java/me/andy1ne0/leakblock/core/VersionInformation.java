package me.andy1ne0.leakblock.core;

/**
 * Class for requesting the latest version of LeakBlock
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
public final class VersionInformation {

    private VersionInformation() {
        throw new UnsupportedOperationException();
    }

    public static String getLatestVersion() {
        String latestVersion = HttpUtil.doGetRequest("https://raw.githubusercontent.com/andy1ne0/LeakBlock/master/src/main/resources/latestversion.txt");
        return latestVersion != null ? latestVersion.trim() : null;
    }
}
