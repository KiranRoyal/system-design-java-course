import json,os,socket,subprocess,time,urllib.request,urllib.error,contextlib,tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def freeport():
 with socket.socket() as s:s.bind(('127.0.0.1',0));return s.getsockname()[1]
def request(base,path,body=None,key=None,method=None):
 headers={'Content-Type':'application/json'}
 if key:headers['Idempotency-Key']=key
 data=None if body is None else json.dumps(body).encode()
 req=urllib.request.Request(base+path,data=data,headers=headers,method=method or ('GET' if body is None else 'POST'))
 try:
  with urllib.request.urlopen(req,timeout=8) as response:status=response.status;raw=response.read()
 except urllib.error.HTTPError as e:status=e.code;raw=e.read()
 return status,json.loads(raw) if raw else None
@contextlib.contextmanager
def service(module,port,extra=()):
 java=str(Path(os.environ['JAVA_HOME'])/'bin/java') if os.environ.get('JAVA_HOME') else 'java'
 with tempfile.TemporaryFile() as log:
  process=subprocess.Popen([java,'-jar',str(ROOT/module/'target'/f'{module}-1.0.0.jar'),f'--server.port={port}',*extra],stdout=log,stderr=subprocess.STDOUT)
  try:
   base=f'http://127.0.0.1:{port}'
   for _ in range(160):
    if process.poll() is not None:log.seek(0);raise RuntimeError(log.read().decode())
    try:
     if request(base,'/actuator/health')[0]==200:break
    except (OSError,ValueError):pass
    time.sleep(.2)
   else:raise RuntimeError('service did not become healthy')
   yield base
  finally:
   process.terminate()
   try:process.wait(timeout=10)
   except subprocess.TimeoutExpired:process.kill();process.wait()
