/*
 * Pluto TV 17.x — Ad Patch
 *
 * Pluto TV 17 is not a new version of the 5.66.0-leanback app that patches/pluto targets: it is a
 * rebuild on Paramount+'s app platform (com.paramount.android.*, com.viacom.*, com.cbs.app.*) with
 * the AVIA player, and none of the 5.66 hooks (BeaconTracker, PauseAdsImageBinder, StitcherSession,
 * unobfuscated media3) exist in it. The Paramount+ VOD ad gate does:
 *
 *   AviaDAIResourceProvider.shouldPlayAd(AviaAdPod): Z   (private, readable name)
 *
 * Its position monitor takes AVIA's own skip-the-pod path when this returns false (see
 * patches/paramount/ParamountPatch.kt, Patch 1). Present, unchanged, in both 17.2.0 builds:
 * Android TV (versionCode 520000962) and phone/tablet (420000962).
 *
 * Not ported (yet), from the Paramount+ patch:
 *   - Pause ads: CbsPauseWithAdsOverlay exists in the TV build, but its render method lost the
 *     " not updating overlay." log anchor (methods are R8-renamed G/H/I) — needs an on-device trace.
 *   - Live slate: AviaNetworkInterceptor.intercept returns an R8-renamed okhttp type here (okhttp3 is
 *     obfuscated in Pluto 17), and Pluto's live breaks are its own stitcher, not Google DAI pods.
 *
 * Whether Pluto's stitched (SSAI) VOD streams go through the DAI provider is to be confirmed
 * on-device; if they don't, this patch is a harmless no-op.
 */

package ajstrick81.morphe.patches.pluto17

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import ajstrick81.morphe.patches.pluto.shared.Constants

@Suppress("unused")
val pluto17SkipAdsPatch = bytecodePatch(
    name = "Skip ads (Pluto TV 17)",
    description = "Skips on-demand ad breaks in Pluto TV 17.x (the Paramount-based app), " +
        "Android TV and phone/tablet builds, through the AVIA player's ad gate. Experimental.",
) {
    compatibleWith(Constants.COMPATIBILITY_17)

    execute {
        // shouldPlayAd(AviaAdPod) -> false for every pod: AVIA skips the pod natively.
        ShouldPlayAd17Fingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )
    }
}
