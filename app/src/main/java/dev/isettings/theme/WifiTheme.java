package dev.isettings.theme;

import android.app.Activity;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import de.robv.android.xposed.*;

// One UI 9 Wi-Fi layout adapter. Native Samsung lists, network dialogs, menus,
// scans and policies remain authoritative. Only the visual tree is adapted.
final class WifiTheme {
 static final String TARGET="com.samsung.android.settings.wifi.WifiSettings";
 static final WeakHashMap<Object,WifiTheme> active=new WeakHashMap<>();
 static void hook(ClassLoader cl){
  try{
   Class<?> c=XposedHelpers.findClass(TARGET,cl);
   XposedHelpers.findAndHookMethod(c,"onResume",new XC_MethodHook(){@Override protected void afterHookedMethod(MethodHookParam p){schedule(p.thisObject);}});
   XposedHelpers.findAndHookMethod(c,"onDestroyView",new XC_MethodHook(){@Override protected void beforeHookedMethod(MethodHookParam p){WifiTheme t=active.remove(p.thisObject);if(t!=null)t.close();}});
   Entry.log("v0.12 WifiSettings hooks registered");
  }catch(Throwable e){Entry.log("Wi-Fi theme unavailable "+e);}
 }
 static void schedule(Object f){
  try{View root=(View)XposedHelpers.callMethod(f,"getView");if(root!=null)for(long delay:new long[]{0,180,600})root.postDelayed(()->install(f),delay);}catch(Throwable e){Entry.log("wifi schedule "+e);}
 }
 static void install(Object f){
  if(active.containsKey(f))return;
  WifiTheme t=null;
  try{
   if(!Boolean.TRUE.equals(XposedHelpers.callMethod(f,"isResumed")))return;
   Activity a=(Activity)XposedHelpers.callMethod(f,"getActivity");if(a==null||a.isFinishing()||a.isDestroyed()||a.isInMultiWindowMode())return;
   if(a.getResources().getConfiguration().orientation!=1)return;
   Object controller=XposedHelpers.getObjectField(f,"mListController");
   if(controller==null)return;
   View scroll=(View)XposedHelpers.getObjectField(controller,"mScrollView");
   if(!(scroll instanceof ViewGroup)||((ViewGroup)scroll).getChildCount()!=1)return;
   View child=((ViewGroup)scroll).getChildAt(0);if(!(child instanceof LinearLayout))return;
   View bar=findByName(child,"SettingsMainSwitchBar");
   if(bar==null)return;
   t=new WifiTheme(a,(LinearLayout)child,scroll,bar,controller);
   t.mount();active.put(f,t);Entry.log("v0.12 Wi-Fi native theme mounted");
  }catch(Throwable e){if(t!=null)t.close();Entry.log("v0.12 Wi-Fi theme skipped "+android.util.Log.getStackTraceString(e));}
 }
 final Activity a; final LinearLayout body; final View scroll,bar; final Object controller;
 final boolean dark; final int bg,card,ink,sub; final Handler handler=new Handler(Looper.getMainLooper());
 final List<ViewGroup> lists=new ArrayList<>();
 final Set<View> styled=Collections.newSetFromMap(new WeakHashMap<>());
 final IdentityHashMap<View,android.graphics.drawable.Drawable> oldBg=new IdentityHashMap<>();
 LinearLayout header; LinearLayout switchRow; UpstreamGlassHost glass; Entry.Glyph nativeGlass; CompoundButton actual; int previousBarVisibility; boolean closed; boolean changing; long deadline;
 final Runnable tick=new Runnable(){public void run(){if(closed)return;try{refresh();}catch(Throwable e){Entry.log("Wi-Fi style refresh "+e);}if(!closed)handler.postDelayed(this,450);}};
 WifiTheme(Activity a,LinearLayout body,View scroll,View bar,Object controller){
  this.a=a;this.body=body;this.scroll=scroll;this.bar=bar;this.controller=controller;dark=(a.getResources().getConfiguration().uiMode&48)==32;
  bg=dark?0xff000000:0xfff2f1f6;card=dark?0xff1c1d22:0xffffffff;ink=dark?0xfff5f5fa:0xff111114;sub=dark?0xffa7a7af:0xff77777d;
 }
 int dp(float n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
 GradientDrawable rounded(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 void background(View v,android.graphics.drawable.Drawable d){if(!oldBg.containsKey(v))oldBg.put(v,v.getBackground());v.setBackground(d);}
 TextView text(String s,int sp,int color,boolean bold){TextView v=new Entry.RegularText(a,bold?650:400);v.setText(s);v.setTextSize(sp);v.setTextColor(color);v.setIncludeFontPadding(false);return v;}
 static View findByName(View v,String name){if(v.getClass().getSimpleName().equals(name))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View x=findByName(g.getChildAt(i),name);if(x!=null)return x;}}return null;}
 static CompoundButton findSwitch(View v){if(v instanceof CompoundButton)return (CompoundButton)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){CompoundButton b=findSwitch(g.getChildAt(i));if(b!=null)return b;}}return null;}
 void mount(){
  previousBarVisibility=bar.getVisibility();actual=findSwitch(bar);
  background(scroll,rounded(bg,0));background(body,rounded(bg,0));
  header=new LinearLayout(a);header.setOrientation(1);header.setPadding(dp(16),dp(16),dp(16),dp(8));header.setBackground(rounded(card,24));header.setClipChildren(false);header.setClipToPadding(false);
  Entry.Glyph icon=new Entry.Glyph(a,"wifi",0xff4eb2ed,0xff008ae5,true,dark);header.addView(icon,new LinearLayout.LayoutParams(dp(52),dp(52)));
  TextView title=text("Wi-Fi",23,ink,true);LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);titleLp.topMargin=dp(10);header.addView(title,titleLp);
  TextView help=text("Connect to Wi-Fi, view available networks, and manage settings for joining networks and nearby hotspots.",14,sub,false);help.setLineSpacing(dp(2),1);LinearLayout.LayoutParams helpLp=new LinearLayout.LayoutParams(-1,-2);helpLp.topMargin=dp(6);helpLp.bottomMargin=dp(12);header.addView(help,helpLp);
  LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.setMargins(dp(4),dp(10),dp(4),dp(12));body.addView(header,0,hp);
  // Only replace the visual switch if a real enabled native switch is available.
  // All state changes still go through the original CompoundButton click handler.
  if(actual!=null&&actual.isEnabled()&&previousBarVisibility==View.VISIBLE){
   switchRow=new LinearLayout(a);switchRow.setGravity(Gravity.CENTER_VERTICAL);switchRow.setClipChildren(false);switchRow.setClipToPadding(false);
   View line=new View(a);line.setBackgroundColor(dark?0xff36373c:0xffe4e4e8);header.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
   switchRow.addView(text("Wi-Fi",17,ink,false),new LinearLayout.LayoutParams(0,-2,1));header.addView(switchRow,new LinearLayout.LayoutParams(-1,dp(52)));
   attachToggle();
   bar.setVisibility(View.GONE);
   TextView original=text("Original Wi-Fi controls",13,0xff438dff,false);original.setPadding(0,dp(4),0,dp(6));header.addView(original,new LinearLayout.LayoutParams(-1,-2));original.setOnClickListener(v->restoreSwitch());
  }
  for(String key:new String[]{"mConnectedList","mAvailableList","mEasySetupList","mAutoHotspotList","mInstantHotspotList","mMcfAutoHotspotList"})try{
   Object x=XposedHelpers.getObjectField(controller,key);if(x instanceof ViewGroup){ViewGroup list=(ViewGroup)x;lists.add(list);background(list,rounded(card,22));list.setClipToOutline(true);}
  }catch(Throwable ignored){}
  // Remove the redundant white/black native content panel without affecting children.
  for(int i=0;i<body.getChildCount();i++){View v=body.getChildAt(i);if(v instanceof FrameLayout)background(v,rounded(bg,0));}
  handler.post(tick);
 }
 void attachToggle(){
  try{if(Entry.upstreamDisabled)throw new IllegalStateException("Compose fallback");
   glass=UpstreamGlassHost.toggle(a,card,dark,actual.isChecked(),this::select,()->{if(!closed)fallbackToggle();});
   switchRow.addView(glass,new LinearLayout.LayoutParams(dp(74),dp(48)));
  }catch(Throwable e){Entry.log("wifi glass init "+e);fallbackToggle();}
 }
 void fallbackToggle(){
  if(closed||switchRow==null)return;
  if(glass!=null){try{glass.close();}catch(Throwable ignored){}switchRow.removeView(glass);glass=null;}
  if(nativeGlass!=null)return;
  nativeGlass=new Entry.Glyph(a,"switch",0,0,false,dark);nativeGlass.setOn(actual.isChecked(),false);
  switchRow.addView(nativeGlass,new LinearLayout.LayoutParams(dp(62),dp(36)));
  nativeGlass.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);nativeGlass.setContentDescription("Wi-Fi switch");nativeGlass.setOnClickListener(v->select(!actual.isChecked()));
 }
 void select(boolean state){
  if(closed||actual==null||!actual.isEnabled()||changing)return;
  if(actual.isChecked()==state)return;
  changing=true;deadline=SystemClock.uptimeMillis()+1800;
  try{actual.performClick();}catch(Throwable e){Entry.log("wifi native click failed "+e);restoreSwitch();}
  handler.postDelayed(()->{changing=false;if(!closed)refresh();},500);
 }
 void restoreSwitch(){
  if(glass!=null){try{glass.close();}catch(Throwable ignored){}glass=null;}
  if(nativeGlass!=null){nativeGlass.stop();nativeGlass=null;}
  if(switchRow!=null){switchRow.removeAllViews();switchRow.setVisibility(View.GONE);}
  bar.setVisibility(previousBarVisibility);
 }
 void refresh(){
  if(actual!=null&&switchRow!=null&&switchRow.getVisibility()==View.VISIBLE){
   if(!actual.isEnabled()){restoreSwitch();}else{boolean state=actual.isChecked();if(glass!=null)glass.confirm(state);if(nativeGlass!=null)nativeGlass.setOn(state,true);}
  }
  for(ViewGroup list:lists){
   for(int i=0;i<list.getChildCount();i++){View row=list.getChildAt(i);if(styled.add(row)){background(row,rounded(Color.TRANSPARENT,0));row.setMinimumHeight(dp(48));styleTexts(row);}}
  }
 }
 void styleTexts(View v){
  if(v instanceof TextView && !(v instanceof EditText)&&!(v instanceof CompoundButton)){
   TextView t=(TextView)v;
   String id="";try{id=t.getResources().getResourceEntryName(t.getId());}catch(Throwable ignored){}
   if(id.equals("title")||id.contains("ssid")||id.equals("summary")){
    t.setTextSize(id.equals("summary")?13:16);t.setTextColor(id.equals("summary")?sub:ink);t.setTypeface(Typeface.create(Typeface.create("sans-serif",Typeface.NORMAL),400,false));t.getPaint().setFakeBoldText(false);
   }
  }
  if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)styleTexts(g.getChildAt(i));}
 }
 void close(){
  if(closed)return;closed=true;handler.removeCallbacksAndMessages(null);
  try{restoreSwitch();}catch(Throwable ignored){}
  try{if(header!=null&&header.getParent()==body)body.removeView(header);}catch(Throwable ignored){}
  for(Map.Entry<View,android.graphics.drawable.Drawable> x:oldBg.entrySet())try{x.getKey().setBackground(x.getValue());}catch(Throwable ignored){}
  oldBg.clear();lists.clear();styled.clear();
 }
}
