import {processImage} from './artifice-core.mjs';
import {processAdaptive,ADAPTIVE_ENGINE} from './adaptive.mjs';
self.onmessage=({data:d})=>{try{const start=performance.now(),result=(d.engine===ADAPTIVE_ENGINE?processAdaptive:processImage)(new Uint8ClampedArray(d.pixels),d.width,d.height,d.settings);self.postMessage({id:d.id,...result,ms:performance.now()-start},[result.output.buffer,result.encoded.buffer,result.residual.buffer]);}catch(e){self.postMessage({id:d.id,error:String(e.message||e)});}};
