"""Real local-provider acceptance evidence. Credentials supplied through environment.

Run against disposable QA accounts only. Writes synthetic uploads/results to an
ignored evidence directory; never treats HTTP success as semantic acceptance.
Requires requests. Existing PDF/DOCX fixture paths are optional CLI arguments.
"""
import json
import os
import sys
import time
from pathlib import Path

import requests

BASE = os.environ.get('QA_API_BASE', 'http://localhost:8080/api/v1')
OUT = Path(os.environ.get('QA_OUTPUT_DIR', 'runtime-logs/st-qa/final-product'))
OUT.mkdir(parents=True, exist_ok=True)
JOB = os.environ['QA_JOB_ID']


def session(email, password_env='QA_PASSWORD'):
    client = requests.Session()
    response = client.post(BASE + '/auth/login', json={'email': email, 'password': os.environ[password_env]}, timeout=30)
    response.raise_for_status()
    client.headers['Authorization'] = 'Bearer ' + response.json()['data']['accessToken']
    return client


def call(client, name, method, path, **kwargs):
    start = time.monotonic()
    try:
        response = client.request(method, BASE + path, timeout=240, **kwargs)
        try:
            body = response.json()
        except ValueError:
            body = {'text': response.text}
        record = {'case': name, 'http': response.status_code, 'elapsedMs': round((time.monotonic()-start)*1000), 'body': body}
    except requests.RequestException as error:
        record = {'case': name, 'elapsedMs': round((time.monotonic()-start)*1000), 'error': str(error)}
    with (OUT / 'ai-results.jsonl').open('a', encoding='utf-8') as stream:
        stream.write(json.dumps(record, ensure_ascii=False) + '\n')
    print(json.dumps({k:v for k,v in record.items() if k != 'body'}), flush=True)
    return record.get('body', {}).get('data') if record.get('http', 500) < 300 else None


def queued(client, name, match_id, feature):
    start = time.monotonic()
    path = f'/ai/matching/{match_id}/{feature}'
    task = call(client, name+'-queue', 'POST', path+'/tasks')
    if not task:
        return
    call(client, name+'-duplicate', 'POST', path+'/tasks')
    polls = 0
    while time.monotonic()-start < 240:
        time.sleep(3)
        polls += 1
        state = call(client, name+f'-poll-{polls}', 'GET', '/ai/tasks/'+task['id'])
        if not state or state['status'] not in ('PENDING', 'RUNNING'):
            break
    call(client, name+'-result', 'GET', path)


FIXTURES = {
    'student-vi': 'Nguyễn Minh An\nSinh viên năm cuối Công nghệ thông tin, Đại học Mẫu, 2022-2026.\nChưa có kinh nghiệm làm việc toàn thời gian.\nKỹ năng: Java cơ bản, HTML, CSS, Git.\nDự án học tập: ứng dụng quản lý thư viện bằng Java, làm giao diện và chức năng mượn sách.\nMong muốn thực tập Java Backend. Không có chứng chỉ.',
    'java-three-years': 'Trần Quốc Bình\nJava Backend Developer\nKinh nghiệm: 3 năm, 07/2023-07/2026, Công ty Mẫu.\nPhát triển REST API bằng Java, Spring Boot, PostgreSQL; viết unit test JUnit; quản lý mã bằng Git.\nDự án: hệ thống quản lý đơn hàng, phụ trách API tạo đơn và kiểm tra dữ liệu.\nHọc vấn: Cử nhân CNTT 2019-2023.\nChưa làm việc với AWS, Kafka hoặc Kubernetes. Không có chứng chỉ.',
    'frontend-en': 'Alex Morgan\nFrontend Developer\nExperience: 2 years, July 2024-July 2026 at Sample Studio.\nSkills: JavaScript, TypeScript, React, HTML, CSS, Git.\nProject: accessible booking interface; implemented responsive forms and keyboard navigation.\nEducation: Bachelor of Computer Science, 2020-2024.\nNo Java backend experience. No cloud certifications.',
    'missing-vi': 'Lê Hà\nEmail: qa.sparse@example.test\nMục tiêu: tìm cơ hội học nghề.\nChưa cung cấp thông tin học vấn, kỹ năng, dự án hoặc số năm kinh nghiệm.',
    'english-student': 'Sam Taylor\nFinal-year computer science student, 2022-2026.\nSkills: Python basics, SQL, Git.\nCourse project: CSV expense tracker implemented in Python.\nNo professional employment experience. No certificates. Seeking a data internship.',
}


def main():
    candidate = session(os.environ['QA_CANDIDATE_EMAIL'], 'QA_CANDIDATE_PASSWORD')
    cases = []
    for name, text in FIXTURES.items():
        file = OUT / (name+'.txt')
        file.write_text(text, encoding='utf-8')
        cases.append((name,file))
    cases += [(Path(p).stem, Path(p)) for p in sys.argv[1:]]
    selected_uploads = {name for name in os.environ.get('QA_UPLOAD_CASES', '').split(',') if name}
    if selected_uploads:
        cases = [(name, file) for name, file in cases if name in selected_uploads]
    ids = json.loads((OUT / 'ai-ids.json').read_text()) if os.environ.get('QA_RESUME_FEATURES') else {}
    for name in filter(None, os.environ.get('QA_REANALYZE', '').split(',')):
        if name in ids:
            call(candidate, name+'-analysis-recheck', 'POST', '/ai/resumes/'+ids[name]['resume']+'/analyze')
            call(candidate, name+'-match-recheck', 'POST', f"/ai/matching/jobs/{JOB}/resumes/{ids[name]['resume']}")
    for name, file in ([] if os.environ.get('QA_RESUME_FEATURES') else cases):
        with file.open('rb') as stream:
            resume = call(candidate, name+'-upload', 'POST', '/ai/resumes/upload', files={'file': (file.name,stream)})
        if not resume:
            continue
        analysis = call(candidate, name+'-analysis', 'POST', f"/ai/resumes/{resume['id']}/analyze")
        if not analysis:
            continue
        match = call(candidate, name+'-match', 'POST', f"/ai/matching/jobs/{JOB}/resumes/{resume['id']}")
        ids[name] = {'resume': resume['id'], 'match': match['id'] if match else None}
        (OUT / 'ai-ids.json').write_text(json.dumps(ids,indent=2), encoding='utf-8')
        call(candidate, name+'-recommendations', 'GET', '/ai/recommendations/jobs', params={'resumeId':resume['id']})
    for name in os.environ.get('QA_FEATURE_CASES', 'student-vi,java-three-years,frontend-en,missing-vi').split(','):
        if name not in ids or not ids[name]['match']:
            continue
        data = ids[name]
        for feature in ('explanation', 'interview'):
            queued(candidate, name+'-'+feature, data['match'], feature)
        tasks = ('CAREER_ROADMAP','LEARNING_ROADMAP','SKILL_ROADMAP',
                 'CERTIFICATE_RECOMMENDATION','PORTFOLIO_RECOMMENDATION',
                 'JOB_SEARCH_ADVICE','RESUME_IMPROVEMENT')
        # Exercise every candidate contract on one fact-rich fixture. The other
        # fixtures retain the two gap-sensitive roadmaps to bound CPU runtime.
        selected_tasks = tasks if name == 'java-three-years' else ('CAREER_ROADMAP','SKILL_ROADMAP')
        for task in selected_tasks:
            call(candidate, name+'-'+task, 'POST', '/ai/assistant/candidate', json={'task':task,'resumeId':data['resume'],'matchId':data['match']})
    if 'java-three-years' in ids:
        call(candidate, 'candidate-chat', 'POST', '/ai/career/chat', json={'message':'Tôi nên chuẩn bị gì cho bước phát triển tiếp theo? Chỉ dùng thông tin trong CV.', 'resumeId':ids['java-three-years']['resume'], 'jobId':JOB})
    if not os.environ.get('QA_SKIP_RECRUITER'):
        employer = session(os.environ['QA_EMPLOYER_EMAIL'], 'QA_EMPLOYER_PASSWORD')
        recruiter_job = os.environ.get('QA_RECRUITER_JOB_ID', JOB)
        call(employer, 'recruiter-summary', 'POST', '/ai/assistant/recruiter', json={'task':'SUMMARIZE_JOB','jobId':recruiter_job})


if __name__ == '__main__':
    main()
