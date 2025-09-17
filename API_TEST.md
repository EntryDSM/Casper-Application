# Casper Application API 테스트 가이드

## 서버 실행
```bash
./gradlew :casper-application-infrastructure:bootRun
```

## API 테스트 시나리오

### 1. 수식 집합 생성 (FormulaSet 등록)

#### 일반전형 졸업예정자 수식 등록
```bash
curl -X POST http://localhost:8080/api/v1/formula-sets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "일반전형_졸업예정자_대전",
    "description": "일반전형 졸업예정자 대전지역 점수 계산",
    "type": "GRADE_CALCULATION",
    "formulas": [
      {
        "name": "3학년_1학기_평균",
        "expression": "(korean_3_1_grade + math_3_1_grade + english_3_1_grade + science_3_1_grade + social_3_1_grade + history_3_1_grade + tech_3_1_grade) / 7",
        "order": 1,
        "resultVariable": "semester_3_1_avg"
      },
      {
        "name": "3학년_2학기_평균", 
        "expression": "(korean_3_2_grade + math_3_2_grade + english_3_2_grade + science_3_2_grade + social_3_2_grade + history_3_2_grade + tech_3_2_grade) / 7",
        "order": 2,
        "resultVariable": "semester_3_2_avg"
      },
      {
        "name": "교과점수_계산",
        "expression": "(semester_3_1_avg * 0.6 + semester_3_2_avg * 0.4) * 1.8 + attendance_score + volunteer_score",
        "order": 3,
        "resultVariable": "final_score"
      }
    ]
  }'
```

**예상 응답:**
```json
{
  "id": "formula-set-001",
  "name": "일반전형_졸업예정자_대전",
  "type": "GRADE_CALCULATION",
  "description": "일반전형 졸업예정자 대전지역 점수 계산",
  "formulas": [
    {
      "id": "formula-001",
      "name": "3학년_1학기_평균",
      "expression": "(korean_3_1_grade + math_3_1_grade + english_3_1_grade + science_3_1_grade + social_3_1_grade + history_3_1_grade + tech_3_1_grade) / 7",
      "order": 1,
      "resultVariable": "semester_3_1_avg"
    }
  ],
  "isActive": true
}
```

#### 검정고시 수식 등록
```bash
curl -X POST http://localhost:8080/api/v1/formula-sets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "일반전형_검정고시_전국",
    "description": "일반전형 검정고시 전국지역 점수 계산", 
    "type": "COMMON_QUALIFICATION",
    "formulas": [
      {
        "name": "검정고시_평균",
        "expression": "(qualification_korean + qualification_math + qualification_english + qualification_science + qualification_social + qualification_optional) / 6",
        "order": 1,
        "resultVariable": "qualification_avg"
      },
      {
        "name": "최종점수_계산",
        "expression": "qualification_avg * 2.0 + attendance_score + volunteer_score + extra_score",
        "order": 2,
        "resultVariable": "final_score"
      }
    ]
  }'
```

### 2. 사용자 생성

```bash
curl -X POST http://localhost:8080/api/applications/users \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01012345678",
    "name": "홍길동",
    "isParent": false
  }'
```

**예상 응답:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "phoneNumber": "01012345678",
  "name": "홍길동",
  "isParent": false
}
```

### 3. 통합 원서 작성 (NEW!) - 원서 + 성적 + 자동계산 한번에

**주의:** 사용자가 미리 생성되어 있어야 합니다. 2번 API로 사용자를 먼저 생성하세요.

#### 일반전형 졸업예정자 (학기별 성적 포함)
```bash
curl -X POST http://localhost:8080/api/applications/user/550e8400-e29b-41d4-a716-446655440000/complete \
  -H "Content-Type: application/json" \
  -d '{
    "applicantName": "홍길동",
    "applicantTel": "01012345678", 
    "parentName": "홍부모",
    "parentTel": "01098765432",
    "sex": "MALE",
    "birthDate": "2006-03-15",
    "streetAddress": "대전광역시 유성구 가정로 76",
    "postalCode": "34111",
    "detailAddress": "101동 501호",
    "isDaejeon": true,
    "applicationType": "COMMON",
    "applicationRemark": "NOTHING",
    "educationalStatus": "PROSPECTIVE_GRADUATE",
    "attendanceScore": 28,
    "volunteerScore": 25.5,
    "korean_3_1": "AABB",
    "social_3_1": "ABBC", 
    "history_3_1": "AAAB",
    "math_3_1": "BBCC",
    "science_3_1": "ABCC",
    "english_3_1": "AABC",
    "techAndHome_3_1": "ABCC",
    "koreanGrade": "AABB",
    "socialGrade": "ABBC",
    "historyGrade": "AAAB", 
    "mathGrade": "BBCC",
    "scienceGrade": "ABCC",
    "englishGrade": "AABC",
    "techAndHomeGrade": "ABCC",
    "korean_2_2": "BBCC",
    "social_2_2": "ABCC",
    "history_2_2": "BBCC",
    "math_2_2": "CCDD",
    "science_2_2": "BCCD",
    "english_2_2": "ABCD",
    "techAndHome_2_2": "BCDD",
    "thirdGradeScore": 85.5,
    "thirdBeforeScore": 82.0,
    "thirdScore": 87.0,
    "extraScore": 3.0
  }'
```

**예상 응답:**
```json
{
  "user": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "phoneNumber": "01012345678",
    "name": "홍길동",
    "isParent": false
  },
  "application": {
    "receiptCode": 24001,
    "applicantName": "홍길동",
    "applicationType": "COMMON",
    "educationalStatus": "PROSPECTIVE_GRADUATE",
    "isDaejeon": true,
    "isSubmitted": true
  },
  "score": {
    "receiptCode": 24001,
    "attendanceScore": 28,
    "volunteerScore": 25.5,
    "totalScore": 145.0,
    "extraScore": 3.0
  },
  "calculation": {
    "finalScore": 145.0,
    "detailScores": {
      "semester_3_1_avg": 3.71,
      "semester_3_2_avg": 3.86,
      "final_score": 145.0
    },
    "executionId": "exec_550e8400-e29b-41d4-a716-446655440001",
    "variableMapping": {
      "korean_3_1_grade": "24001_common_prosp_dj_3_1_korean",
      "attendance_score": "24001_common_prosp_dj_attendance"
    },
    "status": "SUCCESS"
  },
  "status": "COMPLETED",
  "processedAt": "2024-03-15T14:30:00"
}
```

#### 검정고시 통합 신청
**먼저 검정고시 사용자를 생성:**
```bash
curl -X POST http://localhost:8080/api/applications/users \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01011111111",
    "name": "김철수",
    "isParent": false
  }'
```

**검정고시 통합 원서:**
```bash
curl -X POST http://localhost:8080/api/applications/user/550e8400-e29b-41d4-a716-446655440001/complete \
  -H "Content-Type: application/json" \
  -d '{
    "applicantName": "김철수",
    "applicantTel": "01011111111",
    "parentName": "김부모",
    "parentTel": "01022222222",
    "sex": "MALE",
    "birthDate": "2005-08-20",
    "streetAddress": "충청남도 천안시 동남구 병천면",
    "postalCode": "31020",
    "detailAddress": "독립기념관길 1",
    "isDaejeon": false,
    "applicationType": "COMMON",
    "applicationRemark": "BASIC_LIVING",
    "educationalStatus": "QUALIFICATION_EXAM",
    "attendanceScore": 30,
    "volunteerScore": 30.0,
    "qualificationKorean": 85.0,
    "qualificationSocial": 78.0,
    "qualificationMath": 92.0,
    "qualificationScience": 88.0,
    "qualificationEnglish": 76.0,
    "qualificationOpt": 82.0,
    "extraScore": 6.0
  }'
```

### 4. 기존 API 테스트 (개별 처리)

#### 원서 작성
```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "applicantName": "홍길동",
    "applicantTel": "01012345678",
    "parentName": "홍부모",
    "parentTel": "01098765432",
    "sex": "MALE",
    "birthDate": "2006-03-15",
    "streetAddress": "대전광역시 유성구 가정로 76",
    "postalCode": "34111",
    "detailAddress": "101동 501호",
    "isDaejeon": true,
    "applicationType": "COMMON",
    "applicationRemark": "NOTHING",
    "educationalStatus": "PROSPECTIVE_GRADUATE"
  }'
```

#### 성적 입력 (확장된 학기별 과목별 성적)
```bash
curl -X POST http://localhost:8080/api/applications/user/550e8400-e29b-41d4-a716-446655440000/score \
  -H "Content-Type: application/json" \
  -d '{
    "attendanceScore": 28,
    "volunteerScore": 25.5,
    "korean_3_1": "AABB",
    "social_3_1": "ABBC",
    "history_3_1": "AAAB",
    "math_3_1": "BBCC",
    "science_3_1": "ABCC",
    "english_3_1": "AABC",
    "techAndHome_3_1": "ABCC",
    "koreanGrade": "AABB",
    "socialGrade": "ABBC",
    "historyGrade": "AAAB",
    "mathGrade": "BBCC",
    "scienceGrade": "ABCC",
    "englishGrade": "AABC",
    "techAndHomeGrade": "ABCC",
    "korean_2_2": "BBCC",
    "social_2_2": "ABCC",
    "history_2_2": "BBCC",
    "math_2_2": "CCDD",
    "science_2_2": "BCCD",
    "english_2_2": "ABCD",
    "techAndHome_2_2": "BCDD",
    "korean_2_1": "CCDD",
    "social_2_1": "BCCD",
    "history_2_1": "CCDD",
    "math_2_1": "DDEE",
    "science_2_1": "CDDD",
    "english_2_1": "BCDE",
    "techAndHome_2_1": "CDEE",
    "thirdGradeScore": 85.5,
    "thirdBeforeScore": 82.0,
    "thirdBeforeBeforeScore": 79.5,
    "thirdScore": 87.0,
    "extraScore": 3.0
  }'
```

#### 성적 계산 실행
```bash
curl -X POST http://localhost:8080/api/applications/24001/calculate-score \
  -H "Content-Type: application/json"
```

#### 원서 최종 제출 + 자동 계산
```bash
curl -X POST http://localhost:8080/api/applications/user/550e8400-e29b-41d4-a716-446655440000/submit \
  -H "Content-Type: application/json"
```

### 5. 조회 API

#### 원서 조회 (접수번호로)
```bash
curl -X GET http://localhost:8080/api/applications/24001 \
  -H "Content-Type: application/json"
```

#### 성적 조회
```bash
curl -X GET http://localhost:8080/api/applications/24001/score \
  -H "Content-Type: application/json"
```

#### 모든 원서 조회
```bash
curl -X GET http://localhost:8080/api/applications \
  -H "Content-Type: application/json"
```

#### 수식 집합 조회
```bash
curl -X GET http://localhost:8080/api/v1/formula-sets \
  -H "Content-Type: application/json"
```

#### 특정 수식 집합 조회
```bash
curl -X GET http://localhost:8080/api/v1/formula-sets/formula-set-001 \
  -H "Content-Type: application/json"
```

### 6. 수식 실행 테스트

#### 동적 수식 실행
```bash
curl -X POST http://localhost:8080/api/v1/formula-sets/formula-set-001/execute \
  -H "Content-Type: application/json" \
  -d '{
    "korean_3_1_grade": 4.0,
    "math_3_1_grade": 3.5,
    "english_3_1_grade": 4.5,
    "science_3_1_grade": 3.0,
    "social_3_1_grade": 4.0,
    "history_3_1_grade": 3.5,
    "tech_3_1_grade": 3.5,
    "korean_3_2_grade": 4.5,
    "math_3_2_grade": 4.0,
    "english_3_2_grade": 4.5,
    "science_3_2_grade": 3.5,
    "social_3_2_grade": 4.0,
    "history_3_2_grade": 4.0,
    "tech_3_2_grade": 3.5,
    "attendance_score": 28.0,
    "volunteer_score": 25.5
  }'
```

### 7. 에러 케이스 테스트

#### 존재하지 않는 사용자로 원서 작성
```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "00000000-0000-0000-0000-000000000000",
    "applicantName": "테스트",
    "applicationType": "COMMON",
    "educationalStatus": "PROSPECTIVE_GRADUATE"
  }'
```

#### 잘못된 접수번호로 조회
```bash
curl -X GET http://localhost:8080/api/applications/99999 \
  -H "Content-Type: application/json"
```

#### 중복 전화번호로 사용자 생성
```bash
curl -X POST http://localhost:8080/api/applications/users \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01012345678",
    "name": "중복사용자",
    "isParent": false
  }'
```

### 8. 수식 관리 테스트

#### 수식 집합 수정
```bash
curl -X PUT http://localhost:8080/api/v1/formula-sets/formula-set-001 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "일반전형_졸업예정자_대전_수정",
    "description": "수정된 일반전형 졸업예정자 대전지역 점수 계산",
    "type": "GRADE_CALCULATION",
    "formulas": [
      {
        "name": "최종점수_계산_수정",
        "expression": "(semester_3_1_avg * 0.7 + semester_3_2_avg * 0.3) * 2.0 + attendance_score + volunteer_score",
        "order": 1,
        "resultVariable": "final_score"
      }
    ],
    "isActive": true
  }'
```

#### 수식 집합 삭제
```bash
curl -X DELETE http://localhost:8080/api/v1/formula-sets/formula-set-001 \
  -H "Content-Type: application/json"
```

## 성능 테스트

### 대량 원서 생성 (bash script)
```bash
#!/bin/bash
for i in {1..10}; do
  # 1. 사용자 먼저 생성
  USER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/applications/users \
    -H "Content-Type: application/json" \
    -d "{
      \"phoneNumber\": \"0101234567$i\",
      \"name\": \"테스트사용자$i\",
      \"isParent\": false
    }")
  
  # 2. 응답에서 userId 추출 (jq 사용)
  USER_ID=$(echo $USER_RESPONSE | jq -r '.userId')
  
  # 3. 통합 원서 작성
  curl -X POST http://localhost:8080/api/applications/user/$USER_ID/complete \
    -H "Content-Type: application/json" \
    -d "{
      \"applicantName\": \"테스트사용자$i\",
      \"applicantTel\": \"0101234567$i\",
      \"parentName\": \"테스트부모$i\",
      \"parentTel\": \"0109876543$i\",
      \"sex\": \"MALE\",
      \"birthDate\": \"2006-03-15\",
      \"streetAddress\": \"대전광역시 유성구 가정로 76\",
      \"postalCode\": \"34111\",
      \"detailAddress\": \"$i동 $i호\",
      \"isDaejeon\": true,
      \"applicationType\": \"COMMON\",
      \"applicationRemark\": \"NOTHING\",
      \"educationalStatus\": \"PROSPECTIVE_GRADUATE\",
      \"attendanceScore\": $((25 + i)),
      \"volunteerScore\": $((20 + i)),
      \"korean_3_1\": \"AABB\",
      \"math_3_1\": \"BBCC\",
      \"extraScore\": $((i % 5))
    }" &
    
  if [ $((i % 5)) -eq 0 ]; then
    wait
  fi
done
wait
```

---

## 변수명 매핑 예시

### 생성되는 변수명 패턴
- **패턴**: `{receiptCode}_{applicationType}_{educationalStatus}_{region}_{category}`
- **예시**: 
  - `24001_common_prosp_dj_attendance` (접수번호 24001, 일반전형, 졸업예정자, 대전지역, 출석점수)
  - `24001_common_prosp_dj_3_1_korean` (3학년 1학기 국어)
  - `24001_social_qual_nw_qualification_korean` (사회통합전형, 검정고시, 전국, 검정고시 국어)

### 플래그 변수들
- `24001_common_prosp_dj_type_common` (일반전형 여부: 1.0 또는 0.0)
- `24001_common_prosp_dj_region_daejeon` (대전지역 여부: 1.0 또는 0.0)
- `24001_common_prosp_dj_edu_prospective` (졸업예정자 여부: 1.0 또는 0.0)

---

**주의사항:**
1. 서버가 실행 중이어야 합니다
2. 실제 UUID는 응답에서 받은 값을 사용하세요
3. 수식 집합이 사전에 등록되어 있어야 성적 계산이 가능합니다
4. 통합 API(`/complete`)를 사용하면 모든 과정이 자동으로 처리됩니다
5. 동시성 테스트 시 적절한 딜레이를 두고 실행하세요