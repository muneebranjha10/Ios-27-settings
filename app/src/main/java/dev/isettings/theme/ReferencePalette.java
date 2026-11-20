package dev.isettings.theme;
final class ReferencePalette {
 static int[] colors(String kind,int hi,int lo){switch(kind){
  case "plane":return new int[]{0xfffbb36b,0xfff48c25};
  case "wifi":case "bluetooth":case "action":return new int[]{0xff4eb2ed,0xff008ae5};
  case "access":return new int[]{0xff479eec,0xff0d86ef};
  case "signal":case "link":case "battery":return new int[]{0xff79d890,0xff38c65b};
  case "gear":return new int[]{0xffababad,0xff908e94};
  case "sun":case "grid":return new int[]{0xff19a7ff,0xff0078fa};
  case "image":return new int[]{0xff29d7e8,0xff00b5da};
  case "bell":case "sound":return new int[]{0xffff5360,0xffff2347};
  case "moon":case "time":return new int[]{0xff7765ff,0xff5848f9};
  default:return new int[]{hi,lo};
 }}
}
