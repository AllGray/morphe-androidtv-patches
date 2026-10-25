package ajstrick81.morphe.patches.pluto17

import app.morphe.patcher.Fingerprint

// AviaDAIResourceProvider.shouldPlayAd(AviaAdPod): Z — same anchor as the Paramount+ patch (readable
// method name + unique leaf class name); in Pluto 17.2.0 it is private, boolean, and present in both builds.
internal object ShouldPlayAd17Fingerprint : Fingerprint(
    returnType = "Z",
    custom = { method, _ ->
        method.name == "shouldPlayAd" &&
            method.definingClass.endsWith("/AviaDAIResourceProvider;")
    },
)
