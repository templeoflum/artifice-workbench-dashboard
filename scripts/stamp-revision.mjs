import {ENGINE} from '../lib/artifice-core.mjs';
import {readdir,readFile,writeFile,mkdir,copyFile} from 'node:fs/promises';
import {createHash} from 'node:crypto';
await mkdir('public/engine/'+ENGINE,{recursive:true});
for(const f of ['artifice-core.mjs','artifice-worker.mjs','adaptive.mjs','adaptive-worker.mjs','colorspace.mjs'])await copyFile('lib/'+f,'public/engine/'+ENGINE+'/'+f);
const hash=createHash('sha256');
async function visit(dir){for(const entry of (await readdir(dir,{withFileTypes:true})).sort((a,b)=>a.name.localeCompare(b.name))){const path=`${dir}/${entry.name}`;if(path==='app/revision.json')continue;if(entry.isDirectory())await visit(path);else{hash.update(path);hash.update(await readFile(path));}}}
for(const dir of ['app','lib','public'])await visit(dir);
await writeFile('app/revision.json',JSON.stringify({revision:hash.digest('hex')}));
