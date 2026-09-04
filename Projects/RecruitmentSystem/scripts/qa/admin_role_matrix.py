"""Read-only live Admin API guards; does not provision or elevate accounts."""
import json
import os
from pathlib import Path
import requests

base = os.environ.get('QA_API_BASE','http://localhost:8080/api/v1')
paths = ['/admin/users','/admin/companies','/admin/jobs','/admin/applications','/notification-templates','/admin/notification-delivery-logs','/ai/providers']
results=[]
actors={'anonymous':None,'candidate':os.environ['QA_CANDIDATE_EMAIL'],'employer':os.environ['QA_EMPLOYER_EMAIL']}
if os.environ.get('QA_ADMIN_EMAIL'):
    actors['admin']=os.environ['QA_ADMIN_EMAIL']
for role,email in actors.items():
    client=requests.Session()
    if email:
        login=client.post(base+'/auth/login',json={'email':email,'password':os.environ.get('QA_ADMIN_PASSWORD') if role=='admin' else os.environ['QA_PASSWORD']},timeout=30)
        login.raise_for_status()
        client.headers['Authorization']='Bearer '+login.json()['data']['accessToken']
    for path in paths:
        response=client.get(base+path,timeout=30)
        expected=401 if role=='anonymous' else 200 if role=='admin' else 403
        results.append({'actor':role,'path':path,'http':response.status_code,'expected':expected,'pass':response.status_code==expected})
out=Path(os.environ.get('QA_OUTPUT_DIR','runtime-logs/st-qa/final-product'))
out.mkdir(parents=True,exist_ok=True)
(out/'admin-role-matrix.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
print(json.dumps(results,indent=2))
if not all(x['pass'] for x in results):
    raise SystemExit(1)
