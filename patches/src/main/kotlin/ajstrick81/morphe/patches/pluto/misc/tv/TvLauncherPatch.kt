package ajstrick81.morphe.patches.pluto.misc.tv

import app.morphe.patcher.patch.resourcePatch
import ajstrick81.morphe.patches.pluto.shared.Constants
import org.w3c.dom.Element

private const val LEANBACK_LAUNCHER = "android.intent.category.LEANBACK_LAUNCHER"

// ─────────────────────────────────────────────────────────────────────────────
// Show the phone/tablet build on Android TV
//
// Pluto TV 17.x ships a phone/tablet build (versionCode 420000962) next to the TV build
// (520000962). Some people prefer its layout. Android TV launchers only list apps with a
// LEANBACK_LAUNCHER activity, and the phone build has none, so once installed it never
// shows up on the home screen. This patch adds that category to the existing launcher
// activity and marks touchscreen/leanback as optional features, so a TV or streamer
// without a touchscreen accepts it. Manifest-only. Opt-in: the TV build already has it.
// ─────────────────────────────────────────────────────────────────────────────
@Suppress("unused")
val tvLauncherPatch = resourcePatch(
    name = "Show phone build on Android TV",
    description = "Adds the Android TV launcher entry to the phone/tablet build of Pluto TV 17.x, " +
        "so it appears on the TV home screen and installs on a device without a touchscreen. " +
        "Use it only with the phone build; the TV build already has the entry. " +
        "Remote navigation in the phone layout depends on the app. Opt-in.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY_17)

    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement

            fun optionalFeature(name: String) {
                val features = document.getElementsByTagName("uses-feature")
                for (i in 0 until features.length) {
                    val feature = features.item(i) as? Element ?: continue
                    if (feature.getAttribute("android:name") == name) {
                        feature.setAttribute("android:required", "false")
                        return
                    }
                }
                manifest.appendChild(document.createElement("uses-feature").apply {
                    setAttribute("android:name", name)
                    setAttribute("android:required", "false")
                })
            }
            optionalFeature("android.hardware.touchscreen")
            optionalFeature("android.software.leanback")

            // Add LEANBACK_LAUNCHER to every activity whose filter has MAIN + LAUNCHER.
            val filters = document.getElementsByTagName("intent-filter")
            var added = 0
            for (i in 0 until filters.length) {
                val filter = filters.item(i) as? Element ?: continue
                val categories = filter.getElementsByTagName("category")
                val names = (0 until categories.length)
                    .map { (categories.item(it) as Element).getAttribute("android:name") }
                val actions = filter.getElementsByTagName("action")
                val isMain = (0 until actions.length)
                    .any { (actions.item(it) as Element).getAttribute("android:name") == "android.intent.action.MAIN" }
                if (isMain && "android.intent.category.LAUNCHER" in names && LEANBACK_LAUNCHER !in names) {
                    filter.appendChild(document.createElement("category").apply {
                        setAttribute("android:name", LEANBACK_LAUNCHER)
                    })
                    added++
                }
            }
            check(added > 0 || document.getElementsByTagName("category").let { c ->
                (0 until c.length).any { (c.item(it) as Element).getAttribute("android:name") == LEANBACK_LAUNCHER }
            }) { "Pluto TV: no launcher activity found in the manifest" }
        }
    }
}
