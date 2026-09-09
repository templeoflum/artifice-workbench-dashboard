import assert from 'node:assert/strict';import fs from 'node:fs';import {createHash} from 'node:crypto';import {frameSettings,validateMotion,zipStored} from '../lib/motion.mjs';import {processImage,ENGINE,diagnostic} from '../lib/artifice-core.mjs';import {encodePNG} from '../lib/png.mjs';
const a={block:16,predictors:[0,0,0],quant:[0,160,255]},b={block:16,predictors:[0,0,0],quant:[255,0,32]};let framesChecked=0;for(const n of [2,12,24,120]){assert.deepEqual(frameSettings(a,b,0,n),a);assert.deepEqual(frameSettings(a,b,n-1,n),b);for(let i=0;i<n;i++){const s=frameSettings(a,b,i,n);assert(s.quant.every(v=>Number.isInteger(v)&&v>=0&&v<=255));framesChecked++}}
assert.throws(()=>frameSettings(a,{...b,block:8},0,12));assert.throws(()=>frameSettings(a,{...b,predictors:[2,0,0]},0,12));for(const n of [1,121,NaN])assert.throws(()=>frameSettings(a,b,0,n));assert.throws(()=>validateMotion({format:'artifice-motion-v1',engine:ENGINE,inputHash:'wrong'},'expected'));
const p=diagnostic(),hash=x=>createHash('sha256').update(x).digest('hex'),start={...a,quant:[0,0,0]},end={...b,quant:[96,160,224]},items=[['Calm',start],['Stepped',end],['Incompatible',{...end,predictors:[2,0,0]}]].map(([name,settings])=>({name,settings,outputHash:hash(processImage(p,512,512,settings).output)}));fs.writeFileSync('/tmp/motion-sheet.json',JSON.stringify({format:'artifice-sheet-v1',engine:ENGINE,width:512,height:512,inputHash:hash(p),pixels:Buffer.from(p).toString('base64'),items}));
const png=await encodePNG(p,512,512);fs.writeFileSync('/tmp/motion-zip-unit.zip',Buffer.from(await zipStored([{name:'frame-0001.png',bytes:new Uint8Array(await png.arrayBuffer())},{name:'motion.json',bytes:new TextEncoder().encode('{}')}]).arrayBuffer()));console.log(JSON.stringify({framesChecked,endpointSettingsExact:true,incompatibleRejected:true,boundsRejected:true}));
const {frameOrder}=await import('../lib/motion.mjs');
assert.deepEqual(frameOrder(4,true,'pingpong'),[0,1,2,3,2,1]);
assert.deepEqual(frameOrder(2,true,'pingpong'),[0,1]);
assert.deepEqual(frameOrder(4,false,'pingpong'),[0,1,2,3]);
assert.deepEqual(frameOrder(4,true,'repeat'),[0,1,2,3]);
const old={format:'artifice-motion-v1',engine:ENGINE,inputHash:'test',start:a,end:b,frames:12,fps:12,loop:true};
assert.equal(validateMotion(old,'test').loopMode,'repeat');
assert.equal(validateMotion({...old,loopMode:'pingpong'},'test').loopMode,'pingpong');
assert.throws(()=>validateMotion({...old,loopMode:'unknown'},'test'));
for(const n of [2,12,120]){const order=frameOrder(n,true,'pingpong');assert.equal(order.length,2*n-2);for(let k=0;k<order.length;k++)assert.equal(Math.abs(order[k]-order[(k+1)%order.length]),1);}
console.log('Ping-pong order, cycle boundaries and legacy motion imports pass.');
