package ajstrick81.morphe.patches.pluto17

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// AviaDAIResourceProvider.shouldPlayAd(AviaAdPod): Z — same anchor as the Paramount+ patch (readable
// method name + unique leaf class name); in Pluto 17.2.0 it is private, boolean, and present in both builds.
// Kept as a harmless extra: on-device, Pluto 17 VOD does not play through the DAI provider.
internal object ShouldPlayAd17Fingerprint : Fingerprint(
    returnType = "Z",
    custom = { method, _ ->
        method.name == "shouldPlayAd" &&
            method.definingClass.endsWith("/AviaDAIResourceProvider;")
    },
)

// media3's DashManifestParser.parse(Uri, InputStream): DashManifest. R8-renamed in Pluto 17 (17.2.0 TV:
// Ldz3;->c(Landroid/net/Uri;Ljava/io/InputStream;)Lbz3;), so it is found by media3's own error literal,
// which only this method throws, plus its (Uri, InputStream) parameters.
internal object DashManifestParse17Fingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/net/Uri;", "Ljava/io/InputStream;"),
    strings = listOf("inputStream does not contain a valid media presentation description"),
)

// Pluto's ad-break timeline, AdsData(adBreaks: List, ...): a Kotlin data class (17.2.0 TV: Lgm;) whose
// toString() spells its field names. Found by that literal; its constructor is then edited (see the patch).
internal object AdsDataToString17Fingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    strings = listOf("AdsData(adBreaks="),
    custom = { method, _ -> method.name == "toString" },
)
