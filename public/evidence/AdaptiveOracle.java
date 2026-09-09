import java.util.*;import java.io.*;
public class AdaptiveOracle {
final int PRED_NONE=0;int cursor=0;
float random(float size){int n=(cursor++*73+19)&255;return n*size/256f;}
int max(int a,int b){return Math.max(a,b);}int min(int a,int b){return Math.min(a,b);}
float max(float a,float b){return Math.max(a,b);}float sq(float x){return x*x;}float sqrt(float x){return (float)Math.sqrt(x);}
class DefaultBitOutput{StringBuilder bits=new StringBuilder();void writeBoolean(boolean b){bits.append(b?'1':'0');}}
class Planes{int w,h,ww,hh,pattern;Planes(int w,int h,int pattern){this.w=w;this.h=h;this.pattern=pattern;ww=1;hh=1;while(ww<w)ww*=2;while(hh<h)hh*=2;}int get(int c,int x,int y){if(x<0||x>=w||y<0||y>=h)return 128;return switch(pattern){case 0->128;case 1->(x*7+y*3+c*31)&255;case 2->((x+y+c)%2)*255;case 3->x==w/2?255:0;default->x==w/2&&y==h/2?255:0;};}}
class Segment {
  int x,y,size;
  
  // needed for prediction
  int pred_type = PRED_NONE;
  float angle = -1;
  int refa = -1;
  int refx = Short.MAX_VALUE; /// set to -1 if you want grid effect
  int refy = Short.MAX_VALUE; /// set to -1 if you want grid effect
  
  public String toString() {
    return "x="+x+", y="+y+ ", size=" + size;
  }
}

ArrayList<Segment> makeSegmentation(DefaultBitOutput segm_out, Planes p, int pno, int min_size, int max_size, float thr) throws IOException {
  ArrayList<Segment> s = new ArrayList<Segment>();
  
  segment(segm_out, s, p, pno, 0, 0, max(p.ww,p.hh), max(1,min_size), min(512,max_size), thr);
  
  return s;
}

void segment(DefaultBitOutput segm_out, ArrayList<Segment> s, Planes p, int pno, int x, int y, int size, int min_size, int max_size, float thr) throws IOException {
  if(x>=p.w || y>=p.h) return;
  float currStdDev = calcStdDev(p, pno, x, y, size);
  if(size>max_size || (size>min_size && currStdDev>thr)) {
    segm_out.writeBoolean(true);
    int mid = size/2;
    segment(segm_out,s,p,pno,x,y,mid,min_size,max_size,thr);
    segment(segm_out,s,p,pno,x+mid,y,mid,min_size,max_size,thr);
    segment(segm_out,s,p,pno,x,y+mid,mid,min_size,max_size,thr);
    segment(segm_out,s,p,pno,x+mid,y+mid,mid,min_size,max_size,thr);
  } else {
    segm_out.writeBoolean(false);
    Segment segm = new Segment();
    segm.x = x;
    segm.y = y;
    segm.size = size;
    s.add(segm);
  }
}

float calcStdDev(Planes planes, int pno, int x, int y, int size) {
  int limit = (int)(max(0.1f*sq(size),4));
  
  float A = 0;
  float Q = 0;
  for(int k=1;k<=limit;k++) {
    int posx = (int)random(size);
    int posy = (int)random(size);
    
    int xk = planes.get(pno,x+posx,y+posy);
    
    float oldA = A;
    A+=(xk-A)/k;
    Q+=(xk-oldA)*(xk-A);
  }
  
  return sqrt(Q/(limit-1));
}

public static void main(String[] args)throws Exception{AdaptiveOracle o=new AdaptiveOracle();System.out.print("[");boolean first=true;for(int[] d:new int[][]{{16,16},{23,13},{64,33}})for(int pattern=0;pattern<5;pattern++)for(int min:new int[]{8,16})for(int max:new int[]{16,64})for(int thr:new int[]{0,20,80,255}){o.cursor=0;for(int c=0;c<3;c++){Planes p=o.new Planes(d[0],d[1],pattern);DefaultBitOutput bits=o.new DefaultBitOutput();ArrayList<Segment> leaves=o.makeSegmentation(bits,p,c,min,max,thr);if(!first)System.out.print(",");first=false;System.out.print("{\"w\":"+d[0]+",\"h\":"+d[1]+",\"pattern\":"+pattern+",\"min\":"+min+",\"max\":"+max+",\"threshold\":"+thr+",\"channel\":"+c+",\"draws\":"+o.cursor+",\"bits\":\""+bits.bits+"\",\"leaves\":[");for(int i=0;i<leaves.size();i++){Segment s=leaves.get(i);if(i>0)System.out.print(",");System.out.print("["+s.x+","+s.y+","+s.size+"]");}System.out.print("]}");}}System.out.print("]");}}
