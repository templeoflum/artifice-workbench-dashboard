import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {gunzipSync} from 'node:zlib';
// Explicit sampling tape, not a claim of Processing random-generator parity.
const fixtures=JSON.parse(gunzipSync(readFileSync(new URL('../public/evidence/adaptive-fixtures.json.gz',import.meta.url))));
const f=Math.fround;let cursor=0,totalLeaves=0;
for(const c of fixtures){if(c.channel===0)cursor=0;let bits='',leaves=[];const get=(x,y)=>x>=c.w||y>=c.h?128:c.pattern===0?128:c.pattern===1?(x*7+y*3+c.channel*31)&255:c.pattern===2?((x+y+c.channel)%2)*255:c.pattern===3?(x===Math.floor(c.w/2)?255:0):(x===Math.floor(c.w/2)&&y===Math.floor(c.h/2)?255:0);
function visit(x,y,size){if(x>=c.w||y>=c.h)return;const limit=Math.trunc(Math.max(f(f(.1)*f(size*size)),4));let a=0,q=0;for(let k=1;k<=limit;k++){const px=Math.trunc(((cursor++*73+19)&255)*size/256),py=Math.trunc(((cursor++*73+19)&255)*size/256),v=get(x+px,y+py),old=a;a=f(a+f(f(v-a)/k));q=f(q+f(f(v-old)*f(v-a)));}const deviation=f(Math.sqrt(f(q/(limit-1))));if(size>c.max||(size>c.min&&deviation>c.threshold)){bits+='1';const half=size/2;visit(x,y,half);visit(x+half,y,half);visit(x,y+half,half);visit(x+half,y+half,half);}else{bits+='0';leaves.push([x,y,size]);}}
let size=1;while(size<Math.max(c.w,c.h))size*=2;visit(0,0,size);assert.equal(bits,c.bits);assert.deepEqual(leaves,c.leaves);assert.equal(cursor,c.draws);const covered=new Uint8Array(c.w*c.h);for(const [x,y,n] of leaves)for(let yy=y;yy<Math.min(y+n,c.h);yy++)for(let xx=x;xx<Math.min(x+n,c.w);xx++)covered[yy*c.w+xx]++;assert(covered.every(v=>v===1));totalLeaves+=leaves.length;
}
console.log(JSON.stringify({cases:fixtures.length,totalLeaves,splitBitsExact:true,leafOrderExact:true,sampleConsumptionExact:true,singlePixelCoverage:true,scope:'Explicit sample tape, not seeded Processing RNG or reconstructed image parity'}));
