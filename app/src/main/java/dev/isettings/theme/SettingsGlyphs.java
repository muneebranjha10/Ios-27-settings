package dev.isettings.theme;

import android.graphics.*;

// Independently drawn native vector symbols, based on the provided Settings screenshot.
// No Apple SF Symbols or proprietary icon binaries are included.
final class SettingsGlyphs {
 static void path(Canvas c,Paint p,float... xy){Path q=new Path();q.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)q.lineTo(xy[i],xy[i+1]);c.drawPath(q,p);}
 static boolean draw(Canvas c,Paint p,String k){
  p.setShader(null);p.setColor(Color.WHITE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeWidth(1.65f);p.setStyle(Paint.Style.STROKE);
  switch(k){
   case "wifi":
    c.drawArc(3,9,29,34,228,84,false,p);c.drawArc(8,14,24,30,228,84,false,p);p.setStyle(Paint.Style.FILL);c.drawCircle(16,22,1.9f,p);break;
   case "bluetooth":path(c,p,16,5.5f,22,11.5f,10,23);path(c,p,10,9,22,20.5f,16,26.5f,16,5.5f);break;
   case "signal":
    p.setStyle(Paint.Style.FILL);c.drawCircle(16,12,1.8f,p);p.setStyle(Paint.Style.STROKE);path(c,p,16,15,16,27);c.drawArc(8,5,24,21,125,110,false,p);c.drawArc(8,5,24,21,-55,110,false,p);c.drawArc(3,1,29,25,140,80,false,p);c.drawArc(3,1,29,25,-40,80,false,p);break;
   case "link":
    c.drawRoundRect(4.5f,10.5f,19,21.5f,5.5f,5.5f,p);c.drawRoundRect(13,10.5f,27.5f,21.5f,5.5f,5.5f,p);break;
   case "battery":
    c.drawRoundRect(5,10.5f,26,21.5f,2.8f,2.8f,p);path(c,p,28,14,28,18);p.setStyle(Paint.Style.FILL);c.drawRoundRect(7.5f,13,23.5f,19,1.5f,1.5f,p);break;
   case "gear":
    c.drawCircle(16,16,10.5f,p);c.drawCircle(16,16,3.2f,p);for(int i=0;i<12;i++){double a=Math.PI*i/6;float x=(float)Math.cos(a),y=(float)Math.sin(a);path(c,p,16+x*9,16+y*9,16+x*12,16+y*12);}for(int i=0;i<3;i++){double a=Math.PI*i*2/3-.5;path(c,p,16+(float)Math.cos(a)*3.4f,16+(float)Math.sin(a)*3.4f,16+(float)Math.cos(a)*9,16+(float)Math.sin(a)*9);}break;
   case "access":
    c.drawCircle(16,16,11,p);p.setStyle(Paint.Style.FILL);c.drawCircle(16,9,1.8f,p);p.setStyle(Paint.Style.STROKE);path(c,p,9.8f,13,16,14,22.2f,13);path(c,p,16,14,16,19,12.5f,24);path(c,p,16,19,19.5f,24);break;
   case "action":
    path(c,p,5,16,15,16,11,12);path(c,p,15,16,11,20);path(c,p,20,26,20,12,25,7,28,7);break;
   case "ai":
    for(int i=0;i<3;i++){c.save();c.rotate(i*60,16,16);c.drawOval(5,11,27,21,p);c.restore();}p.setStyle(Paint.Style.FILL);c.drawCircle(16,16,1.6f,p);break;
   default:return false;
  }
  p.setStyle(Paint.Style.FILL);return true;
 }
}
