/*
 * Pluto TV 17.x — Ad Patch
 *
 * Pluto TV 17 is a rebuild on Paramount+'s app platform (com.paramount.android.*, com.cbs.app.*), and
 * media3 is R8-renamed, so the 5.66.0-leanback hooks in patches/pluto don't exist in it. On-device
 * (Sony BRAVIA, Android 9, 17.2.0 TV build) its VOD still comes from Pluto's own stitcher as ONE
 * multi-period DASH manifest — the same shape 5.66 used: DRM'd content periods alternating with ad
 * periods whose BaseURL holds the creative path (_ad%2Fcreative%2F…) and pod bumpers (_ad_bumper).
 * The AVIA DAI ad gate the first version relied on is not used for this stream.
 *
 * Two layers, as in the 5.66 patch:
 *   1. The ad VIDEO: the manifest text goes through PlutoMpdFilter before media3's (renamed)
 *      DashManifestParser reads it; ad periods are dropped and the rest re-based contiguous.
 *      Fail-open: a manifest it can't rewrite is passed through unchanged.
 *   2. The ad TIMELINE: AdsData(adBreaks=…) — what draws the seekbar markers, fires the ad beacons
 *      and blocks seeking during breaks — is created with an empty list. With the ad periods gone its
 *      offsets would point into the wrong places anyway.
 *
 * Not covered yet: pause ads, live channels, and re-mapping a resume bookmark saved in the old
 * (ad-inclusive) timeline (5.66 issue #147).
 */

package ajstrick81.morphe.patches.pluto17

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import ajstrick81.morphe.patches.pluto.shared.Constants

private const val FILTER = "Lajstrick81/morphe/extension/pluto17/PlutoMpdFilter;"

@Suppress("unused")
val pluto17SkipAdsPatch = bytecodePatch(
    name = "Skip ads (Pluto TV 17)",
    description = "Removes on-demand ads in Pluto TV 17.x (the Paramount-based app): drops the ad " +
        "periods from the stitched DASH stream so the ad video never plays, and empties the ad-break " +
        "timeline (markers, beacons, seek blocking). Fail-open: a stream it can't rewrite plays as before. " +
        "Live TV ads are not covered. Experimental.",
) {
    compatibleWith(Constants.COMPATIBILITY_17)

    extendWith("extensions/extension.mpe")

    execute {
        // 1. DashManifestParser.parse(Uri p1, InputStream p2): filter the stream before anything reads it.
        DashManifestParse17Fingerprint.method.addInstructions(
            0,
            """
                invoke-static {p2}, $FILTER->filter(Ljava/io/InputStream;)Ljava/io/InputStream;
                move-result-object p2
            """.trimIndent(),
        )

        // 2. AdsData.<init>(List adBreaks p1, String, long, long): always an empty list. The constructor has
        //    no free locals, so the empty list goes straight into the parameter register p1.
        val adsDataType = AdsDataToString17Fingerprint.classDef.type
        val adsData = mutableClassDefByOrNull { it.type == adsDataType }
            ?: error("Pluto 17: AdsData class $adsDataType not found")
        val init = adsData.methods.first {
            it.name == "<init>" && it.parameterTypes.firstOrNull()?.toString() == "Ljava/util/List;"
        }
        init.addInstructions(
            0,
            """
                invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                move-result-object p1
            """.trimIndent(),
        )

        // Paramount's DAI gate, kept from the first version: harmless if unused.
        ShouldPlayAd17Fingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )
    }
}
