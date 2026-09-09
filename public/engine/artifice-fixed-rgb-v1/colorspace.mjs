// GLIC YCbCr conversion, preserving Processing Float32 arithmetic and truncation.
const f=Math.fround,m=(a,b)=>f(f(a)*b),add=(a,b)=>f(a+b),sub=(a,b)=>f(a-b),byte=v=>Math.max(0,Math.min(255,Math.trunc(v)));
export function toYCbCr(r,g,b){return [byte(add(add(m(.298839,r),m(.586811,g)),m(.114350,b))),byte(add(add(sub(m(-.168736,r),m(.331264,g)),m(.5,b)),127.5)),byte(add(sub(sub(m(.5,r),m(.418688,g)),m(.081312,b)),127.5))];}
export function fromYCbCr(y,cb,cr){cb=f(cb-127.5);cr=f(cr-127.5);return [byte(Math.trunc(add(y,m(1.402,cr)))+1),byte(sub(sub(y,m(.344136,cb)),m(.714136,cr))),byte(Math.trunc(add(y,m(1.772,cb)))+1)];}
export function convertPixels(input,inverse=false){const out=new Uint8ClampedArray(input),fn=inverse?fromYCbCr:toYCbCr;for(let i=0;i<input.length;i+=4){const p=fn(input[i],input[i+1],input[i+2]);out[i]=p[0];out[i+1]=p[1];out[i+2]=p[2];}return out;}
