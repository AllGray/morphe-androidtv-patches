# ProGuard rules for the Pluto TV extension. Its entry points are called only from injected smali.

# Pluto DASH period strip — skipAdsPatch Hook 5 (see SkipAdsPatch.kt).
# PlutoDashManifestProbe.stripAdPeriods(DashManifest) is called only from injected
# smali (DashManifestParser.parse return), so R8 sees it as unreferenced and would
# strip or rename it. The media3 classes it references are provided by the app at
# runtime (compileOnly).
-keep class ajstrick81.morphe.extension.pluto.ads.PlutoDashManifestProbe {
    public static androidx.media3.exoplayer.dash.manifest.DashManifest stripAdPeriods(androidx.media3.exoplayer.dash.manifest.DashManifest);
    public static long mapResumePosition(androidx.media3.common.Player, long);
}
-dontwarn androidx.media3.exoplayer.dash.manifest.**

# Pluto LIVE ad-break mask — liveSlatePatch (see LiveSlatePatch.kt). Its @JvmStatic
# entry points are called only from injected smali, so R8 would strip/rename them.
-keep class ajstrick81.morphe.extension.pluto.ads.PlutoLiveSlateHelper {
    public static void onAdTick(java.lang.Object);
    public static void registerActivity(android.app.Activity);
    public static void unregisterActivity(android.app.Activity);
    public static void setAviaPlayer(java.lang.Object);
}

# Pluto TV 17 — DASH ad-period filter. PlutoMpdFilter.filter(InputStream) is called only from injected
# smali at the start of media3's (R8-renamed) DashManifestParser.parse, so R8 would strip or rename it.
-keep class ajstrick81.morphe.extension.pluto17.PlutoMpdFilter {
    public static java.io.InputStream filter(java.io.InputStream);
}
