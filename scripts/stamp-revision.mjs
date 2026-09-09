import {readdir,readFile,writeFile} from 'node:fs/promises';
import {createHash} from 'node:crypto';
const hash=createHash('sha256');
async function visit(dir){for(const entry of (await readdir(dir,{withFileTypes:true})).sort((a,b)=>a.name.localeCompare(b.name))){const path=`${dir}/${entry.name}`;if(path==='app/revision.json')continue;if(entry.isDirectory())await visit(path);else{hash.update(path);hash.update(await readFile(path));}}}
for(const dir of ['app','lib','public'])await visit(dir);
await writeFile('app/revision.json',JSON.stringify({revision:hash.digest('hex')}));
