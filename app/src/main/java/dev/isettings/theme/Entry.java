package dev.isettings.theme;

import android.app.Activity;
import android.animation.ValueAnimator;
import android.animation.ArgbEvaluator;
import android.view.animation.PathInterpolator;
import java.io.File;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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
  ContentObserver airplaneObserver; Glyph airplaneGlyph; LiquidSearch glassView,headerGlass; boolean mountedNow=false; boolean navigating=false;
  Theme(Activity a,FrameLayout root,Map<String,Object> prefs){
   this.a=a;this.root=root;this.prefs=prefs;dark=(a.getResources().getConfiguration().uiMode&48)==32;
   bg=dark?0xff101013:0xfff2f1f6;card=dark?0xff242426:0xffffffff;fg=dark?0xfff5f5f7:0xff111111;sub=dark?0xffa2a2aa:0xff8e8e93;line=dark?0xff3c3c40:0xffe5e5e8;
   nativeSearch=findSearch(root);layer=new FrameLayout(a);layer.setBackgroundColor(bg);layer.setClickable(true);
   scroll=new ScrollView(a);scroll.setVerticalScrollBarEnabled(false);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
   content=new LinearLayout(a);content.setOrientation(1);content.setPadding(dp(20),dp(48),dp(20),dp(84));scroll.addView(content,new ScrollView.LayoutParams(-1,-2));layer.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
  }
  int dp(float x){return Math.round(a.getResources().getDisplayMetrics().density*x);}
  GradientDrawable round(int c,float r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
  TextView text(String s,float z,int color,boolean bold){TextView v=new RegularText(a,bold?650:400);v.setText(s);v.setTextSize(z);v.setTextColor(color);v.setIncludeFontPadding(false);return v;}
  LinearLayout group(){LinearLayout g=new LinearLayout(a);g.setOrientation(1);g.setBackground(round(card,28));g.setClipToOutline(true);content.addView(g,new LinearLayout.LayoutParams(-1,-2));return g;}
  void gap(int n){content.addView(new View(a),new LinearLayout.LayoutParams(1,dp(n)));}
  String value(Object p,String method){try{Object x=XposedHelpers.callMethod(p,method);return x==null?"":x.toString();}catch(Throwable e){return "";}}
  void mount(){
   TextView largeTitle=text("Settings",32,fg,true);content.addView(largeTitle);gap(10);
   Object account=prefs.get("top_level_samsung_account");
   if(account!=null){used.add("top_level_samsung_account");LinearLayout g=group(),row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(16),dp(12),dp(16),dp(12));View avatar=accountAvatar(account);row.addView(avatar,new LinearLayout.LayoutParams(dp(44),dp(44)));LinearLayout words=new LinearLayout(a);words.setOrientation(1);LinearLayout.LayoutParams w=new LinearLayout.LayoutParams(0,-2,1);w.leftMargin=dp(12);row.addView(words,w);String name=value(account,"getTitle");TextView accountName=text(name.isEmpty()?"Samsung Account":name,18,fg,true);accountName.setSingleLine(true);accountName.setEllipsize(android.text.TextUtils.TruncateAt.END);words.addView(accountName);String summary=value(account,"getSummary");TextView desc=text("Samsung account",13,sub,false);desc.setMaxLines(2);words.addView(desc);row.addView(new Glyph(a,"chevron",sub,sub,false,dark),new LinearLayout.LayoutParams(dp(17),dp(22)));g.addView(row,new LinearLayout.LayoutParams(-1,-2));row.setMinimumHeight(dp(76));interactive(row,()->openPref(account));gap(32);}
   LinearLayout g=group();
   row(g,"Airplane Mode","plane",0xfff8b250,0xfff79407,"",()->toggleAirplane(),true);
   row(g,"Wi-Fi","wifi",0xff489bfd,0xff2b77f4,wifi(),()->action(Settings.ACTION_WIFI_SETTINGS),false);
   row(g,"Bluetooth","bluetooth",0xff459cfa,0xff2d7afc,bluetooth(),()->action(Settings.ACTION_BLUETOOTH_SETTINGS),false);
   row(g,"Mobile Service","signal",0xff6fdc83,0xff3ec75d,"",()->action(Settings.ACTION_DATA_ROAMING_SETTINGS),false);
   row(g,"Personal Hotspot","link",0xff6fdc83,0xff3ec75d,"",()->action("android.settings.TETHER_SETTINGS"),false);
   pref(g,"top_level_battery","Battery","battery",0xff6fdc83,0xff3ec75d);gap(32);
   g=group();pref(g,"top_level_general","General","gear",0xffa9a9ae,0xff83838a);pref(g,"top_level_accessibility","Accessibility","access",0xff489bfd,0xff2b77f4);pref(g,"top_level_advanced_features",null,"action",0xff489bfd,0xff2b77f4);pref(g,"top_level_galaxy_ai",null,"ai",0xffc4a0ff,0xff8073e8);pref(g,"top_level_display",null,"sun",0xff489bfd,0xff2b77f4);pref(g,"top_level_wallpaper",null,"image",0xff489bfd,0xff2b77f4);pref(g,"top_level_homescreen",null,"grid",0xff489bfd,0xff2b77f4);pref(g,"top_level_lockscreen",null,"lock",0xff9b9de8,0xff696bd3);gap(32);g=group();pref(g,"top_level_notifications",null,"bell",0xffff5360,0xffff2347);pref(g,"top_level_sounds",null,"sound",0xffff5360,0xffff2347);if(g.getChildCount()==0)content.removeView(g);else gap(32);
   g=group();for(Map.Entry<String,Object> e:prefs.entrySet())if(!used.contains(e.getKey())){Object p=e.getValue();String n=value(p,"getTitle");if(!n.isEmpty()){String kind=category(e.getKey(),n);int[] colors=palette(kind);row(g,n,kind,colors[0],colors[1],"",()->openPref(p),false);}}
   if(g.getChildCount()==0)content.removeView(g);
   gap(16);TextView original=text("Original Samsung Settings · v0.7",15,0xff2b77f4,false);original.setGravity(Gravity.CENTER);content.addView(original,new LinearLayout.LayoutParams(-1,dp(48)));original.setOnClickListener(v->nativePage());
   FrameLayout search=new FrameLayout(a);search.setElevation(dp(3));search.setClipToOutline(true);search.setBackground(round(dark?0xff333337:0xfff3f3f6,24));
   glassView=new LiquidSearch(a,content,bg,dark,false);search.addView(glassView,new FrameLayout.LayoutParams(-1,-1));
   LinearLayout labels=new LinearLayout(a);labels.setGravity(Gravity.CENTER_VERTICAL);labels.setPadding(dp(14),0,dp(14),0);labels.addView(new Glyph(a,"search",sub,sub,false,dark),new LinearLayout.LayoutParams(dp(22),dp(22)));
   TextView searchLabel=text("Search",16,dark?0xffe0e0e5:0xff65656b,false);LinearLayout.LayoutParams sw=new LinearLayout.LayoutParams(0,-2,1);sw.leftMargin=dp(8);labels.addView(searchLabel,sw);
   search.addView(labels,new FrameLayout.LayoutParams(-1,-1));interactive(search,()->search());search.setContentDescription("Search Samsung Settings");
   FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,dp(46),Gravity.BOTTOM);sp.setMargins(dp(24),0,dp(24),dp(12));layer.addView(search,sp);
   FrameLayout header=new FrameLayout(a);header.setElevation(dp(4));header.setAlpha(0f);header.setVisibility(View.INVISIBLE);
   headerGlass=new LiquidSearch(a,content,bg,dark,true);header.addView(headerGlass,new FrameLayout.LayoutParams(-1,-1));
   TextView heading=text("Settings",17,fg,true);heading.setGravity(Gravity.CENTER);header.addView(heading,new FrameLayout.LayoutParams(-1,-1));
   FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,dp(49),Gravity.TOP);layer.addView(header,hp);
   scroll.setOnScrollChangeListener((v,x,y,ox,oy)->{glassView.requestSample();float fraction=Math.max(0,Math.min(1,(y-dp(40))/(float)dp(40)));largeTitle.setAlpha(1f-fraction);header.setVisibility(fraction>0?View.VISIBLE:View.INVISIBLE);header.setAlpha(fraction);if(fraction>0)headerGlass.requestSample();});
   search.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->glassView.requestSample());header.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->headerGlass.requestSample());

   for(int i=0;i<root.getChildCount();i++){View v=root.getChildAt(i);importance.put(v,v.getImportantForAccessibility());v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);}
   root.addView(layer,new FrameLayout.LayoutParams(-1,-1));mountedNow=true;
   Runnable fit=()->{if(!mountedNow||root.getHeight()==0)return;WindowInsets insets=root.getRootWindowInsets();if(insets==null)return;int[] where=new int[2];root.getLocationOnScreen(where);Rect bounds=a.getWindowManager().getCurrentWindowMetrics().getBounds();android.graphics.Insets bars=insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());int top=Math.max(0,bounds.top+bars.top-where[1]);int bottom=Math.max(0,where[1]+root.getHeight()-(bounds.bottom-bars.bottom));FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)scroll.getLayoutParams();if(lp.topMargin!=top||lp.bottomMargin!=bottom){lp.topMargin=top;lp.bottomMargin=bottom;scroll.setLayoutParams(lp);}if(hp.topMargin!=top){hp.topMargin=top;header.setLayoutParams(hp);}if(sp.bottomMargin!=dp(12)+bottom){sp.bottomMargin=dp(12)+bottom;search.setLayoutParams(sp);}};layer.setOnApplyWindowInsetsListener((v,insets)->{fit.run();return insets;});layer.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->fit.run());layer.post(fit);layer.requestApplyInsets();
   try{airplaneObserver=new ContentObserver(handler){@Override public void onChange(boolean self){refreshAirplane();}};a.getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON),false,airplaneObserver);}catch(Throwable e){log("observer "+e);}
  }
  View accountAvatar(Object account){try{Object icon=XposedHelpers.callMethod(account,"getIcon");if(icon instanceof Drawable){ImageView image=new ImageView(a);Drawable d=(Drawable)icon;Drawable.ConstantState st=d.getConstantState();image.setImageDrawable(st==null?d:st.newDrawable(a.getResources()).mutate());image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setBackground(round(dark?0xff39393c:0xffe9edf3,22));image.setClipToOutline(true);image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);return image;}}catch(Throwable e){log("account avatar unavailable "+e);}return new Glyph(a,"account",0xff489bfd,0xff2b77f4,false,dark);}
  void interactive(View v,Runnable click){
   v.setOnTouchListener((view,event)->{if(!ValueAnimator.areAnimatorsEnabled())return false;switch(event.getActionMasked()){case MotionEvent.ACTION_DOWN:view.animate().cancel();if(view instanceof FrameLayout&&glassView!=null)glassView.press(true,event.getX(),event.getY());if(airplaneGlyph!=null&&airplaneGlyph.getParent()==view)airplaneGlyph.press(true);view.animate().scaleX(.985f).scaleY(.97f).alpha(.98f).setInterpolator(new DecelerateInterpolator()).setDuration(110).start();break;case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:view.animate().cancel();if(view instanceof FrameLayout&&glassView!=null)glassView.press(false,event.getX(),event.getY());if(airplaneGlyph!=null&&airplaneGlyph.getParent()==view)airplaneGlyph.press(false);view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(340).setInterpolator(new OvershootInterpolator(1.25f)).start();break;}return false;});
   v.setOnClickListener(view->{if(!mountedNow||navigating)return;boolean toggle=airplaneGlyph!=null&&airplaneGlyph.getParent()==view; if(toggle||!ValueAnimator.areAnimatorsEnabled()){try{click.run();}catch(Throwable e){error(e);}return;}navigating=true;handler.postDelayed(()->{navigating=false;if(mountedNow)try{click.run();}catch(Throwable e){error(e);}},view instanceof FrameLayout?210:125);});
  }
  String category(String key,String name){String k=(key+" "+name).toLowerCase(Locale.ROOT);if(k.contains("warranty"))return "shield";if(k.contains("connect"))return k.contains("device")?"devices":"wifi";if(k.contains("routine")||k.contains("mode"))return "moon";if(k.contains("theme"))return "palette";if(k.contains("privacy")||k.contains("security"))return "shield";if(k.contains("location"))return "location";if(k.contains("emergency")||k.contains("safety"))return "medical";if(k.contains("account")||k.contains("backup"))return "people";if(k.contains("google"))return "google";if(k.contains("wellbeing")||k.contains("well_being"))return "time";if(k.contains("parent"))return "family";if(k.contains("device care")||k.contains("device_care")||k.contains("devicecare"))return "speed";if(k.contains("apps")||k.contains("application"))return "apps";if(k.contains("update"))return "update";if(k.contains("guide")||k.contains("manual"))return "help";if(k.contains("about"))return "info";if(k.contains("developer"))return "code";return "options";}
  int[] palette(String k){switch(k){case "heart":case "medical":return new int[]{0xffff778a,0xffed4362};case "shield":case "family":case "speed":return new int[]{0xff6fdc83,0xff3ec75d};case "moon":case "palette":return new int[]{0xffaa92fa,0xff7b65de};case "options":case "code":case "info":return new int[]{0xffa9a9ae,0xff83838a};default:return new int[]{0xff489bfd,0xff2b77f4};}}
  void pref(LinearLayout g,String key,String alias,String kind,int hi,int lo){Object p=prefs.get(key);if(p==null)return;used.add(key);row(g,alias==null?value(p,"getTitle"):alias,kind,hi,lo,"",()->openPref(p),false);}
  void row(LinearLayout g,String name,String kind,int hi,int lo,String status,Runnable click,boolean toggle){
   if(g.getChildCount()>0){View v=new View(a);v.setBackgroundColor(line);LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(-1,Math.max(1,dp(.5f)));l.setMargins(dp(60),0,dp(16),0);g.addView(v,l);}
   LinearLayout r=new LinearLayout(a);r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(dp(16),0,dp(16),0);r.addView(new Glyph(a,kind,hi,lo,true,dark),new LinearLayout.LayoutParams(dp(27),dp(27)));TextView label=text(name,16,fg,false);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(0,-2,1);l.leftMargin=dp(13);r.addView(label,l);
   if(toggle){airplaneGlyph=new Glyph(a,"switch",0,0,false,dark);airplaneGlyph.setOn(isAirplane(),false);r.addView(airplaneGlyph,new LinearLayout.LayoutParams(dp(64),dp(34)));r.setContentDescription("Airplane Mode, "+(airplaneGlyph.on?"on":"off"));airplaneGlyph.setClickable(true);airplaneGlyph.setOnTouchListener((view,event)->{switch(event.getActionMasked()){case MotionEvent.ACTION_DOWN: view.getParent().requestDisallowInterceptTouchEvent(true);airplaneGlyph.beginDrag(event.getX());return true;case MotionEvent.ACTION_MOVE:airplaneGlyph.drag(event.getX());return true;case MotionEvent.ACTION_UP: view.getParent().requestDisallowInterceptTouchEvent(false);boolean target=airplaneGlyph.endDrag(false);if(target!=isAirplane())toggleAirplane();else airplaneGlyph.setOn(isAirplane(),true);return true;case MotionEvent.ACTION_CANCEL:view.getParent().requestDisallowInterceptTouchEvent(false);airplaneGlyph.endDrag(true);return true;}return true;});}
   else{if(!status.isEmpty()){TextView note=text(status,16,sub,false);note.setSingleLine(true);note.setMaxWidth(dp(105));note.setEllipsize(android.text.TextUtils.TruncateAt.END);r.addView(note);}r.addView(new Glyph(a,"chevron",sub,sub,false,dark),new LinearLayout.LayoutParams(dp(17),dp(22)));}
   g.addView(r,new LinearLayout.LayoutParams(-1,dp(47)));interactive(r,click);r.setFocusable(true);
  }
  void refreshAirplane(){if(airplaneGlyph!=null&&mountedNow){airplaneGlyph.setOn(isAirplane(),true);((View)airplaneGlyph.getParent()).setContentDescription("Airplane Mode, "+(airplaneGlyph.on?"on":"off"));}}
  boolean isAirplane(){return Settings.Global.getInt(a.getContentResolver(),Settings.Global.AIRPLANE_MODE_ON,0)==1;}
  void toggleAirplane(){boolean next=!isAirplane();try{ConnectivityManager c=(ConnectivityManager)a.getSystemService(Context.CONNECTIVITY_SERVICE);if(c==null)throw new IllegalStateException("no connectivity service");XposedHelpers.callMethod(c,"setAirplaneMode",next);handler.postDelayed(()->{if(mountedNow){refreshAirplane();if(isAirplane()!=next)action(Settings.ACTION_AIRPLANE_MODE_SETTINGS);}},450);}catch(Throwable e){action(Settings.ACTION_AIRPLANE_MODE_SETTINGS);}}
  String wifi(){try{WifiManager w=(WifiManager)a.getApplicationContext().getSystemService(Context.WIFI_SERVICE);return w==null?"":w.isWifiEnabled()?"On":"Off";}catch(Throwable e){return "";}}
  String bluetooth(){try{BluetoothManager b=(BluetoothManager)a.getSystemService(Context.BLUETOOTH_SERVICE);return b==null||b.getAdapter()==null?"":b.getAdapter().isEnabled()?"On":"Off";}catch(Throwable e){return "";}}
  void nativePage(){suppressed=new WeakReference<>(a);remove();}
  void openPref(Object p){try{if(!Boolean.TRUE.equals(XposedHelpers.callMethod(p,"isEnabled")))return;nativePage();XposedHelpers.callMethod(p,"performClick");}catch(Throwable e){error(e);}}
  void action(String action){try{Intent i=new Intent(action);i.setPackage("com.android.settings");if(i.resolveActivity(a.getPackageManager())==null){Object p=prefs.get("top_level_connections");if(p!=null){openPref(p);return;}throw new IllegalStateException("no native activity");}a.startActivity(i);}catch(Throwable e){error(e);}}
  void search(){nativePage();try{if(nativeSearch!=null&&nativeSearch.performClick())return;Toast.makeText(a,"Use the original Settings search",Toast.LENGTH_SHORT).show();}catch(Throwable e){error(e);}}
  void error(Throwable e){log("action "+e);Toast.makeText(a,"Native action unavailable",Toast.LENGTH_SHORT).show();}
  void remove(){mountedNow=false;if(glassView!=null)glassView.stop();if(headerGlass!=null)headerGlass.stop();if(airplaneGlyph!=null)airplaneGlyph.stop();handler.removeCallbacksAndMessages(null);if(airplaneObserver!=null){try{a.getContentResolver().unregisterContentObserver(airplaneObserver);}catch(Throwable ignored){}airplaneObserver=null;}if(layer.getParent()==root)root.removeView(layer);for(Map.Entry<View,Integer> v:importance.entrySet())v.getKey().setImportantForAccessibility(v.getValue());importance.clear();}
  static View findSearch(View v){String id="";try{if(v.getId()!=View.NO_ID)id=v.getResources().getResourceEntryName(v.getId()).toLowerCase(Locale.ROOT);}catch(Throwable ignored){}if(v.isShown()&&v.isClickable()&&(id.contains("search")||v.getClass().getName().equals("com.samsung.android.settings.homepage.SearchView")))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findSearch(g.getChildAt(i));if(x!=null)return x;}}return null;}
 }
 static final class LocalFonts {
  private static final Map<Integer,Typeface> cache=new HashMap<>();
  static synchronized Typeface get(Context c,int weight){Typeface hit=cache.get(weight);if(hit!=null)return hit;String style=weight>=600?"Semibold":"Regular";String[] names={"SF-Pro-Text-"+style+".otf","SF-Pro-Display-"+style+".otf","SFUIText-"+style+".otf"};for(String dir:new String[]{new File(c.getFilesDir(),"isettings-fonts").getAbsolutePath(),"/system/fonts","/product/fonts"})for(String name:names)try{File f=new File(dir,name);if(f.isFile()&&f.canRead()){Typeface face=Typeface.createFromFile(f);cache.put(weight,face);return face;}}catch(Throwable ignored){} Typeface face=Typeface.create(Typeface.create("sans-serif",Typeface.NORMAL),weight,false);cache.put(weight,face);return face;}
 }
 static final class RegularText extends TextView {
  final Typeface chosen;
  RegularText(Context c,int weight){super(c);chosen=LocalFonts.get(c,weight);super.setTypeface(chosen);applyWeight();}
  void applyWeight(){if(chosen!=null){getPaint().setTypeface(chosen);getPaint().setFakeBoldText(false);getPaint().setTextSkewX(0);}}
  @Override protected void onMeasure(int w,int h){applyWeight();super.onMeasure(w,h);applyWeight();}
  @Override protected void onDraw(Canvas c){applyWeight();super.onDraw(c);}
 }
 // Native-view adaptation of the rounded-rectangle refraction and interactive highlight
 // from AndroidLiquidGlass 2.0.1, Copyright 2025 Kyant, Apache-2.0.
 // Modified: RenderNode capture, capsule coordinates, material tint, press uniforms.
 static final class LiquidSearch extends View {
  final View source;final int bg;final boolean dark,header;final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);final int[] src=new int[2],dst=new int[2];
  final RenderNode node=new RenderNode("iSettings backdrop");RuntimeShader shader;boolean stopped,dirty=true,ready,logged;float progress,touchX,touchY;int pad;ValueAnimator motion;
  LiquidSearch(Context c,View source,int bg,boolean dark,boolean header){super(c);this.source=source;this.bg=bg;this.dark=dark;this.header=header;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);try{if(Build.VERSION.SDK_INT>=33)shader=new RuntimeShader(GlassEngine.SHADER);}catch(Throwable e){log("glass_shader_init_failed "+e);}}
  int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
  void requestSample(){if(!stopped){dirty=true;postInvalidateOnAnimation();}}
  void press(boolean down,float x,float y){touchX=x;touchY=y;if(motion!=null)motion.cancel();if(!ValueAnimator.areAnimatorsEnabled()){progress=down?1:0;invalidate();return;}motion=ValueAnimator.ofFloat(progress,down?1f:0f);motion.setDuration(down?125:480);motion.setInterpolator(down?new DecelerateInterpolator():new OvershootInterpolator(.85f));motion.addUpdateListener(v->{progress=(Float)v.getAnimatedValue();postInvalidateOnAnimation();});motion.start();}
  void stop(){stopped=true;if(motion!=null)motion.cancel();node.discardDisplayList();shader=null;ready=false;}
  @Override protected void onSizeChanged(int w,int h,int ow,int oh){super.onSizeChanged(w,h,ow,oh);pad=dp(32);dirty=true;ready=false;requestSample();}
  void capture(int w,int h){node.setPosition(0,0,w+2*pad,h+2*pad);RecordingCanvas rec=node.beginRecording();try{rec.drawColor(bg);source.getLocationOnScreen(src);getLocationOnScreen(dst);rec.save();try{rec.translate(src[0]-dst[0]+pad,src[1]-dst[1]+pad);source.draw(rec);}finally{rec.restore();}}finally{node.endRecording();}ready=true;dirty=false;}
  @Override protected void onDraw(Canvas c){super.onDraw(c);int w=getWidth(),h=getHeight();if(w==0||h==0||stopped)return;float r=header?0:h*.5f,t=Math.max(0,Math.min(1,progress));boolean drawn=false;
   paint.setShader(null);paint.setStyle(Paint.Style.FILL);Path clip=new Path();clip.addRoundRect(0,0,w,h,r,r,Path.Direction.CW);
   if(shader!=null&&c.isHardwareAccelerated())try{if(dirty||!ready)capture(w,h);GlassEngine.configure(shader,w,h,-pad,-pad,r,header?dp(1):dp(10)+dp(5)*t,header?0:dp(20)+dp(14)*t,header?0:0.9f);
    node.setRenderEffect(GlassEngine.effect(shader,header?dp(13):dp(3.5f)-dp(1.5f)*t));c.save();try{c.clipPath(clip);c.translate(-pad,-pad);c.drawRenderNode(node);drawn=true;}finally{c.restore();}
    if(!logged){logged=true;log("v0.7 GPU glass active "+(header?"header":"search"));}
   }catch(Throwable e){shader=null;log("v0.7 glass fallback "+e);}
   if(!drawn){paint.setColor(bg);c.drawRoundRect(0,0,w,h,r,r,paint);}
   paint.setColor(header?(dark?0xd0101013:0xcaf7f7fa):(dark?0x50303035:0x55f8f8fb));c.drawRoundRect(0,0,w,h,r,r,paint);
   if(header){paint.setColor(dark?0x33444448:0x22a0a0a8);c.drawRect(0,h-1,w,h,paint);return;}
   paint.setShader(new LinearGradient(0,0,w,h,new int[]{dark?0xe0ffffff:0xffffffff,0x20ffffff,dark?0x88ffffff:0x85b8b8c0},new float[]{0,.45f,1},Shader.TileMode.CLAMP));paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1,dp(.65f)));c.drawRoundRect(1,1,w-1,h-1,r-1,r-1,paint);paint.setStyle(Paint.Style.FILL);paint.setShader(null);
   paint.setShader(new LinearGradient(0,0,0,h,new int[]{dark?0x55ffffff:0x9cffffff,0x00ffffff,0x00000000,dark?0x35000000:0x26ffffff},new float[]{0,.19f,.81f,1},Shader.TileMode.CLAMP));c.drawRoundRect(0,0,w,h,r,r,paint);paint.setShader(null);if(t>.005f){paint.setShader(new RadialGradient(Math.max(0,Math.min(w,touchX)),Math.max(0,Math.min(h,touchY)),h*2.2f,new int[]{((int)(t*80)<<24)|0xffffff,0x00ffffff},null,Shader.TileMode.CLAMP));c.drawRoundRect(0,0,w,h,r,r,paint);paint.setShader(null);}
  }
 }
 static final class Glyph extends View {
  final String kind;final int hi,lo;final boolean tile,dark;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);boolean on;float position,pressed;ValueAnimator spring,pressAnimation;final Bitmap mask;final Bitmap reference;float dragStart,dragPosition;boolean dragging,dragMoved;RuntimeShader thumbShader;RenderNode thumbNode;boolean thumbLogged;
  Glyph(Context c,String k,int hi,int lo,boolean tile,boolean dark){super(c);kind=k;this.hi=hi;this.lo=lo;this.tile=tile;this.dark=dark;reference=null;mask=IconMasks.load(k);if(k.equals("switch")&&Build.VERSION.SDK_INT>=33)try{thumbShader=new RuntimeShader(GlassEngine.SHADER);thumbNode=new RenderNode("liquid toggle");}catch(Throwable e){log("toggle shader init "+e);}setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  void stop(){if(spring!=null)spring.cancel();if(pressAnimation!=null)pressAnimation.cancel();if(thumbNode!=null)thumbNode.discardDisplayList();}
  void press(boolean down){if(pressAnimation!=null)pressAnimation.cancel();pressAnimation=ValueAnimator.ofFloat(pressed,down?1f:0f);pressAnimation.setDuration(down?160:420);pressAnimation.setInterpolator(down?new DecelerateInterpolator():new OvershootInterpolator(.8f));pressAnimation.addUpdateListener(v->{pressed=Math.max(0f,(Float)v.getAnimatedValue());invalidate();});if(ValueAnimator.areAnimatorsEnabled())pressAnimation.start();else{pressed=down?1f:0f;invalidate();}}
  void setOn(boolean state,boolean animate){if(on==state&&animate&&Math.abs(position-(state?1f:0f))<.002f)return;on=state;if(spring!=null)spring.cancel();float end=state?1f:0f;if(!animate||!ValueAnimator.areAnimatorsEnabled()){position=end;invalidate();return;}spring=ValueAnimator.ofFloat(position,end);spring.setDuration(480);spring.setInterpolator(new OvershootInterpolator(.8f));spring.addUpdateListener(v->{position=(Float)v.getAnimatedValue();invalidate();});spring.start();}
  void beginDrag(float x){dragging=true;dragMoved=false;dragStart=x;dragPosition=position;if(spring!=null)spring.cancel();press(true);}
  void drag(float x){if(!dragging)return;float dx=x-dragStart;if(Math.abs(dx)>getWidth()*.065f)dragMoved=true;if(dragMoved){position=Math.max(0,Math.min(1,dragPosition+dx/(getWidth()*.37f)));invalidate();}}
  boolean endDrag(boolean cancel){boolean target=cancel?on:dragMoved?position>=.5f:!on;dragging=false;press(false);if(cancel)setOn(on,true);return target;}
  void drawToggle(Canvas c){
   float w=getWidth(),h=getHeight(),density=getResources().getDisplayMetrics().density,trackH=28*density,cy=h*.5f;float t=Math.max(0,Math.min(1,position)),pr=Math.max(0,Math.min(1,pressed));
   float thumbW=40*density*(1f+.16f*pr),thumbH=24*density*(1f+.26f*pr),travel=w-44*density;float cx=22*density+travel*t;
   RectF track=new RectF(0,cy-trackH*.5f,w,cy+trackH*.5f),thumb=new RectF(cx-thumbW*.5f,cy-thumbH*.5f,cx+thumbW*.5f,cy+thumbH*.5f);
   int color=(Integer)new ArgbEvaluator().evaluate(t,dark?0xff57575b:0xffc7c7cc,dark?0xff30d158:0xff34c759);
   p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRoundRect(track,trackH*.5f,trackH*.5f,p);
   boolean glass=false;if(pr>.015f&&thumbShader!=null&&thumbNode!=null&&c.isHardwareAccelerated())try{
    int ww=Math.max(1,(int)Math.ceil(w)),hh=Math.max(1,(int)Math.ceil(h));thumbNode.setPosition(0,0,ww,hh);RecordingCanvas rc=thumbNode.beginRecording();
    try{rc.drawColor(dark?0xff242426:0xffffffff);rc.save();rc.scale(.70f,.52f+.23f*pr,cx,cy);p.setColor(color);rc.drawRoundRect(track,trackH*.5f,trackH*.5f,p);rc.restore();}finally{thumbNode.endRecording();}
    GlassEngine.configure(thumbShader,thumbW,thumbH,-thumb.left,-thumb.top,thumbH*.5f,5*density*pr,10*density*pr,.8f);
    thumbNode.setRenderEffect(GlassEngine.effect(thumbShader,8*density*(1-pr)));Path shape=new Path();shape.addRoundRect(thumb,thumbH*.5f,thumbH*.5f,Path.Direction.CW);c.save();try{c.clipPath(shape);c.drawRenderNode(thumbNode);glass=true;}finally{c.restore();}
    if(!thumbLogged){thumbLogged=true;log("v0.7 GPU glass active toggle");}
   }catch(Throwable e){thumbShader=null;log("toggle lens fallback "+e);}
   p.setColor(glass?((int)(105+(1f-pr)*150)<<24)|0xffffff:Color.WHITE);c.drawRoundRect(thumb,thumbH*.5f,thumbH*.5f,p);
   p.setShader(new LinearGradient(thumb.left,thumb.top,thumb.right,thumb.bottom,new int[]{0xffffffff,0x85ffffff,0xc0ffffff},null,Shader.TileMode.CLAMP));p.setStrokeWidth(Math.max(1,density*.7f));p.setStyle(Paint.Style.STROKE);c.drawRoundRect(thumb,thumbH*.5f,thumbH*.5f,p);p.setStyle(Paint.Style.FILL);p.setShader(null);
  }
  void path(Canvas c,float...xy){Path q=new Path();q.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)q.lineTo(xy[i],xy[i+1]);c.drawPath(q,p);}
  @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();c.scale(getWidth()/32f,getHeight()/32f);p.setShader(null);p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
   if(kind.equals("switch")){c.restore();drawToggle(c);return;}
   if(tile){int[] col=ReferenceIcons.colors(kind,hi,lo);p.setShader(new LinearGradient(0,0,0,32,col[0],col[1],Shader.TileMode.CLAMP));c.drawRoundRect(0,0,32,32,7.5f,7.5f,p);p.setShader(null);if(tile&&CrispIcons.draw(c,p,kind)){c.restore();return;}}
   if(tile&&SettingsGlyphs.draw(c,p,kind)){c.restore();return;}
   if(mask!=null){p.setColor(Color.WHITE);p.setColorFilter(new PorterDuffColorFilter(tile?Color.WHITE:hi,PorterDuff.Mode.SRC_IN));float pad=tile?4:2;c.drawBitmap(mask,null,new RectF(pad,pad,32-pad,32-pad),p);p.setColorFilter(null);c.restore();return;}
   p.setColor(tile?Color.WHITE:hi);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);
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
