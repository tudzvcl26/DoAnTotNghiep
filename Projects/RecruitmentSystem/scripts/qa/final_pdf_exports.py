"""Export synthetic long Unicode CVs through the real backend for PDF visual QA.

Requires QA_PASSWORD, QA_CANDIDATE_EMAIL, QA_SOURCE_CV_ID. Retains originals.
"""
import copy
import json
import os
from pathlib import Path
import requests

base = os.environ.get('QA_API_BASE', 'http://localhost:8080/api/v1')
out = Path(os.environ.get('QA_OUTPUT_DIR', 'runtime-logs/st-qa/final-product'))
out.mkdir(parents=True, exist_ok=True)
client = requests.Session()
auth = client.post(base+'/auth/login', json={'email':os.environ['QA_CANDIDATE_EMAIL'],'password':os.environ['QA_PASSWORD']}, timeout=30)
auth.raise_for_status()
client.headers['Authorization'] = 'Bearer '+auth.json()['data']['accessToken']
source = client.get(base+'/cvs/'+os.environ['QA_SOURCE_CV_ID'], timeout=30)
source.raise_for_status()
source = source.json()['data']
results = []
for template, layout in [('classic','single'),('professional','header'),('developer','sidebar-left'),('modern-professional','sidebar-right')]:
    content = copy.deepcopy(source['content'])
    content['designConfig']['layout'] = layout
    content['personalInfo']['fullName'] = 'Nguyễn Đình Tuấn Tú'
    content['experiences'][0]['description'] = '\n'.join(f'QA-{n:02d}: Phát triển API bằng Java, kiểm thử dữ liệu tiếng Việt, ghi tài liệu và đánh giá kết quả thực hành.' for n in range(1,37))
    payload = {'title':'Final QA '+layout,'templateId':template,'language':'vi','content':content}
    saved = client.post(base+'/cvs', json=payload, timeout=30)
    saved.raise_for_status()
    cv = saved.json()['data']
    pdf = client.get(base+'/cvs/'+cv['id']+'/pdf', timeout=60)
    pdf.raise_for_status()
    (out/(layout+'.pdf')).write_bytes(pdf.content)
    results.append({'template':template,'layout':layout,'cvId':cv['id'],'bytes':len(pdf.content),'http':pdf.status_code})
    print(results[-1],flush=True)
(out/'pdf-exports.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
