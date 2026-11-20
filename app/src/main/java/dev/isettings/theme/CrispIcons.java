package dev.isettings.theme;
import android.graphics.*;
// Independently drawn vector glyphs. Drawn at native display resolution; no upscaled screenshot masks.
final class CrispIcons {
 static void path(Canvas c,Paint p,float...a){Path x=new Path();x.moveTo(a[0],a[1]);for(int i=2;i<a.length;i+=2)x.lineTo(a[i],a[i+1]);c.drawPath(x,p);}
 static void stroke(Canvas c,Paint p,Path x){c.drawPath(x,p);}
 static boolean draw(Canvas c,Paint p,String k){
 p.setShader(null);p.setColor(Color.WHITE);p.setColorFilter(null);p.setStrokeWidth(1.55f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);Path x=new Path();
 switch(k){
 case "plane":
 p.setStyle(Paint.Style.FILL);x.moveTo(27,16);x.cubicTo(27,14.8f,25,14,22,14);x.lineTo(19,14);x.lineTo(10,5.8f);x.lineTo(7.4f,5.8f);x.lineTo(12.2f,14);x.lineTo(7.6f,14);x.lineTo(4.9f,10.8f);x.lineTo(3.2f,10.8f);x.lineTo(4.5f,16);x.lineTo(3.2f,21.2f);x.lineTo(4.9f,21.2f);x.lineTo(7.6f,18);x.lineTo(12.2f,18);x.lineTo(7.4f,26.2f);x.lineTo(10,26.2f);x.lineTo(19,18);x.lineTo(22,18);x.cubicTo(25,18,27,17.2f,27,16);x.close();c.drawPath(x,p);break;
 case "wifi":
 p.setStrokeWidth(2.2f);x.moveTo(6.4f,13.1f);x.quadTo(16,4.2f,25.6f,13.1f);stroke(c,p,x);x.reset();x.moveTo(10.2f,17.3f);x.quadTo(16,11.7f,21.8f,17.3f);stroke(c,p,x);p.setStyle(Paint.Style.FILL);c.drawCircle(16,22.2f,2.0f,p);break;
 case "bluetooth":p.setStrokeWidth(1.45f);path(c,p,16,6.4f,21.3f,11.7f,10.6f,22.2f);path(c,p,10.6f,9.8f,21.3f,20.4f,16,25.6f,16,6.4f);break;
 case "signal":p.setStyle(Paint.Style.FILL);c.drawCircle(16,12.1f,1.8f,p);p.setStyle(Paint.Style.STROKE);path(c,p,16,15.6f,16,26);for(int i=0;i<2;i++){float rx=4.5f+i*4.1f,ry=6.0f+i*4.3f;c.drawArc(16-rx,12-ry,16+rx,12+ry,145,70,false,p);c.drawArc(16-rx,12-ry,16+rx,12+ry,-35,70,false,p);}break;
 case "link":c.save();c.rotate(-2,16,16);x.moveTo(15.3f,11);x.lineTo(10.2f,11);x.cubicTo(3,11,3,21,10.2f,21);x.lineTo(14,21);x.cubicTo(18,21,20,17,17.6f,14.7f);c.drawPath(x,p);x.reset();x.moveTo(16.7f,21);x.lineTo(21.8f,21);x.cubicTo(29,21,29,11,21.8f,11);x.lineTo(18,11);x.cubicTo(14,11,12,15,14.4f,17.3f);c.drawPath(x,p);c.restore();break;
 case "battery":p.setStrokeWidth(1.5f);c.drawRoundRect(5,10.8f,25.6f,21.2f,2.9f,2.9f,p);path(c,p,27.5f,14.2f,27.5f,17.8f);p.setStyle(Paint.Style.FILL);c.drawRoundRect(7.3f,13,23.3f,19,1.2f,1.2f,p);break;
 case "gear":p.setStrokeWidth(1.25f);c.drawCircle(16,16,9.7f,p);c.drawCircle(16,16,2.8f,p);for(int i=0;i<16;i++){double a=i*Math.PI/8;float xx=(float)Math.cos(a),yy=(float)Math.sin(a);path(c,p,16+9.4f*xx,16+9.4f*yy,16+12.1f*xx,16+12.1f*yy);}for(int i=0;i<3;i++){double a=-Math.PI/2+i*Math.PI*2/3;path(c,p,16+(float)Math.cos(a)*3,16+(float)Math.sin(a)*3,16+(float)Math.cos(a)*9,16+(float)Math.sin(a)*9);}break;
 case "access":p.setStrokeWidth(1.45f);c.drawCircle(16,16,10.4f,p);p.setStyle(Paint.Style.FILL);c.drawCircle(16,9.3f,1.65f,p);p.setStyle(Paint.Style.STROKE);path(c,p,10,13.4f,16,14.6f,22,13.4f);path(c,p,16,14.6f,16,19,12.8f,24);path(c,p,16,19,19.2f,24);break;
 case "action":path(c,p,5,16,15,16,11.7f,12.7f);path(c,p,15,16,11.7f,19.3f);x.moveTo(19,25.5f);x.lineTo(19,12);x.quadTo(19,6.4f,26.4f,6.4f);stroke(c,p,x);break;
 case "ai":p.setStrokeWidth(1.45f);for(int i=0;i<3;i++){c.save();c.rotate(i*60,16,16);c.drawRoundRect(6,10.5f,26,21.5f,5.5f,5.5f,p);c.restore();}break;
 case "sun":p.setStyle(Paint.Style.FILL);c.drawCircle(16,16,4.9f,p);p.setStyle(Paint.Style.STROKE);for(int i=0;i<8;i++){double a=i*Math.PI/4;path(c,p,16+(float)Math.cos(a)*8,16+(float)Math.sin(a)*8,16+(float)Math.cos(a)*11.5f,16+(float)Math.sin(a)*11.5f);}break;
 case "grid":c.drawRoundRect(9.8f,4.8f,22.2f,27.2f,2.6f,2.6f,p);p.setStyle(Paint.Style.FILL);for(int r=0;r<4;r++)for(int j=0;j<3;j++)c.drawRoundRect(12+j*3.1f,8+r*4,14+j*3.1f,10+r*4,.6f,.6f,p);break;
 case "image":p.setStrokeWidth(1.5f);for(int i=0;i<8;i++){c.save();c.rotate(i*45,16,16);c.drawOval(12.7f,4.5f,19.3f,16.8f,p);c.restore();}c.drawCircle(16,16,2.7f,p);break;
 case "bell":p.setStyle(Paint.Style.FILL);x.moveTo(5.7f,22);x.quadTo(8.9f,19,9,13);x.quadTo(9,6,16,6);x.quadTo(23,6,23,13);x.quadTo(23,19,26.3f,22);x.quadTo(26.3f,23.8f,24.5f,23.8f);x.lineTo(7.5f,23.8f);x.quadTo(5.7f,23.8f,5.7f,22);x.close();c.drawPath(x,p);c.drawOval(13.5f,25,18.5f,28,p);break;
 case "sound":p.setStyle(Paint.Style.FILL);x.moveTo(5,12);x.lineTo(10,12);x.lineTo(15.5f,7.2f);x.lineTo(15.5f,24.8f);x.lineTo(10,20);x.lineTo(5,20);x.close();c.drawPath(x,p);p.setStyle(Paint.Style.STROKE);for(int i=0;i<3;i++){float z=4+i*3.6f;c.drawArc(16-z,16-z,16+z,16+z,-65,130,false,p);}break;
 case "moon":p.setStyle(Paint.Style.FILL);x.addCircle(16,15.5f,10.4f,Path.Direction.CW);Path cut=new Path();cut.addCircle(21,11,9.4f,Path.Direction.CW);x.op(cut,Path.Op.DIFFERENCE);c.drawPath(x,p);break;
 case "time":path(c,p,9.5f,6,22.5f,6);path(c,p,9.5f,26,22.5f,26);x.moveTo(10.5f,6);x.cubicTo(10.5f,13,16,13,16,16);x.cubicTo(16,19,10.5f,19,10.5f,26);stroke(c,p,x);x.reset();x.moveTo(21.5f,6);x.cubicTo(21.5f,13,16,13,16,16);x.cubicTo(16,19,21.5f,19,21.5f,26);stroke(c,p,x);break;
 default:return false;
 }p.setStyle(Paint.Style.FILL);return true;
 }
}
