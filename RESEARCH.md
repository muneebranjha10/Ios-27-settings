# Research and implementation decisions
- Native glass: https://github.com/JosephMontfort/Liquid-Glass-on-Android-Views/tree/a44972cf649dd8fae7fb819afcaa088405b20399 (MIT). Reviewed native View, toggle, thumb renderer, spring physics and backdrop capture. The upstream source explains the RenderNode recording/reentrancy problem. Use native host to avoid Compose window lifecycle collisions.
- https://github.com/Kyant0/AndroidLiquidGlass (Apache-2.0): original glass component reference; legacy fallback remains. No additional custom Compose host.
- https://github.com/sparrowcode/SettingsIconGenerator (MIT): measured iOS 27.1 color gradients and per-symbol sizing; used palette and optical sizing rules. Does not provide redistributable Apple symbols.
- https://github.com/zhrispineda/Settings-iOS : SwiftUI simulation, no declared license and relies on Apple private APIs. Reference structure only, no copied code.
- https://github.com/iVintageLover/Solid-Glass : packages, no declared license. Reference only; no assets copied.
- https://github.com/gearui/gearui-kit (Apache-2.0): list geometry reference; uses Kuikly, not a drop-in native Settings implementation.
- https://github.com/seunghan91/ios27-design-system (MIT): secondary design reference, not exact native Apple optics.

Device gates: module mounts; no Settings crash; shader visible on search scroll; airplane switch animates and follows confirmed system state; Wi-Fi switch and native network list work; light/dark icon alignment. CI compilation cannot validate these gates.
