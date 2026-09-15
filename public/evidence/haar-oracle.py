import sys,ast,json,gzip
sys.path.insert(0,'/tmp/artifice-wavelet-deps')
import numpy as np,pywt
from pathlib import Path
src=Path('/Users/donovan/work/0000000/Artifice Engine/src/artifice/nodes/transform/wavelet.py').read_text()
names={'_wavelet_transform_2d','_inverse_wavelet_2d','_compress_dwt'}
ns={'np':np,'pywt':pywt,'Any':object,'NDArray':np.ndarray}
for n in ast.parse(src).body:
 if isinstance(n,ast.FunctionDef) and n.name in names: exec('from __future__ import annotations\n'+ast.get_source_segment(src,n),ns)
rows=[];err=0
for w,h in [(1,1),(1,17),(17,1),(7,9),(32,32)]:
 for pattern in range(5):
  y,x=np.indices((h,w));a=([np.full((h,w),128),((x+y)%2)*255,(x*37+y*71+x*y)%256,(x==w//2)*255,((x==w//2)&(y==h//2))*255][pattern]).astype(np.float64)
  coeff=ns['_wavelet_transform_2d'](a,'haar','fwt',1);canonical=[np.rint(coeff[0]*2)/2,tuple(np.rint(c*2)/2 for c in coeff[1])]
  err=max(err,max(float(np.max(np.abs(v-r))) for v,r in zip([coeff[0],*coeff[1]],[canonical[0],*canonical[1]])))
  for threshold in [0,1,32,128,255]:
   filtered=ns['_compress_dwt'](canonical,threshold,True);result=ns['_inverse_wavelet_2d'](filtered,'haar','fwt')[:h,:w]
   result=np.rint(result*4)/4
   rows.append(dict(w=w,h=h,input=a.ravel().tolist(),threshold=threshold,coeff=[c.ravel().tolist() for c in [canonical[0],*canonical[1]]],output=np.clip(np.floor(result+.5),0,255).astype(int).ravel().tolist()))
Path('public/evidence/haar-fixtures.json.gz').write_bytes(gzip.compress(json.dumps(rows).encode(),mtime=0));print(json.dumps(dict(cases=len(rows),pywavelets=pywt.__version__,coefficientCanonicalizationMaxError=err)))
