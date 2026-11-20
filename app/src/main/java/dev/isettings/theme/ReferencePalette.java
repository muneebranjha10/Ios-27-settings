package dev.isettings.theme;
// Palette measurements from Sparrow Code SettingsIconGenerator (MIT, 2023),
// see bundled license. Native symbol artwork is independently drawn.
final class ReferencePalette {
 static int[] colors(String kind,int hi,int lo){switch(kind){
 case "plane":return new int[]{0xffff9b36,0xffff8d28};
 case "wifi":case "bluetooth":case "action":case "access":case "sun":case "grid":return new int[]{0xff0093ff,0xff0088ff};
 case "signal":case "link":case "battery":return new int[]{0xff42d464,0xff34c759};
 case "gear":return new int[]{0xff9f9fa4,0xff8e8e93};
 case "bell":case "sound":return new int[]{0xffff3f42,0xffff383c};
 case "moon":case "time":return new int[]{0xff6d60ff,0xff6155f5};
 default:return new int[]{hi,lo};
 }}
}
