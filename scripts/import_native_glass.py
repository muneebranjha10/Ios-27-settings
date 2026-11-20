# Import the pinned upstream sources, preserving their license and code.
from pathlib import Path
import shutil, sys
src=Path(sys.argv[1])
out=Path("nativeglass/src/main")
paths=["liquidglass/src/main/java/com/example/blur/NativeGauss.kt","liquidglass/src/main/java/com/example/liquidglass/AdvancedFastBlur.kt","liquidglass/src/main/java/com/example/liquidglass/AsyncRenderer.kt","liquidglass/src/main/java/com/example/liquidglass/BackdropCapture.kt","liquidglass/src/main/java/com/example/liquidglass/BackdropLuminanceMeter.kt","liquidglass/src/main/java/com/example/liquidglass/BitmapPool.kt","liquidglass/src/main/java/com/example/liquidglass/BlurMethod.kt","liquidglass/src/main/java/com/example/liquidglass/ChromaticAberrationEffect.kt","liquidglass/src/main/java/com/example/liquidglass/ChromaticDispersionEffect.kt","liquidglass/src/main/java/com/example/liquidglass/DispersionMapGenerator.kt","liquidglass/src/main/java/com/example/liquidglass/DisplacementMapGenerator.kt","liquidglass/src/main/java/com/example/liquidglass/EdgeHighlightEffect.kt","liquidglass/src/main/java/com/example/liquidglass/EnhancedBlurEffect.kt","liquidglass/src/main/java/com/example/liquidglass/GlassAccessibility.kt","liquidglass/src/main/java/com/example/liquidglass/GlassLensRenderer.kt","liquidglass/src/main/java/com/example/liquidglass/GlassMaterial.kt","liquidglass/src/main/java/com/example/liquidglass/GlassRuntimeEffects.kt","liquidglass/src/main/java/com/example/liquidglass/HardwareBackdropBlur.kt","liquidglass/src/main/java/com/example/liquidglass/LightSourceController.kt","liquidglass/src/main/java/com/example/liquidglass/LiquidDampedSpringPhysics.kt","liquidglass/src/main/java/com/example/liquidglass/LiquidGlassThumbRenderer.kt","liquidglass/src/main/java/com/example/liquidglass/LiquidGlassToggle.kt","liquidglass/src/main/java/com/example/liquidglass/LiquidGlassView.kt","liquidglass/src/main/java/com/example/liquidglass/NativeChromaticAberration.kt","liquidglass/src/main/java/com/example/liquidglass/NativeChromaticDispersion.kt","liquidglass/src/main/java/com/example/liquidglass/ScrollEdgeBlurView.kt"]
for p in paths:
    target=Path("nativeglass")/p.removeprefix("liquidglass/")
    target.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(src/p,target)
attrs=out/"res/values/attrs.xml"
attrs.parent.mkdir(parents=True,exist_ok=True)
shutil.copyfile(src/"liquidglass/src/main/res/values/attrs.xml",attrs)
# Adapt toggle to keep glass visible while the confirmed switch state is moving.
p=out/"java/com/example/liquidglass/LiquidGlassToggle.kt"
s=p.read_text()
def replace(a,b):
    global s
    if a not in s: raise RuntimeError("upstream source changed: "+a)
    s=s.replace(a,b,1)
replace("thumbView.pressProgress = physics.pressProgress", "thumbView.pressProgress = maxOf(physics.pressProgress, if (checked) 0.34f else 0.10f, 4f * fraction * (1f - fraction))")
replace("isClickable = true", "clipChildren = false\n        clipToPadding = false\n        isClickable = true")
replace("private fun requestFrame() {", "override fun onDetachedFromWindow() {\n        removeCallbacks(frameCallback)\n        isAnimating = false\n        super.onDetachedFromWindow()\n    }\n\n    private fun requestFrame() {\n        removeCallbacks(frameCallback)")
replace("maxRefractionAmount = dpF(10)", "maxRefractionAmount = dpF(14)")
replace("backdropParent = this@LiquidGlassToggle.parent as? View ?: this@LiquidGlassToggle", "backdropParent = this@LiquidGlassToggle")
p.write_text(s)
# Retain complete MIT license in the installable APK.
lic=Path("app/src/main/assets/NATIVE-LIQUID-GLASS-LICENSE.txt")
lic.parent.mkdir(parents=True,exist_ok=True)
shutil.copyfile(src/"LICENSE",lic)
