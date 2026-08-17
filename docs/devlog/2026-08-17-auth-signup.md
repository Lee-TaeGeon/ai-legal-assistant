# 2026-08-17 회원가입 구현

## 구현한 기능

- members 테이블 생성
- Member 엔티티 구현
- MemberRepository 구현
- SignupRequest DTO 구현
- AuthService 구현
- AuthController 구현
- POST /api/auth/signup 구현

## 회원가입 처리 흐름

Postman
→ AuthController
→ AuthService
→ MemberRepository
→ PostgreSQL

## 비밀번호

- BCrypt 사용
- 평문 비밀번호를 DB에 저장하지 않음
- password_hash 컬럼에 해시 저장

## 예외 처리

- 중복 이메일: 409 Conflict
- 입력값 검증 실패: 400 Bad Request

## 테스트 결과

- 정상 회원가입: 성공
- DB 저장: 성공
- BCrypt 해시 저장: 성공
- 중복 이메일 차단: 성공
- 잘못된 이메일 검증: 성공
- 8자 미만 비밀번호 검증: 성공
- 빈 닉네임 검증: 성공

## 개발 중 발생한 문제

### Lombok builder() 인식 오류

원인:
Eclipse에 Lombok IDE 지원이 설치되어 있지 않았음.

해결:

- Lombok 1.18.46 Eclipse 설치
- Eclipse 재실행
- Project → Clean
- Member.builder() 정상 인식 확인

## 다음 작업

- 로그인 API 구현
- BCrypt 비밀번호 비교
- 인증 방식 구현
