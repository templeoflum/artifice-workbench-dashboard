from pathlib import Path
import os,json,hashlib,ast,subprocess,datetime,sys
base=Path('/Users/donovan/work/0000000');out=Path(sys.argv[1]) if len(sys.argv)>1 else Path.cwd()/'intake-output';out.mkdir(parents=True,exist_ok=True)
roots=['Artifice Engine','[archive]/[archive] Artifice Engine v1','[archive]/[archive] Artifice v2 (merged)','Artifice v3']
rows=[]; inventories={};skip={'.git','venv','.venv','__pycache__','.pytest_cache','.claude','node_modules'}
for i,r in enumerate(roots):
 p=base/r; files=[]; errors=[]
 for b,ds,fs in os.walk(p):
  ds[:]=[d for d in ds if d not in skip]
  for f in sorted(fs):
   q=Path(b)/f
   if f=='.DS_Store' or '.egg-info' in str(q):continue
   rel=str(q.relative_to(p)); raw=q.read_bytes();files.append({'path':rel,'bytes':len(raw),'sha256':hashlib.sha256(raw).hexdigest()})
   if q.suffix=='.py':
    try: ast.parse(raw,filename=rel)
    except Exception as e:errors.append({'path':rel,'error':str(e)})
 inv={x['path']:x['sha256'] for x in files};inventories[r]=inv
 def git(*args):
  result=subprocess.run(['git','-C',str(p),*args],capture_output=True,text=True)
  return result.stdout.strip() if result.returncode==0 else None
 row={'id':str(i),'name':p.name,'root':r,'files':len(files),'python':sum(x['path'].endswith('.py') for x in files),'shaders':sum(x['path'].endswith('.glsl') for x in files),'tests':sum(Path(x['path']).name.startswith('test_') and x['path'].endswith('.py') for x in files),'references':sum(x['path'].startswith('reference/') for x in files),'syntaxErrors':errors,'head':git('rev-parse','HEAD'),'commits':git('rev-list','--count','HEAD'),'inventory':f'/evidence/version-{i}.json'}
 rows.append(row);(out/f'version-{i}.json').write_text(json.dumps({'root':str(p),'files':files,'syntaxErrors':errors},indent=2))
pairs=[]
for i,a in enumerate(roots):
 for b in roots[i+1:]:
  aa={k:v for k,v in inventories[a].items() if k.startswith('src/')};bb={k:v for k,v in inventories[b].items() if k.startswith('src/')};common=aa.keys()&bb.keys();pairs.append({'a':a,'b':b,'same':sum(aa[k]==bb[k] for k in common),'changed':sum(aa[k]!=bb[k] for k in common),'onlyA':len(aa.keys()-bb.keys()),'onlyB':len(bb.keys()-aa.keys())})
data={'updatedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'scope':'All Artifice-named project folders found by recursive path search under /Users/donovan/work. Environments, caches, Git internals and generated package metadata excluded from counts. Git histories identified but historical checkouts are not separately audited.','versions':rows,'overlap':pairs}
(out/'intake.json').write_text(json.dumps(data,indent=2));print(json.dumps(data,indent=2))
