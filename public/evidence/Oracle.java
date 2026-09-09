import java.util.*;
public class Oracle {
static final int PRED_NONE=0,PRED_CORNER=1,PRED_H=2,PRED_V=3,PRED_PAETH=9,CLAMP_MOD256=1;
int round(float x){return Math.round(x);} int abs(int x){return Math.abs(x);} int constrain(int x,int a,int b){return Math.min(b,Math.max(a,x));}
class Segment {int x,y,size,pred_type;Segment(int a,int b,int n){x=a;y=b;size=n;}}
class RefColor {int[] c={128,128,128};}
class Planes {int w,h;int[][][] channels;RefColor ref=new RefColor();Planes(int a,int b){w=a;h=b;channels=new int[3][w][h];}
public int get(int pno, int x, int y) {
    if (x<0 || x>=w || y<0 || y>=h) {
      return ref.c[pno];
    } else {
      return channels[pno][x][y];
    }
  }
void set(int pno, int x, int y, int val) {
    if (x>=0 && x<w && y>=0 && y<h) {
      channels[pno][x][y] = val;
    }
  }
void subtract(int pno, Segment s, int[][] values, int clamp_method) {
    for (int x=0; x<s.size; x++) {
      for (int y=0; y<s.size; y++) {
        int v = get(pno, x+s.x, y+s.y) - values[x][y];
        set(pno, x+s.x, y+s.y, clamp_in(clamp_method, v));
      }
    }
  }
void add(int pno, Segment s, int[][] values, int clamp_method) {
    for (int x=0; x<s.size; x++) {
      for (int y=0; y<s.size; y++) {
        int v = get(pno, x+s.x, y+s.y) + values[x][y];
        set(pno, x+s.x, y+s.y, clamp_out(clamp_method, v));
      }
    }
  }
}
int[][] pred_gen(Planes p, int pno, Segment s, int type) {
  int[][] res = new int[s.size][s.size];

  for (int x=0; x<s.size; x++) {
    for (int y=0; y<s.size; y++) {
      switch(type) {
      case 0: 
        res[x][y] = p.get(pno, s.x-1, s.y-1); 
        break;
      case 1: 
        res[x][y] = p.get(pno, s.x-1, s.y+y); 
        break;
      case 2: 
        res[x][y] = p.get(pno, s.x+x, s.y-1); 
        break;
      }
    }
  }

  switch(type) {
  case 0: 
    s.pred_type = PRED_CORNER; 
    break;
  case 1: 
    s.pred_type = PRED_H; 
    break;
  case 2: 
    s.pred_type = PRED_V; 
    break;
  }
  return res;
}
int[][] pred_paeth(Planes p, int pno, Segment s) {
  int[][] res = new int[s.size][s.size];
  int c = p.get(pno, s.x-1, s.y-1);

  for (int x=0; x<s.size; x++) {
    int v1 = p.get(pno, s.x+x, s.y-1);
    for (int y=0; y<s.size; y++) {
      int v2 = p.get(pno, s.x-1, s.y+y);
      int pp = v1+v2-c;
      int pa = abs(pp-v2);
      int pb = abs(pp-v1);
      int pc = abs(pp-c);
      int v = ((pa<=pb) && (pa<=pc))?v2:(pb<=pc?v1:c);
      res[x][y] = constrain(v, 0, 255);
    }
  } 

  s.pred_type = PRED_PAETH;
  return res;
}
void quantize(Planes planes, int p, Segment s, float val, boolean forward) {
  if (val > 1) {
    for (int x=0; x<s.size; x++) {
      for (int y=0; y<s.size; y++) {
        float col = planes.get(p, x+s.x, y+s.y); 

        if (forward)
          col = col / val;
        else
          col = col * val;

        planes.set(p, x+s.x, y+s.y, (int)round(col));
      }
    }
  }
}
int clamp_in(int method, int x) {
  switch(method) {
  case CLAMP_MOD256:
    return x<0?x+256:x>255?x-256:x;
  default: 
    return x;
  }
}
int clamp_out(int method, int x) {
  switch(method) {
  case CLAMP_MOD256: 
    return x<0?x+256:x>255?x-256:x;
  default: 
    return constrain(x, 0, 255);
  }
}
int[][] prediction(int mode,Planes p,int c,Segment s){if(mode==2)return pred_gen(p,c,s,1);if(mode==3)return pred_gen(p,c,s,2);if(mode==9)return pred_paeth(p,c,s);return new int[s.size][s.size];}
void blocks(ArrayList<Segment> out,int x,int y,int n,int size,int w,int h){if(x>=w||y>=h)return;if(n<=size){out.add(new Segment(x,y,n));return;}int m=n/2;blocks(out,x,y,m,size,w,h);blocks(out,x+m,y,m,size,w,h);blocks(out,x,y+m,m,size,w,h);blocks(out,x+m,y+m,m,size,w,h);}
void run(){boolean first=true;System.out.print("[");for(int[] dim:new int[][]{{1,1},{1,17},{17,1},{17,19}})for(int bs:new int[]{8,16,32,64})for(int mode:new int[]{0,2,3,9})for(int q:new int[]{0,1,2,3,4,7,64,255}){
int w=dim[0],h=dim[1];Planes p=new Planes(w,h),encoded=new Planes(w,h),decoded=new Planes(w,h);for(int c=0;c<3;c++)for(int x=0;x<w;x++)for(int y=0;y<h;y++)p.set(c,x,y,(x*37+y*71+c*89+(x*y)%29)%256);
ArrayList<Segment> segs=new ArrayList<>();int n=1;while(n<Math.max(w,h))n*=2;blocks(segs,0,0,n,bs,w,h);
for(int c=0;c<3;c++)for(Segment s:segs){int[][] pred=prediction(mode,p,c,s);p.subtract(c,s,pred,0);quantize(p,c,s,q/2.0f,true);for(int x=0;x<s.size;x++)for(int y=0;y<s.size;y++){encoded.set(c,s.x+x,s.y+y,p.get(c,s.x+x,s.y+y));decoded.set(c,s.x+x,s.y+y,p.get(c,s.x+x,s.y+y));}quantize(p,c,s,q/2.0f,false);pred=prediction(mode,p,c,s);p.add(c,s,pred,0);}
for(int c=0;c<3;c++)for(Segment s:segs){quantize(decoded,c,s,q/2.0f,false);decoded.add(c,s,prediction(mode,decoded,c,s),0);}
if(!first)System.out.print(",");first=false;System.out.print("{\"w\":"+w+",\"h\":"+h+",\"block\":"+bs+",\"predictor\":"+mode+",\"quant\":"+q+",\"encoded\":");printPlane(encoded);System.out.print(",\"output\":");printPlane(decoded);System.out.print("}");
}System.out.print("]");}
void printPlane(Planes p){System.out.print("[");boolean first=true;for(int y=0;y<p.h;y++)for(int x=0;x<p.w;x++)for(int c=0;c<3;c++){if(!first)System.out.print(",");first=false;System.out.print(p.get(c,x,y));}System.out.print("]");}
public static void main(String[] args){new Oracle().run();}
}
