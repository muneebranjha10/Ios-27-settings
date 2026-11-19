package dev.isettings.theme;

import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.bluetooth.BluetoothManager;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.lang.ref.WeakReference;
import java.util.*;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class Entry implements IXposedHookLoadPackage {
 static final String HOME="com.android.settings.homepage.SettingsHomepageActivity";
 static final String TOP="com.android.settings.homepage.TopLevelSettings";
 static WeakReference<Object> fragment=new WeakReference<>(null);
 static WeakReference<Activity> suppressed=new WeakReference<>(null);
 static WeakReference<Theme> mounted=new WeakReference<>(null);
 static void log(String s){XposedBridge.log("iSettings27: "+s);}
 static boolean home(Object a){return a!=null&&HOME.equals(a.getClass().getName());}
 @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p){
  if(!"com.android.settings".equals(p.packageName)||!p.packageName.equals(p.processName))return;
  try{
   Class<?> top=XposedHelpers.findClass(TOP,p.classLoader);
   XposedHelpers.findAndHookMethod(top,"onResume",new XC_MethodHook(){protected void afterHookedMethod(MethodHookParam p){fragment=new WeakReference<>(p.thisObject);schedule(p.thisObject);}});
  }catch(Throwable e){log("top hook unavailable "+e);}
  try{
   XposedHelpers.findAndHookMethod(Activity.class,"onPostResume",new XC_MethodHook(){protected void afterHookedMethod(MethodHookParam p){if(home(p.thisObject)){Object f=fragment.get();if(f!=null)schedule(f);}}});
   XposedHelpers.findAndHookMethod(Activity.class,"onStop",new XC_MethodHook(){protected void beforeHookedMethod(MethodHookParam p){if(home(p.thisObject)){Theme t=mounted.get();if(t!=null&&t.a==p.thisObject)t.remove();if(suppressed.get()==p.thisObject)suppressed.clear();}}});
   XposedHelpers.findAndHookMethod(Activity.class,"onDestroy",new XC_MethodHook(){protected void beforeHookedMethod(MethodHookParam p){if(home(p.thisObject)){Theme t=mounted.get();if(t!=null&&t.a==p.thisObject)t.remove();}}});
   XposedHelpers.findAndHookMethod(Activity.class,"onConfigurationChanged",Configuration.class,new XC_MethodHook(){protected void afterHookedMethod(MethodHookParam p){if(home(p.thisObject)){Theme t=mounted.get();if(t!=null&&t.a==p.thisObject)t.remove();Object f=fragment.get();if(f!=null)schedule(f);}}});
   log("lifecycle hooks registered");
  }catch(Throwable e){log("lifecycle hook unavailable "+e);}
 }
 static void schedule(Object f){try{Activity a=(Activity)XposedHelpers.callMethod(f,"getActivity");if(!home(a))return;View v=a.findViewById(android.R.id.content);if(v!=null)for(long delay:new long[]{0,180,600})v.postDelayed(()->install(a,f),delay);}catch(Throwable e){log("schedule "+e);}}
 static void install(Activity a,Object f){
  if(suppressed.get()==a||a.isFinishing()||a.isDestroyed()||!a.hasWindowFocus()||a.isInMultiWindowMode())return;
  Configuration cfg=a.getResources().getConfiguration();
  if(cfg.orientation!=Configuration.ORIENTATION_PORTRAIT||cfg.smallestScreenWidthDp>=600)return;
  Theme old=mounted.get();if(old!=null&&old.a==a&&old.layer.getParent()!=null)return;
  Theme t=null;
  try{
   if(XposedHelpers.callMethod(f,"getActivity")!=a||!Boolean.TRUE.equals(XposedHelpers.callMethod(f,"isResumed"))||!Boolean.TRUE.equals(XposedHelpers.callMethod(f,"isVisible")))return;
   Object list=XposedHelpers.callMethod(f,"getListView"), adapter=XposedHelpers.callMethod(list,"getAdapter");
   int n=(Integer)XposedHelpers.callMethod(adapter,"getItemCount");
   LinkedHashMap<String,Object> prefs=new LinkedHashMap<>();
   for(int i=0;i<n;i++){Object item=XposedHelpers.callMethod(adapter,"getItem",i);if(item==null)continue;Object k=XposedHelpers.callMethod(item,"getKey"), title=XposedHelpers.callMethod(item,"getTitle");if(k instanceof String&&title!=null&&!title.toString().trim().isEmpty()&&!k.toString().contains("inset_category"))prefs.put(k.toString(),item);}
   if(!prefs.containsKey("top_level_connections")||!prefs.containsKey("top_level_general"))return;
   View root=a.findViewById(android.R.id.content);if(!(root instanceof FrameLayout))return;
   if(old!=null)old.remove();t=new Theme(a,(FrameLayout)root,prefs);t.mount();mounted=new WeakReference<>(t);log("themed homepage mounted; "+prefs.size()+" native entries");
  }catch(Throwable e){if(t!=null)t.remove();log("native homepage retained "+e);}
 }
 static final class Theme {
  final Activity a;final FrameLayout root,layer;final ScrollView scroll;final LinearLayout content;final Map<String,Object> prefs;
  final Set<String> used=new HashSet<>();final IdentityHashMap<View,Integer> importance=new IdentityHashMap<>();
  final boolean dark;final int bg,card,fg,sub,line;final View nativeSearch;final Handler handler=new Handler(Looper.getMainLooper());
  ContentObserver airplaneObserver; Glyph airplaneGlyph; boolean mountedNow=false;
  Theme(Activity a,FrameLayout root,Map<String,Object> prefs){
   this.a=a;this.root=root;this.prefs=prefs;dark=(a.getResources().getConfiguration().uiMode&48)==32;
   bg=dark?0xff222326:0xfff2f1f6;card=dark?0xff000000:0xffffffff;fg=dark?0xfff5f5f7:0xff111111;sub=dark?0xff929298:0xff8e8e93;line=dark?0xff343437:0xffe5e5e8;
   nativeSearch=findSearch(root);layer=new FrameLayout(a);layer.setBackgroundColor(bg);layer.setClickable(true);
   scroll=new ScrollView(a);scroll.setVerticalScrollBarEnabled(false);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
   content=new LinearLayout(a);content.setOrientation(1);content.setPadding(dp(20),dp(48),dp(20),dp(84));scroll.addView(content,new ScrollView.LayoutParams(-1,-2));layer.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
  }
  int dp(float x){return Math.round(a.getResources().getDisplayMetrics().density*x);}
  GradientDrawable round(int c,float r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
  TextView text(String s,float z,int color,boolean bold){TextView v=new TextView(a);v.setText(s);v.setTextSize(z);v.setTextColor(color);v.setTypeface(Typeface.create(Typeface.create("sans-serif",Typeface.NORMAL),bold?700:400,false));v.getPaint().setFakeBoldText(false);v.setIncludeFontPadding(false);return v;}
  LinearLayout group(){LinearLayout g=new LinearLayout(a);g.setOrientation(1);g.setBackground(round(card,28));g.setClipToOutline(true);content.addView(g,new LinearLayout.LayoutParams(-1,-2));return g;}
  void gap(int n){content.addView(new View(a),new LinearLayout.LayoutParams(1,dp(n)));}
  String value(Object p,String method){try{Object x=XposedHelpers.callMethod(p,method);return x==null?"":x.toString();}catch(Throwable e){return "";}}
  void mount(){
   content.addView(text("Settings",34,fg,true));gap(10);
   Object account=prefs.get("top_level_samsung_account");
   if(account!=null){used.add("top_level_samsung_account");LinearLayout g=group(),row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(16),dp(12),dp(16),dp(12));row.addView(new Glyph(a,"account",0xff489bfd,0xff2b77f4,false,dark),new LinearLayout.LayoutParams(dp(58),dp(58)));LinearLayout words=new LinearLayout(a);words.setOrientation(1);LinearLayout.LayoutParams w=new LinearLayout.LayoutParams(0,-2,1);w.leftMargin=dp(12);row.addView(words,w);String name=value(account,"getTitle");words.addView(text(name.isEmpty()?"Samsung Account":name,19,fg,true));String summary=value(account,"getSummary");TextView desc=text(summary.isEmpty()?"Samsung account and services":summary,13,sub,false);desc.setMaxLines(2);words.addView(desc);row.addView(new Glyph(a,"chevron",sub,sub,false,dark),new LinearLayout.LayoutParams(dp(15),dp(20)));g.addView(row,new LinearLayout.LayoutParams(-1,-2));row.setMinimumHeight(dp(88));row.setOnClickListener(v->openPref(account));gap(32);}
   LinearLayout g=group();
   row(g,"Airplane Mode","plane",0xfff8b250,0xfff79407,"",()->toggleAirplane(),true);
   row(g,"Wi-Fi","wifi",0xff489bfd,0xff2b77f4,wifi(),()->action(Settings.ACTION_WIFI_SETTINGS),false);
   row(g,"Bluetooth","bluetooth",0xff459cfa,0xff2d7afc,bluetooth(),()->action(Settings.ACTION_BLUETOOTH_SETTINGS),false);
   row(g,"Mobile Service","signal",0xff6fdc83,0xff3ec75d,"",()->action(Settings.ACTION_DATA_ROAMING_SETTINGS),false);
   row(g,"Personal Hotspot","link",0xff6fdc83,0xff3ec75d,"",()->action("android.settings.TETHER_SETTINGS"),false);
   pref(g,"top_level_battery","Battery","battery",0xff6fdc83,0xff3ec75d);gap(32);
   g=group();pref(g,"top_level_general","General","gear",0xffa9a9ae,0xff83838a);pref(g,"top_level_accessibility","Accessibility","access",0xff489bfd,0xff2b77f4);pref(g,"top_level_advanced_features",null,"spark",0xff489bfd,0xff2b77f4);pref(g,"top_level_galaxy_ai",null,"spark",0xffc4a0ff,0xff8073e8);pref(g,"top_level_display",null,"sun",0xff489bfd,0xff2b77f4);pref(g,"top_level_sounds",null,"sound",0xffff7b91,0xffeb3d62);pref(g,"top_level_notifications",null,"bell",0xffff7b91,0xffeb3d62);pref(g,"top_level_wallpaper",null,"image",0xff489bfd,0xff2b77f4);pref(g,"top_level_homescreen",null,"grid",0xff489bfd,0xff2b77f4);pref(g,"top_level_lockscreen",null,"lock",0xff9b9de8,0xff696bd3);gap(32);
   g=group();for(Map.Entry<String,Object> e:prefs.entrySet())if(!used.contains(e.getKey())){Object p=e.getValue();String n=value(p,"getTitle");if(!n.isEmpty()){String kind=category(e.getKey(),n);int[] colors=palette(kind);row(g,n,kind,colors[0],colors[1],"",()->openPref(p),false);}}
   if(g.getChildCount()==0)content.removeView(g);
   gap(16);TextView original=text("Original Samsung Settings",15,0xff2b77f4,false);original.setGravity(Gravity.CENTER);content.addView(original,new LinearLayout.LayoutParams(-1,dp(48)));original.setOnClickListener(v->nativePage());
   LinearLayout search=new LinearLayout(a);search.setGravity(Gravity.CENTER_VERTICAL);search.setPadding(dp(14),0,dp(14),0);GradientDrawable glass=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,dark?new int[]{0xe648484c,0xe62e2e32}:new int[]{0xf2ffffff,0xe6eeeeF2});glass.setCornerRadius(dp(22));glass.setStroke(dp(1),dark?0x44ffffff:0xb8ffffff);search.setBackground(glass);search.setElevation(dp(2));search.addView(new Glyph(a,"search",sub,sub,false,dark),new LinearLayout.LayoutParams(dp(23),dp(23)));TextView s=text("Search",17,sub,false);LinearLayout.LayoutParams sw=new LinearLayout.LayoutParams(0,-2,1);sw.leftMargin=dp(8);search.addView(s,sw);search.setOnClickListener(v->search());search.setContentDescription("Search Samsung Settings");FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,dp(44),Gravity.BOTTOM);sp.setMargins(dp(24),0,dp(24),dp(12));layer.addView(search,sp);
   for(int i=0;i<root.getChildCount();i++){View v=root.getChildAt(i);importance.put(v,v.getImportantForAccessibility());v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);}
   root.addView(layer,new FrameLayout.LayoutParams(-1,-1));mountedNow=true;
   Runnable fit=()->{if(!mountedNow||root.getHeight()==0)return;WindowInsets insets=root.getRootWindowInsets();if(insets==null)return;int[] where=new int[2];root.getLocationOnScreen(where);Rect bounds=a.getWindowManager().getCurrentWindowMetrics().getBounds();android.graphics.Insets bars=insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());int top=Math.max(0,bounds.top+bars.top-where[1]);int bottom=Math.max(0,where[1]+root.getHeight()-(bounds.bottom-bars.bottom));FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)scroll.getLayoutParams();if(lp.topMargin!=top||lp.bottomMargin!=bottom){lp.topMargin=top;lp.bottomMargin=bottom;scroll.setLayoutParams(lp);}if(sp.bottomMargin!=dp(12)+bottom){sp.bottomMargin=dp(12)+bottom;search.setLayoutParams(sp);}};layer.setOnApplyWindowInsetsListener((v,insets)->{fit.run();return insets;});layer.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->fit.run());layer.post(fit);layer.requestApplyInsets();
   try{airplaneObserver=new ContentObserver(handler){@Override public void onChange(boolean self){refreshAirplane();}};a.getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON),false,airplaneObserver);}catch(Throwable e){log("observer "+e);}
  }
  String category(String key,String name){String k=(key+" "+name).toLowerCase(Locale.ROOT);if(k.contains("warranty"))return "shield";if(k.contains("connect"))return k.contains("device")?"devices":"wifi";if(k.contains("routine")||k.contains("mode"))return "moon";if(k.contains("theme"))return "palette";if(k.contains("privacy")||k.contains("security"))return "shield";if(k.contains("location"))return "location";if(k.contains("emergency")||k.contains("safety"))return "medical";if(k.contains("account")||k.contains("backup"))return "people";if(k.contains("google"))return "google";if(k.contains("wellbeing")||k.contains("well_being"))return "heart";if(k.contains("parent"))return "family";if(k.contains("device care")||k.contains("device_care")||k.contains("devicecare"))return "speed";if(k.contains("apps")||k.contains("application"))return "apps";if(k.contains("update"))return "update";if(k.contains("guide")||k.contains("manual"))return "help";if(k.contains("about"))return "info";if(k.contains("developer"))return "code";return "options";}
  int[] palette(String k){switch(k){case "heart":case "medical":return new int[]{0xffff778a,0xffed4362};case "shield":case "family":case "speed":return new int[]{0xff6fdc83,0xff3ec75d};case "moon":case "palette":return new int[]{0xffaa92fa,0xff7b65de};case "options":case "code":case "info":return new int[]{0xffa9a9ae,0xff83838a};default:return new int[]{0xff489bfd,0xff2b77f4};}}
  void pref(LinearLayout g,String key,String alias,String kind,int hi,int lo){Object p=prefs.get(key);if(p==null)return;used.add(key);row(g,alias==null?value(p,"getTitle"):alias,kind,hi,lo,"",()->openPref(p),false);}
  void row(LinearLayout g,String name,String kind,int hi,int lo,String status,Runnable click,boolean toggle){
   if(g.getChildCount()>0){View v=new View(a);v.setBackgroundColor(line);LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(-1,Math.max(1,dp(.5f)));l.setMargins(dp(60),0,dp(16),0);g.addView(v,l);}
   LinearLayout r=new LinearLayout(a);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(16),0,dp(16),0);r.addView(new Glyph(a,kind,hi,lo,true,dark),new LinearLayout.LayoutParams(dp(30),dp(30)));TextView label=text(name,17,fg,false);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(0,-2,1);l.leftMargin=dp(14);r.addView(label,l);
   if(toggle){airplaneGlyph=new Glyph(a,"switch",0,0,false,dark);airplaneGlyph.on=isAirplane();r.addView(airplaneGlyph,new LinearLayout.LayoutParams(dp(63),dp(28)));r.setContentDescription("Airplane Mode, "+(airplaneGlyph.on?"on":"off"));}
   else{if(!status.isEmpty()){TextView note=text(status,16,sub,false);note.setSingleLine(true);note.setMaxWidth(dp(105));note.setEllipsize(android.text.TextUtils.TruncateAt.END);r.addView(note);}r.addView(new Glyph(a,"chevron",sub,sub,false,dark),new LinearLayout.LayoutParams(dp(17),dp(22)));}
   g.addView(r,new LinearLayout.LayoutParams(-1,dp(53)));r.setOnClickListener(v->{try{click.run();}catch(Throwable e){error(e);}});r.setFocusable(true);
  }
  void refreshAirplane(){if(airplaneGlyph!=null&&mountedNow){airplaneGlyph.on=isAirplane();airplaneGlyph.invalidate();((View)airplaneGlyph.getParent()).setContentDescription("Airplane Mode, "+(airplaneGlyph.on?"on":"off"));}}
  boolean isAirplane(){return Settings.Global.getInt(a.getContentResolver(),Settings.Global.AIRPLANE_MODE_ON,0)==1;}
  void toggleAirplane(){boolean next=!isAirplane();try{ConnectivityManager c=(ConnectivityManager)a.getSystemService(Context.CONNECTIVITY_SERVICE);if(c==null)throw new IllegalStateException("no connectivity service");XposedHelpers.callMethod(c,"setAirplaneMode",next);handler.postDelayed(()->{if(mountedNow){refreshAirplane();if(isAirplane()!=next)action(Settings.ACTION_AIRPLANE_MODE_SETTINGS);}},450);}catch(Throwable e){action(Settings.ACTION_AIRPLANE_MODE_SETTINGS);}}
  String wifi(){try{WifiManager w=(WifiManager)a.getApplicationContext().getSystemService(Context.WIFI_SERVICE);return w==null?"":w.isWifiEnabled()?"On":"Off";}catch(Throwable e){return "";}}
  String bluetooth(){try{BluetoothManager b=(BluetoothManager)a.getSystemService(Context.BLUETOOTH_SERVICE);return b==null||b.getAdapter()==null?"":b.getAdapter().isEnabled()?"On":"Off";}catch(Throwable e){return "";}}
  void nativePage(){suppressed=new WeakReference<>(a);remove();}
  void openPref(Object p){try{if(!Boolean.TRUE.equals(XposedHelpers.callMethod(p,"isEnabled")))return;nativePage();XposedHelpers.callMethod(p,"performClick");}catch(Throwable e){error(e);}}
  void action(String action){try{Intent i=new Intent(action);i.setPackage("com.android.settings");if(i.resolveActivity(a.getPackageManager())==null){Object p=prefs.get("top_level_connections");if(p!=null){openPref(p);return;}throw new IllegalStateException("no native activity");}a.startActivity(i);}catch(Throwable e){error(e);}}
  void search(){nativePage();try{if(nativeSearch!=null&&nativeSearch.performClick())return;Toast.makeText(a,"Use the original Settings search",Toast.LENGTH_SHORT).show();}catch(Throwable e){error(e);}}
  void error(Throwable e){log("action "+e);Toast.makeText(a,"Native action unavailable",Toast.LENGTH_SHORT).show();}
  void remove(){mountedNow=false;handler.removeCallbacksAndMessages(null);if(airplaneObserver!=null){try{a.getContentResolver().unregisterContentObserver(airplaneObserver);}catch(Throwable ignored){}airplaneObserver=null;}if(layer.getParent()==root)root.removeView(layer);for(Map.Entry<View,Integer> v:importance.entrySet())v.getKey().setImportantForAccessibility(v.getValue());importance.clear();}
  static View findSearch(View v){String id="";try{if(v.getId()!=View.NO_ID)id=v.getResources().getResourceEntryName(v.getId()).toLowerCase(Locale.ROOT);}catch(Throwable ignored){}if(v.isShown()&&v.isClickable()&&(id.contains("search")||v.getClass().getName().equals("com.samsung.android.settings.homepage.SearchView")))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findSearch(g.getChildAt(i));if(x!=null)return x;}}return null;}
 }
 static final class Glyph extends View {
  final String kind;final int hi,lo;final boolean tile,dark;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);boolean on;final Bitmap mask;
  Glyph(Context c,String k,int hi,int lo,boolean tile,boolean dark){super(c);kind=k;this.hi=hi;this.lo=lo;this.tile=tile;this.dark=dark;mask=IconMasks.load(k);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  void path(Canvas c,float...xy){Path q=new Path();q.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)q.lineTo(xy[i],xy[i+1]);c.drawPath(q,p);}
  @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();c.scale(getWidth()/32f,getHeight()/32f);p.setShader(null);p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
   if(kind.equals("switch")){p.setColor(on?0xff34c759:dark?0xff555558:0xffc7c7cc);c.drawRoundRect(0,0,32,32,8,16,p);p.setColor(Color.WHITE);c.drawRoundRect(on?16:1,2,on?31:16,30,8,14,p);c.restore();return;}
   if(tile){p.setShader(new LinearGradient(0,0,0,32,hi,lo,Shader.TileMode.CLAMP));c.drawRoundRect(0,0,32,32,8,8,p);p.setShader(null);}
   if(mask!=null){p.setColor(Color.WHITE);p.setColorFilter(new PorterDuffColorFilter(tile?(dark?0xff111111:Color.WHITE):hi,PorterDuff.Mode.SRC_IN));float pad=tile?4:2;c.drawBitmap(mask,null,new RectF(pad,pad,32-pad,32-pad),p);p.setColorFilter(null);c.restore();return;}
   p.setColor(tile?(dark?0xff111111:Color.WHITE):hi);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);
   switch(kind){
    case "chevron":path(c,12,9,20,16,12,23);break;
    case "search":c.drawCircle(13,13,7,p);path(c,18,18,25,25);break;
    case "wifi":c.drawArc(4,8,28,28,222,96,false,p);c.drawArc(9,13,23,26,222,96,false,p);p.setStyle(Paint.Style.FILL);c.drawCircle(16,23,1.8f,p);break;
    case "bluetooth":path(c,16,5,16,27,23,21,9,10);path(c,9,22,23,11,16,5);break;
    case "plane":p.setStyle(Paint.Style.FILL);Path q=new Path();q.moveTo(27,15);q.lineTo(19,14);q.lineTo(11,5);q.lineTo(8,5);q.lineTo(12,14);q.lineTo(7,14);q.lineTo(4,10);q.lineTo(2,10);q.lineTo(4,16);q.lineTo(2,22);q.lineTo(4,22);q.lineTo(7,18);q.lineTo(12,18);q.lineTo(8,27);q.lineTo(11,27);q.lineTo(19,18);q.lineTo(27,17);q.close();c.drawPath(q,p);break;
    case "battery":c.drawRoundRect(5,10,25,22,3,3,p);path(c,27,14,27,18);p.setStyle(Paint.Style.FILL);c.drawRoundRect(8,13,22,19,1,1,p);break;
    case "signal":p.setStrokeWidth(3.4f);for(int i=0;i<4;i++)path(c,7+i*6,25,7+i*6,20-i*4);break;
    case "link":c.drawRoundRect(5,11,19,21,5,5,p);c.drawRoundRect(13,11,27,21,5,5,p);break;
    case "account":p.setStyle(Paint.Style.FILL);for(int i=0;i<24;i++){double angle=i*Math.PI/12;c.drawCircle(16+(float)Math.cos(angle)*14,16+(float)Math.sin(angle)*14,0.75f,p);}c.drawCircle(16,12,4,p);c.drawRoundRect(9,18,23,24,4,4,p);break;
    case "access":c.drawCircle(16,16,10,p);c.drawCircle(16,10,1.5f,p);path(c,10,14,22,14);path(c,16,14,16,19,12,24);path(c,16,19,20,24);break;
    case "lock":c.drawRoundRect(8,14,24,26,3,3,p);c.drawArc(11,5,21,20,180,180,false,p);break;
    case "sun":c.drawCircle(16,16,5,p);for(int i=0;i<8;i++){double angle=i*Math.PI/4;path(c,16+(float)Math.cos(angle)*8,16+(float)Math.sin(angle)*8,16+(float)Math.cos(angle)*12,16+(float)Math.sin(angle)*12);}break;
    case "spark":path(c,16,5,19,13,27,16,19,19,16,27,13,19,5,16,13,13,16,5);break;
    case "grid":for(int x=7;x<=18;x+=11)for(int y=7;y<=18;y+=11)c.drawRoundRect(x,y,x+7,y+7,1,1,p);break;
    case "bell":path(c,6,22,10,18,10,12,13,8,19,8,22,12,22,18,26,22,6,22);path(c,13,25,19,25);break;
    case "sound":path(c,6,12,11,12,17,7,17,25,11,20,6,20,6,12);c.drawArc(12,9,26,23,-60,120,false,p);break;
    case "image":c.drawRoundRect(5,6,27,26,3,3,p);path(c,6,23,12,16,17,21,22,14,27,20);c.drawCircle(12,11,2,p);break;
    default:c.drawCircle(16,16,8,p);c.drawCircle(16,16,3,p);for(int i=0;i<8;i++){double angle=i*Math.PI/4;path(c,16+(float)Math.cos(angle)*8,16+(float)Math.sin(angle)*8,16+(float)Math.cos(angle)*11,16+(float)Math.sin(angle)*11);}break;
   }c.restore();
  }
 }
}
