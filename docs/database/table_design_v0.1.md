# AI 소송 도우미 테이블 설계 v0.1

## 1. 설계 범위

본 문서는 AI 소송 도우미 v1.0-lite에서 사용할 데이터베이스 테이블의 초안을 정의한다.

데이터베이스는 PostgreSQL을 사용한다.

---

## 2. 테이블 목록

| 테이블명 | 설명 |
|---|---|
| members | 회원 정보 |
| legal_cases | 사용자가 등록한 소송 사건 |
| documents | 사건에 업로드한 법률문서 |
| questions | 일반 또는 사건·문서 기반 질문 |
| question_documents | 질문과 참조 문서의 연결 |
| answers | AI가 생성한 답변 |
| sources | 답변에 사용한 법령·판례·문서 출처 |

---

## 3. 테이블 관계

- members 1 : N legal_cases
- members 1 : N questions
- legal_cases 1 : N documents
- legal_cases 1 : N questions
- questions N : M documents
- questions 1 : N answers
- answers 1 : N sources

### 관계 구조

회원 한 명은 여러 사건을 등록할 수 있다.

하나의 사건에는 여러 문서와 질문이 연결될 수 있다.

하나의 질문은 여러 문서를 참조할 수 있다.

하나의 질문에는 답변을 다시 생성할 수 있으므로 여러 답변이 저장될 수 있다.

하나의 답변에는 여러 법령, 판례 또는 문서 출처가 연결될 수 있다.

---

## 4. members 테이블

회원 정보를 저장한다.

| 컬럼명 | 타입 | 필수 | 제약조건·설명 |
|---|---|---:|---|
| id | BIGINT | O | 기본키, 자동 증가 |
| email | VARCHAR(255) | O | 이메일, 중복 불가 |
| password_hash | VARCHAR(255) | O | 암호화된 비밀번호 |
| nickname | VARCHAR(50) | O | 사용자 닉네임 |
| role | VARCHAR(20) | O | 기본값 USER |
| created_at | TIMESTAMPTZ | O | 가입일시 |
| updated_at | TIMESTAMPTZ | O | 수정일시 |

### 제약조건

- email은 UNIQUE로 설정한다.
- 비밀번호 원문은 저장하지 않는다.
- password_hash에는 암호화된 값만 저장한다.

---

## 5. legal_cases 테이블

사용자가 등록한 사건 정보를 저장한다.

| 컬럼명 | 타입 | 필수 | 제약조건·설명 |
|---|---|---:|---|
| id | BIGINT | O | 기본키, 자동 증가 |
| member_id | BIGINT | O | members.id 외래키 |
| case_name | VARCHAR(200) | O | 사용자가 정한 사건명 |
| case_type | VARCHAR(50) | O | 민사, 형사, 가사 등 |
| status | VARCHAR(30) | O | 사건 진행 상태 |
| opponent_name | VARCHAR(100) | X | 상대방 이름 또는 명칭 |
| description | TEXT | X | 사건 개요 |
| memo | TEXT | X | 사용자 메모 |
| created_at | TIMESTAMPTZ | O | 생성일시 |
| updated_at | TIMESTAMPTZ | O | 수정일시 |

### 사건 상태 예시

- PREPARING
- IN_PROGRESS
- JUDGMENT
- CLOSED

---

## 6. documents 테이블

사건에 업로드한 문서와 텍스트 추출 결과를 저장한다.

| 컬럼명 | 타입 | 필수 | 제약조건·설명 |
|---|---|---:|---|
| id | BIGINT | O | 기본키, 자동 증가 |
| case_id | BIGINT | O | legal_cases.id 외래키 |
| original_filename | VARCHAR(255) | O | 사용자가 업로드한 원래 파일명 |
| stored_filename | VARCHAR(255) | O | 서버에 저장된 파일명 |
| document_type | VARCHAR(50) | O | 소장, 답변서, 판결문 등 |
| content_type | VARCHAR(100) | O | 파일 MIME 타입 |
| file_size | BIGINT | O | 파일 크기 |
| storage_path | TEXT | O | 파일 저장 위치 |
| extracted_text | TEXT | X | 추출한 문서 본문 |
| processing_status | VARCHAR(30) | O | 문서 처리 상태 |
| error_message | TEXT | X | 처리 실패 사유 |
| created_at | TIMESTAMPTZ | O | 업로드일시 |
| updated_at | TIMESTAMPTZ | O | 수정일시 |

### 문서 처리 상태 예시

- UPLOADED
- EXTRACTING
- COMPLETED
- FAILED

---

## 7. questions 테이블

사용자가 입력한 질문을 저장한다.

| 컬럼명 | 타입 | 필수 | 제약조건·설명 |
|---|---|---:|---|
| id | BIGINT | O | 기본키, 자동 증가 |
| member_id | BIGINT | O | members.id 외래키 |
| case_id | BIGINT | X | legal_cases.id 외래키 |
| question_type | VARCHAR(30) | O | 일반 질문 또는 사건 기반 질문 |
| content | TEXT | O | 질문 내용 |
| created_at | TIMESTAMPTZ | O | 질문일시 |

### 질문 유형 예시

- GENERAL
- CASE_BASED
- DOCUMENT_BASED

### 참고

일반 AI 상담은 특정 사건과 연결되지 않을 수 있으므로 case_id는 NULL을 허용한다.

---

## 8. question_documents 테이블

질문에서 참조한 문서를 연결한다.

| 컬럼명 | 타입 | 필수 | 제약조건·설명 |
|---|---|---:|---|
| question_id | BIGINT | O | questions.id 외래키 |
| document_id | BIGINT | O | documents.id 외래키 |

### 기본키

question_id와 document_id를 합친 복합 기본키를 사용한다.

### 참고

한 질문이 여러 문서를 참고할 수 있고, 한 문서도 여러 질문에서 사용될 수 있다.

---

## 9. answers 테이블

AI가 생성한 답변을 저장한다.

| 컬럼명 | 타입 | 필수 | 제약조건·설명 |
|---|---|---:|---|
| id | BIGINT | O | 기본키, 자동 증가 |
| question_id | BIGINT | O | questions.id 외래키 |
| content | TEXT | O | AI 답변 |
| model_name | VARCHAR(100) | X | 사용한 LLM 모델 |
| verification_status | VARCHAR(30) | O | 답변 검증 상태 |
| created_at | TIMESTAMPTZ | O | 답변 생성일시 |

### 검증 상태 예시

- VERIFIED
- REFERENCE_ONLY
- UNVERIFIED
- NOT_FOUND

---

## 10. sources 테이블

AI 답변에 사용한 출처를 저장한다.

| 컬럼명 | 타입 | 필수 | 제약조건·설명 |
|---|---|---:|---|
| id | BIGINT | O | 기본키, 자동 증가 |
| answer_id | BIGINT | O | answers.id 외래키 |
| document_id | BIGINT | X | 참조한 documents.id |
| source_type | VARCHAR(30) | O | 출처 유형 |
| title | VARCHAR(500) | X | 법령명, 판례명 또는 문서명 |
| reference_number | VARCHAR(255) | X | 조문 또는 판례번호 |
| citation_text | TEXT | X | 답변 근거가 된 내용 |
| source_url | TEXT | X | 외부 출처 주소 |
| page_number | INTEGER | X | 문서 페이지 번호 |
| verification_status | VARCHAR(30) | O | 출처 확인 상태 |
| created_at | TIMESTAMPTZ | O | 저장일시 |

### 출처 유형 예시

- LAW
- PRECEDENT
- DOCUMENT
- OTHER

---

## 11. 외래키 및 삭제 규칙

- members 삭제 시 연결된 legal_cases와 questions를 삭제한다.
- legal_cases 삭제 시 연결된 documents와 questions를 삭제한다.
- questions 삭제 시 연결된 question_documents와 answers를 삭제한다.
- answers 삭제 시 연결된 sources를 삭제한다.
- documents 삭제 시 실제 저장된 파일도 함께 삭제한다.

초기 MVP에서는 ON DELETE CASCADE를 기본으로 사용한다.

---

## 12. 인덱스

조회 성능을 위해 다음 컬럼에 인덱스를 생성한다.

- members.email
- legal_cases.member_id
- documents.case_id
- documents.processing_status
- questions.member_id
- questions.case_id
- questions.created_at
- answers.question_id
- sources.answer_id
- question_documents.document_id

---

## 13. 추후 검토사항

다음 항목은 개발 과정에서 검토한다.

- 회원 탈퇴 시 즉시 삭제 또는 일정 기간 보관 여부
- 사건과 문서의 소프트 삭제 적용 여부
- AI 답변 버전 관리 방식
- 업로드 문서 보관 기간
- 개인정보 마스킹 결과 저장 방식
- RAG 도입 시 임베딩 테이블 또는 pgvector 추가