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
