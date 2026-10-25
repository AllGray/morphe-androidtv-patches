# Morphe Patches — Pluto TV (AllGray)

A small fork of [ajstrick81/morphe-androidtv-patches](https://github.com/ajstrick81/morphe-androidtv-patches)
that keeps only the **Pluto TV** patches and adds support for **Pluto TV 17**. All the reverse
engineering of Pluto 5.66 is ajstrick81's work (GPLv3, see LICENSE and NOTICE).

| Patch | Pluto TV version |
|---|---|
| Skip ads, Mask live ad breaks, Override certificate pinning, Disable auto-updates | `5.66.0-leanback` |
| **Skip ads (Pluto TV 17)** — on-demand ads removed (tested on a Sony BRAVIA, Android 9) | `17.2.0` (Android TV and phone/tablet builds) |
| Clone Pluto TV (install beside the original) | `5.66.0-leanback`, `17.2.0` |

Pluto TV 17 is a rebuild on Paramount+'s app platform (AVIA player), so the 5.66 hooks don't exist
there. Its VOD is still Pluto's stitched multi-period DASH, so the 17 patch drops the ad periods from the
manifest text before media3's (renamed) parser reads it, and empties the ad-break timeline (AdsData).
Tested: 97 ad periods removed from a 44-minute episode, playback and resume fine. A few cosmetic
markers remain where content periods meet; live channels are not covered. See
`patches/src/main/kotlin/ajstrick81/morphe/patches/pluto17/`.

## Use

In Morphe Manager: **Sources → add** `https://github.com/AllGray/morphe-androidtv-patches` (or import
the `.mpp` from Releases), then pick the Pluto TV `.apkm` from APKMirror (Android TV listing,
`armeabi-v7a`). Keep **Optimize for device architecture** off.

Releases are built by GitHub Actions on every push to `main`.
