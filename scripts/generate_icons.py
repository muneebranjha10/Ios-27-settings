#!/usr/bin/env python3
# Generate high-resolution alpha masks from the exact Ionicons version in the supplied demo lockfile.
import base64, pathlib, sys
import cairosvg
root=pathlib.Path(sys.argv[1])
icons={"plane":"airplane","wifi":"wifi","bluetooth":"bluetooth","signal":"stats-chart","link":"unlink","battery":"battery-full","gear":"settings","access":"accessibility","spark":"sparkles","sun":"sunny","sound":"volume-high","bell":"notifications","image":"image","grid":"grid","lock":"lock-closed","shield":"shield-checkmark","devices":"laptop-outline","moon":"moon","palette":"color-palette","location":"location","medical":"medical","people":"people","google":"logo-google","heart":"heart","family":"people-circle","speed":"speedometer","apps":"apps","update":"download","help":"help-circle","info":"information-circle","code":"code-slash","options":"options","search":"search-outline","chevron":"chevron-forward"}
out=pathlib.Path("app/src/main/java/dev/isettings/theme/IconMasks.java")
out.parent.mkdir(parents=True,exist_ok=True)
lines=["package dev.isettings.theme;","import android.graphics.*;","import android.util.Base64;","import java.util.HashMap;","final class IconMasks {","private static final HashMap<String,Bitmap> cache=new HashMap<>();","static synchronized Bitmap load(String key){ if(cache.containsKey(key))return cache.get(key); String data; switch(key){"]
for key,name in icons.items():
    svg=root/"dist"/"svg"/(name+".svg")
    if not svg.is_file(): raise FileNotFoundError(svg)
    png=cairosvg.svg2png(url=str(svg),output_width=96,output_height=96)
    enc=base64.b64encode(png).decode("ascii")
    lines.append('case "'+key+'": data="'+enc+'"; break;')
lines+=["default:return null;} byte[] b=Base64.decode(data,Base64.DEFAULT); Bitmap bitmap=BitmapFactory.decodeByteArray(b,0,b.length); cache.put(key,bitmap);return bitmap;}","}"]
out.write_text("\n".join(lines)+"\n")
assets=pathlib.Path("app/src/main/assets")
assets.mkdir(parents=True,exist_ok=True)
license=next((root/n for n in ("LICENSE","LICENSE.md","LICENSE.txt") if (root/n).is_file()),None)
if license is None:raise FileNotFoundError("Ionicons license")
(assets/"IONICONS-LICENSE.txt").write_bytes(license.read_bytes())
