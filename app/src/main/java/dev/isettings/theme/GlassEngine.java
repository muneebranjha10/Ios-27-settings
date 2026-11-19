package dev.isettings.theme;
import android.graphics.*;

// Derived from AndroidLiquidGlass 2.0.1, Shaders.kt, Copyright 2025 Kyant.
// SPDX-License-Identifier: Apache-2.0. Modified for native View/RenderNode and safe numerical bounds.
final class GlassEngine {
 static final String SHADER="""
uniform shader content;
uniform float2 size;
uniform float2 offset;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;
uniform float chromaticAberration;
float radiusAt(float2 coord,float4 radii){if(coord.x>=0.0){return coord.y<=0.0?radii.y:radii.z;}return coord.y<=0.0?radii.x:radii.w;}
float sdRoundedRect(float2 coord,float2 halfSize,float radius){float2 q=abs(coord)-(halfSize-float2(radius));return length(max(q,float2(0.0)))-radius+min(max(q.x,q.y),0.0);}
float2 gradSdRoundedRect(float2 coord,float2 halfSize,float radius){float2 q=abs(coord)-(halfSize-float2(radius));if(q.x>=0.0||q.y>=0.0)return sign(coord)*normalize(max(q,float2(0.0))+float2(0.00001));float gx=step(q.y,q.x);return sign(coord)*float2(gx,1.0-gx);}
float circleMap(float x){return 1.0-sqrt(max(0.0,1.0-x*x));}
half4 main(float2 coord){
 float2 hs=size*0.5;float2 centered=(coord+offset)-hs;float radius=radiusAt(centered,cornerRadii);
 float sd=sdRoundedRect(centered,hs,radius);if(-sd>=refractionHeight)return content.eval(coord);
 sd=min(sd,0.0);float d=circleMap(clamp(1.0+sd/max(0.01,refractionHeight),0.0,1.0))*refractionAmount;
 float gradRadius=min(radius*1.5,min(hs.x,hs.y));
 float2 grad=normalize(gradSdRoundedRect(centered,hs,gradRadius)+depthEffect*normalize(centered+float2(0.00001))+float2(0.00001));
 float2 p=coord+d*grad;float2 dispersed=d*grad*chromaticAberration*((centered.x*centered.y)/max(1.0,hs.x*hs.y));
 half4 color=half4(0.0);
 half4 red=content.eval(p+dispersed);color.r+=red.r/3.5;color.a+=red.a/7.0;
 half4 orange=content.eval(p+dispersed*(2.0/3.0));color.r+=orange.r/3.5;color.g+=orange.g/7.0;color.a+=orange.a/7.0;
 half4 yellow=content.eval(p+dispersed*(1.0/3.0));color.r+=yellow.r/3.5;color.g+=yellow.g/3.5;color.a+=yellow.a/7.0;
 half4 green=content.eval(p);color.g+=green.g/3.5;color.a+=green.a/7.0;
 half4 cyan=content.eval(p-dispersed*(1.0/3.0));color.g+=cyan.g/3.5;color.b+=cyan.b/3.0;color.a+=cyan.a/7.0;
 half4 blue=content.eval(p-dispersed*(2.0/3.0));color.b+=blue.b/3.0;color.a+=blue.a/7.0;
 half4 purple=content.eval(p-dispersed);color.r+=purple.r/7.0;color.b+=purple.b/3.0;color.a+=purple.a/7.0;
 return color;
}
 """;
 static void configure(RuntimeShader shader,float w,float h,float ox,float oy,float r,float height,float amount,float dispersion){
  shader.setFloatUniform("size",w,h);shader.setFloatUniform("offset",ox,oy);shader.setFloatUniform("cornerRadii",r,r,r,r);
  shader.setFloatUniform("refractionHeight",Math.max(.01f,height));shader.setFloatUniform("refractionAmount",-amount);
  shader.setFloatUniform("depthEffect",1f);shader.setFloatUniform("chromaticAberration",dispersion);
 }
 static RenderEffect effect(RuntimeShader shader,float blur){RenderEffect lens=RenderEffect.createRuntimeShaderEffect(shader,"content");return blur>.1f?RenderEffect.createChainEffect(lens,RenderEffect.createBlurEffect(blur,blur,Shader.TileMode.CLAMP)):lens;}
}
