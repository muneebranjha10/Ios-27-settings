# iSettings 27
Experimental native Android LSPosed theme for Samsung Settings. Built from the supplied iOS-style reference and the layout tokens in ionic-theme-ios27 ios26-v9.5.0 (see THIRD_PARTY_LICENSE.txt). Not an Apple product.

## Install privately for testing
Open Actions, select Build experimental APK, and download the artifact after a successful run. Extract and install app-debug.apk. In LSPosed, enable the module and scope only Samsung Settings (com.android.settings). Force-stop Settings and open it. If anything fails, disable the module and force-stop Settings again. No system APK replacement, no SystemUI hooks, no boot scripts. Debug signing may change between CI runs.

## Beta scope
Homepage only. Native preference actions remain in Samsung Settings; connectivity rows launch native screens. Airplane Mode attempts the existing system service operation and checks real state; if unavailable, it opens native controls. The account remains Samsung Account, not Apple Account. Search opens native Settings search if found. Original Settings fallback at the bottom. Theme only mounts on portrait phone homepage when the native preferences are ready. Native submenu designs remain unchanged.
53dp rows, 28dp card corners, 20dp margins, 32dp group gaps, 17sp labels, source-derived icon gradients, 44dp translucent search pill. Native glyphs and fonts are approximations; translucent glass is not optical refraction or backdrop blur. No pixel-perfect or 120Hz claim. This is not tested on a device. 
Before releasing: test light/dark, every category, airplane state, native search, back, screen zoom, rotation, account and Settings restart. Check LSPosed logs for iSettings27.

## 0.3 changes
Uses Ionicons 8.1.0 glyphs from the exact demo dependency version, not Apple SF Symbols. The CI generates Android bitmap masks at build time from npm SVGs and bundles their license. For a local build, run npm pack ionicons@8.1.0, extract the archive, install CairoSVG and run python scripts/generate_icons.py path/to/package before Gradle. Unmapped categories have differentiated icon types, and the scroll container is inset from system bars. Visual fixes require on-device verification. The text and glass material remain approximations.

## 0.4
White glyphs in both themes; dark charcoal cards; lighter typography on themed text; compact rows. Animated airplane switch follows actual observed state. Spring-like touch feedback on homepage controls. Search uses a sampled scroll-content backdrop at up to 30 updates/sec, AGSL blur/refraction at edges on API 33+ hardware canvas, and an opaque fallback if unsupported. No exact Apple optical or frame-rate claim. No submenu transition replacement. Device testing required.

## 0.5
Ported capsule refraction math from Kyant AndroidLiquidGlass 2.0.1 (Apache-2.0), adapted to native RenderNode and RuntimeShader. Live backdrop sampled from actual scroll content with padding, blur and optical distortion; touch highlight and spring release, animated confirmed-state switch. Custom screenshot-derived glyphs replace multiple mismatches. Local SF Pro support: reads named fonts from Settings private files/isettings-fonts or system/product fonts if already readable; no proprietary font files shipped. Defaults to Android sans-serif otherwise. No exact iOS icon/font claim. Hardware runtime and navigation tests still needed.

## 0.6
Reference-derived alpha masks for 17 icons, with sampled gradient palette. Masks from supplied screenshot references; not original scalable Apple icon files. Confirm version at bottom of homepage (v0.6). Reusable native renderer based on 2.0.1 Kyant lens and seven-sample dispersion, with original attribution/license retained. Search and collapsing header use live backdrop; toggle press/drag uses lens applied to track, with actual state confirmation. Settings cards remain opaque as in reference. No complete Compose library integration; native submenus are unchanged. The shader and performance require physical device verification; check LSPosed logs for 'v0.6 GPU glass active' or fallback. Font files are not bundled. Before public redistribution verify rights to screenshot-derived icon assets.
