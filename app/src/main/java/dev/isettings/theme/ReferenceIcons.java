package dev.isettings.theme;
import android.graphics.*;
import android.util.Base64;
import java.util.*;
import java.util.zip.Inflater;
// Alpha masks derived from the user's supplied screenshot references; no Apple font files.
final class ReferenceIcons {
 private static final Map<String,Bitmap> cache=new HashMap<>();
 static synchronized Bitmap load(String kind){
  if(cache.containsKey(kind))return cache.get(kind);
  String data;switch(kind){
   case "plane": data="eNpjYKABYDQSROGLlBkKIPOFu1pQ+EzV2w1RNHidLkbSbcAgdvYYQoN4EQPz2tcBcD7nUgbGuW+XCDBC+ew7XNP3vbs9ozwIwmdbPWvvu///7t5ZBuXv7lxz7//bM7unwdQru+37t7MsDaqeYykD09y3ixH2JTGwrL2FsI/ZgEF871YBVPcWofg/eweKf5gri1G8x5SmgBpgigwDBQDzKTBK";break;
   case "wifi": data="eNpjYBgIwKRsJIDgMZp1rd69IhnO99xz5+7du7eKoVz2PWdWlKXPPnMqAKK6584UBaARUWdPgM2Q2L0UTDNG32kE0azTBRhUMsqcGJhmQTQoMqitvXv3VBKDMNRAljV3T++5e9IAZp/mmeXOJtWnG2B8thVAKeZZDnAHKTIIKTIoI7mfdfYKB2T/aJ69MxGZz7r3Foo8Q9RktABQYBgcAABXIS7n";break;
   case "bluetooth": data="eNpjYKAJEELjRyai8nN2KKDws2dNRuFXua10QOZnKVgiFDC1CWQpiC+B8xmrSqqUqooQqllXrc5Yiqzd8uxJA2Q+y7kjKNZp3D2BLM/ctbtrigDC/MiWauNZCTjtZxAAuk8cxQNZxp0BKP6rRPVfNpr/PQtRw0eYgU4AADyOIA8=";break;
   case "signal": data="eNpjYKA5YAyB0C4CEJqlFUJHOEBojSZGCF0I4Xs6qBkwMDAHsjRD+BUCmUCVLC2MbRB+OxCyGACJcojxbQytDBbNDBkCGQh+xFQgPwKmvg1oB5DKgJrHkC7AXsDYylgG4UcaqDuwF7AWMbdA+JoBTCB5BbYiCJ8V6A52oNssoO5lKgHyCxgYXKH+YRCE8AWR/CxWgBoGbGh81gRUPpMCw8AAACoSHJg=";break;
   case "link": data="eNpjYKA/EEDjCxsbGxshuKIdQNCeBOMyVpa6hqalT4fpYpkKZDEyZjpA+WyNDAxMrikWSSYQFewNDAzmM2eYF1cWQ+QLGBjLjITEEoQ7IPqbGVgLgUoMGCwMwAIVQeJJxqalQJUJYL5YmXpyR3sinM8gJFZkbAik1QOgVjK3gqxizFCAOTGiPC0tLb0F7gVmIDct1RDFl4ICDIMQAABDPh9I";break;
   case "battery": data="eNpjYBh0gFEJDIwUoHzVDghIFYDwg4QgCoQCIMqN0tPLgaAswAHCZzn7DgTeLhCA8u/9B4G3CxVgfLD8u4Uw+XNg7h2YeuHZu0FgV4EBhB+oDLJNWVEQYh+DWygEpEEdyOwCBs7GDEMQAABZCDQ3";break;
   case "gear": data="eNpjYKAGYA0QM0DiOnIey2kQhnOZds37//7lLgUYn/n9rfbKdX9gfKlV7xYyMLCs2z0Rwuf823kYSEn+L4Dw2Z8znQLRfQkQu9ZuZMgNADJ0T4JIBrb7CQxSmwWA6v5CFKxVYGB9ux1ozSuI7WeAak6uTRRkvg220ecXAwPXQZnbu8+83wziawCVcRyJXVZWfrcQrGGNAAPLW6BaxltgLsteAwb2E0FAxhuwfvZ7DQw+DSB3vwU7kMnyCMNskEzsZIiP2H+bXQXpOxcAcb/k3bMTGBgYc+40QPgsqu+3CAh5vHWCBRnjuXt39r59LQALH8YS7Vf7JrkJIAKUrUEyADm8GQUYGagDABxHUcs=";break;
   case "access": data="eNpjYKAGYFRSEkDiMkWsWlWqgOBW7ZpeuWoaXMBiZ5AAg0pXE5TLPCuRgdmAQWwnVIHENgYGq8UMDNmNEH50IQODNZCvsRXC7xRgUlQNElJgXAHRvo1BrXPWzBmJDFVgA9iASpXnnjAEagwA8TkWAonaa0DCqgDMXwDiHwMSWg0I+eNwefbNIDumAwmvBBCfFegcxhZ3oP+yDMC+WSHAwNRkocDAOBPi5ywHBpYEcQMG1qUQ92lOZmALYAtgsIK6n3mlARMDowLLSgOoh72WATUyZU2GhQdz17Rg18zpBvAAE6navWu6A3KAurgoMNAEAAAxYDZ8";break;
   case "action": data="eNpjYKAVYBQUROYyV86cIYDE11geGoIsnxWAqnumACp/OgNuvpsCA/NyJH7NJAaJ43CVxq5zbxpoXYfxWTpnnb1ToA3nM7mErr3loH0cSX/OYgaJ7cjmGzAw47MfG7+NND5DKhpfkIEuAAAJ0RnJ";break;
   case "ai": data="eNpjYKAUCAgyCKp2znCE8QWFBIU8p1dNhitgVFHucmBZLigA5bNUztrlZLJ65gwDCF8jJWvlzJmzl5gWQJR3KHgmGRupL2IoB/OZpzN1KDAwMC1nyADzmWZYTAHRXkHtEPuqZwaAaLYZS6H8rRBzo7eAKZHdixnBDO2TCmB90yDqGKIyG0HUbKeVEP4M5iUgapZhNdilTNsYl4HonkAIn2ULK1hj1I5ZYIezHs+aBKJNO3e3Ag1mzjo53RDEV2TM6mxjYKxaOQXiYQEGC4HIZovJTA0MYHc4G0U6MHdMF2BdJOICVs8gVsDgGsIglsBoDHY/A2OoqwJzSKgAkwmIH2IkqGTEwKBsJGwSBuKHOitCgzU0jeLIYwAA4AhBSA==";break;
   case "sun": data="eNpjYKAJYBRA5XMvQOXzbEDiqED4TjB+fQIDswED+3cYX/oLiIyHG8HYxFQ5TcEDyQTf//+PINt+/927v0gu4Pz37t37BphkRwEPiL+Aox3MZ7p7gOcdiM97B6aeCyQ/AaGfBSj/zgHJgvp/758jcR1Y995yEIZz2b8yMDIw5CfA+Plgo6S+wvgRQBuAQlNwhQcQ8KLxWR1oE08MAGyTNcg=";break;
   case "grid": data="eNpjYKAGEGQUFETi5v579+7dUTiX5c9MILhnAONzPAKR/gUwPueDaMmiI3xwPseDPOnJT/mQ5BkZGQT0kORjpJqOyCHxozlR9D8EkXpI+vOkF6DwvSQKkPlo6jkeoPEJyRNUr87mWITs3nrdxd+Q/Qv0nwJCnu2HkpGycr8DPADX/weCl4jwZQ4FAgOqxBwAg9U6YA==";break;
   case "image": data="eNpjYKAKYDZA4bKcOYMsIMBbwHGAEc71eT2bgeHEvsMwxc8tfjIw7Gvug2gRlGlg6AsQe8HAuVEQLCBnwJD32O8pI8tFiHruBoas3uPHGDgWQPjsT42j7Y4cMc5PgPDLc6/NnXvqZu21NjCXq4Hlqe/vfZfzDLgmgPg2Agyx5zZIvz3CwHQJypd5N4Hr3UYonyeB+Xjsi3lXaxQ4NoAN2GFzZe7duzfiNndBzOf4WVYTe+xq+voCiHs5FzBE1V07wsA9AeJeWQOG3CuxTxlg7uVJYKhPYP/GwL4Rwme9IvqKgWFvYKwDNABqzuxiYNh95ho8gBRkAtguMCEFINvtswEoASyWSJ14YwAA0rVXHA==";break;
   case "bell": data="eNpjYKAJsEhgm66A4LK8b9j//xKCr/OP9d+7X3Au672/nP/e/RWA8e3+/eX49+4PXH7/u3dG799/h3GZ3r97v8H3bQNc+793SIYxMLAB+e9vhMHtB5r17t3//wYo/HfvHGB8TjD/fQKMLwPhL4Dx+8DK3z2FGf8OCqAG8sH4UA28aHwmFxdnZSMTFxe4/xiYjY0VkIOL+9/7Cch85n2vBFDCk8WANvHEAAD7wWV4";break;
   case "sound": data="eNpjYKAfEICzWEBMxiNw/vwCEDkHxuX6P4GBwYJBLwDCZdz/HsjXK+BaAOYZW/1738BswHGB9SKIb/Pu3bv3DewXmZ4wPQHx+8F8xmsM14EYZDaYz7CLYSfDCST+KiB/JxJ/J8MOiDxEP9NThmsQ/dp374HMP8D8mPkx2DmCEkD7GQU4D7BdgDkfqJ/BzoF7ApTPAeKLMtg6wDxUnwAid8H9ywT2yGKGgQcAp75KjA==";break;
   case "moon": data="eNpjYBgAwNiGyo/9hsJlfX8JhW/77iIKf/37g8hc5nfvUNRz/Hv3BJnP/e/dd2Q+z793b9D47wpQ1b97hmreu3eTkFwP5L77Oy00YgLMPSCB///eL4Dydd+BwRsBKJ8Fwn8MN8EHLG2AMLHm3f/bhcgBZBpqQJ+oAABLHEQf";break;
   case "time": data="eNpjYKADyFoFBFPhXOb3/4HgrwCc/w4E3sL5jPdB/N8I/f3v37379w3B513s4qJzAMFnvsLA4GuAZGFcANsTZAewPYlzQHHRyg5UF+7oROGy/6hPQObX/ah/hsRlffej/h2SgbrvD8j8O4jgz38twHj+BZzLdG4hA4P0G4Rz3wC9yngX7l+me7uBACHP4DETCJrpETMMAG/XPto=";break;
   default:return null;
  }
  Inflater inflater=new Inflater();try{inflater.setInput(Base64.decode(data,Base64.DEFAULT));byte[] a=new byte[512];int n=inflater.inflate(a);if(n!=512||!inflater.finished())return null;int[] colors=new int[1024];for(int i=0;i<512;i++){int v=a[i]&255;colors[i*2]=((v>>>4)*17<<24)|0xffffff;colors[i*2+1]=((v&15)*17<<24)|0xffffff;}Bitmap b=Bitmap.createBitmap(colors,32,32,Bitmap.Config.ARGB_8888);cache.put(kind,b);return b;}catch(Throwable e){return null;}finally{inflater.end();}
 }
 static int[] colors(String kind,int hi,int lo){switch(kind){
  case "plane":return new int[]{0xfffca754,0xfffe8c29};
  case "wifi":case "bluetooth":case "action":return new int[]{0xff3b9ff6,0xff0187fb};
  case "access":return new int[]{0xff479eec,0xff0d86ef};
  case "signal":case "link":case "battery":return new int[]{0xff79d88a,0xff33c955};
  case "gear":return new int[]{0xffababad,0xff908e94};
  case "sun":case "grid":return new int[]{0xff19a7ff,0xff0078fa};
  case "image":return new int[]{0xff29d7e8,0xff00b5da};
  case "bell":case "sound":return new int[]{0xffff5360,0xffff2347};
  case "moon":case "time":return new int[]{0xff7765ff,0xff5848f9};
  default:return new int[]{hi,lo};
 }}
}
