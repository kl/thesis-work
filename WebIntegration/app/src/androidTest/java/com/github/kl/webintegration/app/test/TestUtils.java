package com.github.kl.webintegration.app.test;

class TestUtils {

    // Retrieved from running getInstrumentation().getTargetContext().getCacheDir().getPath()
    private static final String DEX_CACHE_PATH = "/data/data/com.github.kl.webintegration.app/cache";

    // Workaround for https://code.google.com/p/dexmaker/issues/detail?id=2
    static void setDexCache() {
        System.setProperty("dexmaker.dexcache", DEX_CACHE_PATH);
    }
}


