from pathlib import Path
import ast,json,hashlib,sys,numpy as np
base=Path('/Users/donovan/work/0000000');results=[]
for root in ['Artifice Engine','[archive]/[archive] Artifice Engine v1']:
 p=base/root/'src/artifice/nodes/quantization/quantize_node.py';tree=ast.parse(p.read_text());nodes=[n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name in ['quantize_value','dequantize_value']];ns={'np':np};exec(compile(ast.Module(body=nodes,type_ignores=[]),str(p),'exec'),ns)
 x=np.array([-1.,-.5,0.,.5,1.])
 with np.errstate(all='ignore'):
  q=ns['quantize_value'](x,1,True);y=ns['dequantize_value'](q,1,True)
  good=ns['dequantize_value'](ns['quantize_value'](x,8,True),8,True)
 results.append({'version':root,'source':'src/artifice/nodes/quantization/quantize_node.py','sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'method':'Executed unmodified quantize_value and dequantize_value function ASTs with NumPy; application imports excluded. Not an end-to-end app test.','input':x.tolist(),'signedOneBitIntegers':q.tolist(),'signedOneBitFinite':bool(np.isfinite(y).all()),'signedOneBitNonfiniteCount':int((~np.isfinite(y)).sum()),'signedEightBitFinite':bool(np.isfinite(good).all()),'signedEightBitMaxError':float(np.max(abs(good-x)))})
out=Path(sys.argv[1]) if len(sys.argv)>1 else Path.cwd()/'checks.json';out.write_text(json.dumps(results,indent=2));print(out.read_text())
