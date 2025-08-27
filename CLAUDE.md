# Casper Application - Claude 가이드

> 수학식 처리를 위한 DDD 기반 도메인 중심 Kotlin 애플리케이션

## 📋 프로젝트 개요

### 프로젝트 정보
- **이름**: Casper-Application
- **버전**: 0.0.1
- **그룹**: hs.kr.entrydsm
- **언어**: Kotlin (v1.9.23)
- **빌드 도구**: Gradle with Kotlin DSL
- **아키텍처**: Domain-Driven Design (DDD)

### 프로젝트 목적
**동적 수식 계산 시스템**을 통한 입시 전형 점수 계산 자동화

#### 문제 정의
기존 equus-application의 입시 전형 계산 시스템은 모든 계산 로직이 하드코딩되어 있어 다음과 같은 문제점이 있습니다:
- **정책 변경 대응 어려움**: 입시 정책 변경 시마다 코드 수정 및 배포 필요
- **휴먼 에러**: 약 30여 개의 하드코딩된 상수들이 여러 파일에 중복 정의
- **확장성 부족**: 새로운 전형 추가 시 전체 시스템 수정 필요

#### 해결 방안
casper-application은 고급 수학식 파싱 엔진을 활용하여:
- **MySQL + JPA 기반 수식 저장**: 모든 계산 로직을 DB에서 관리
- **동적 변수 시스템**: 정책 변경 시 DB 수정만으로 즉시 반영
- **실시간 수식 검증**: 잘못된 수식 사전 차단 및 안전한 계산 환경 제공

## 🏗️ 아키텍처

### 모듈 구조
```
casper-application/
├── buildSrc/                          # 빌드 설정 관리
├── casper-convention/                  # 커스텀 Gradle 플러그인 (문서화 규칙)
├── casper-application-domain/          # 도메인 계층 (핵심 비즈니스 로직)
└── casper-application-infrastructure/  # 인프라 계층 (Spring Boot, Web)
```

### 도메인 계층 구조 (DDD 패턴)
```
domain/
├── ast/           # 추상 구문 트리 (Abstract Syntax Tree)
├── calculator/    # 계산기 핵심 도메인
├── evaluator/     # 표현식 평가
├── expresser/     # 수식 포맷팅 및 출력
├── lexer/         # 어휘 분석 (토큰화)
├── parser/        # 구문 분석 (파싱)
└── global/        # 공통 도메인 구성요소
```

각 도메인 컨텍스트는 DDD 패턴으로 구조화:
- **aggregates/** - 집계 루트
- **entities/** - 엔티티
- **values/** - 값 객체
- **factories/** - 팩토리
- **services/** - 도메인 서비스
- **specifications/** - 명세 패턴
- **policies/** - 정책 객체

## 🔧 개발 환경

### 필수 요구사항
- **Java**: OpenJDK 17 이상
- **Kotlin**: 1.9.23
- **Gradle**: 7.0 이상 (Wrapper 사용 권장)

### 주요 의존성
```kotlin
// 도메인 모듈
kotlinx-serialization-json: 1.6.3
kotlinx-coroutines-core: 1.8.1

// 인프라 모듈
spring-boot: 3.4.4
apache-commons-jexl: 3.5.0
```

### 코드 품질 도구
- **Ktlint**: 12.1.1 (코드 스타일 검사)
- **Custom Convention Plugin**: KDoc 문서화 규칙 검사

## 🚀 시작하기

### 빌드 및 실행
```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew :casper-application-infrastructure:bootRun

# 테스트 실행
./gradlew test

# 코드 스타일 검사
./gradlew ktlintCheck

# 문서화 규칙 검사
./gradlew checkAllDocs
```

### 검증 태스크
```bash
# 모든 검증 실행
./gradlew check

# 개별 문서화 검사
./gradlew checkClassDocs      # 클래스 KDoc 검사
./gradlew checkObjectDocs     # 객체 KDoc 검사
./gradlew checkInterfaceDocs  # 인터페이스 KDoc 검사
./gradlew checkFunctionDocs   # 함수 KDoc 검사
```

## 💻 Kotlin 코딩 컨벤션

### 아키텍처 원칙
1. **DDD 우선**: 모든 도메인 객체는 명확한 DDD 패턴 적용
2. **불변성**: 모든 도메인 객체는 불변으로 설계
3. **타입 안전성**: 컴파일 타임 타입 검증 우선

### 네이밍 컨벤션
- **클래스**: PascalCase (`CalculationSession`, `ASTNode`)
- **패키지**: 소문자 (`calculator`, `entities`)
- **변수/함수**: camelCase (`executionTimeMs`, `getVariables()`)
- **상수**: UPPER_SNAKE_CASE (`MAX_FORMULA_LENGTH`)
- **도메인 용어**: 유비쿼터스 언어 사용 (`AST`, `LR`)

### 클래스 구조 스타일
```kotlin
// Data class 선호
data class CalculationSession(
    val sessionId: String,
    val userId: String?,
    val formula: String,
    val variables: Map<String, Any> = emptyMap(),
    val executionTimeMs: Long? = null
) : EntityMarker {
    init {
        require(sessionId.isNotBlank()) { "Session ID must not be blank" }
        require(formula.isNotBlank()) { "Formula must not be blank" }
    }
    
    companion object {
        const val MAX_FORMULA_LENGTH = 1000
        
        fun create(userId: String?, formula: String): CalculationSession {
            return CalculationSession(
                sessionId = generateSessionId(),
                userId = userId,
                formula = formula
            )
        }
    }
}

// Sealed class 활용
sealed class ASTNode : EntityMarker {
    abstract val nodeType: NodeType
    abstract fun accept(visitor: ASTVisitor): Any
}
```

### 함수형 프로그래밍 스타일
```kotlin
// 고차 함수 활용
inline fun <R> Result<T, E>.fold(
    onSuccess: (T) -> R,
    onFailure: (E) -> R
): R = when (this) {
    is Success -> onSuccess(value)
    is Failure -> onFailure(error)
}

// 불변성과 함수형 스타일
fun calculateScore(formulas: List<String>, variables: Map<String, Any>): List<CalculationResult> {
    return formulas
        .map { formula -> CalculationRequest(formula, variables) }
        .map { request -> calculator.calculate(request) }
        .filter { result -> result.isSuccess() }
}
```

### DDD 어노테이션 활용
```kotlin
@Entity(aggregateRoot = "Calculator", context = "calculator")
data class CalculationSession(...)

@Aggregate(context = "calculator")
class Calculator private constructor(...) : AggregateMarker {
    companion object {
        @JvmStatic
        fun createDefault(): Calculator = Calculator(...)
    }
}

@Factory(complexity = Complexity.MEDIUM, cache = true)
object CalculatorFactory {
    fun create(config: CalculatorConfiguration): Calculator = ...
}
```

## 🧪 테스트 전략

### 테스트 구조
- **단위 테스트**: JUnit 5 + Kotlin Test
- **기능 테스트**: 도메인별 functional test
- **통합 테스트**: 다중 단계 계산 검증

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "CalculatorFunctionalTest"

# 도메인별 테스트
./gradlew :casper-application-domain:test
```

### 주요 테스트 케이스
1. **기본 산술 연산**: 사칙연산, 우선순위, 괄호
2. **변수 처리**: 변수 치환 및 계산
3. **조건문**: IF 함수 및 중첩 IF
4. **복잡한 수식**: 실제 점수 계산 수식
5. **오류 처리**: 다양한 오류 상황 검증
6. **성능 테스트**: 반복 계산 성능 검증

## 🏢 도메인 모델링

### 기존 도메인 구조 분석

#### 1. AST (추상 구문 트리) 도메인
- **Aggregate Root**: `ExpressionAST` - AST 전체 관리, 최적화, 검증
- **Entities**: `ASTNode`, `BinaryOpNode`, `UnaryOpNode`, `NumberNode`, `VariableNode`, `FunctionCallNode`, `IfNode`
- **Services**: `TreeTraverser`, `TreeOptimizer`
- **Value Objects**: `NodeSize`, `TreeDepth`, `OptimizationLevel`, `ASTValidationResult`
- **재활용 방안**: **완전 재활용** - 수식 파싱과 최적화에 그대로 활용

#### 2. Calculator (계산기) 도메인 
- **Aggregate Root**: `Calculator` - 계산 프로세스 전체 조율 (렉싱→파싱→평가)
- **Entities**: `CalculationSession` - 계산 세션, 변수 바인딩, 이력 관리
- **Services**: `CalculatorService`, `ValidationService`  
- **Value Objects**: `CalculationRequest`, `CalculationResult`, `CalculationStep`
- **재활용 방안**: **확장 재활용** - FormulaSet 실행 기능 추가

#### 3. Evaluator (평가기) 도메인
- **Aggregate Root**: `ExpressionEvaluator` - AST Visitor 패턴으로 노드 평가
- **Entities**: `EvaluationContext`, `MathFunction`
- **Services**: `MathFunctionService`
- **Registries**: `FunctionRegistry` - 수학 함수 등록소
- **재활용 방안**: **완전 재활용** - 기존 평가 로직 그대로 활용

#### 4. Lexer/Parser 도메인
- **Lexer Aggregate**: `LexerAggregate` - 토큰화 프로세스
- **Parser Aggregates**: `LRParser`, `LRParserTable`, `AutomaticLRParserGenerator`
- **Entities**: `Token`, `LRItem`, `ParsingState`, `Production`
- **Services**: `ParserService`, `LRParserTableService`
- **재활용 방안**: **완전 재활용** - 수식 파싱에 그대로 활용

#### 5. Expresser (표현기) 도메인
- **Aggregate Root**: `ExpressionFormatter`, `ExpressionReporter`
- **Services**: `ExpresserService` - 수식/결과 포맷팅
- **재활용 방안**: **부분 재활용** - 수식 표시용 확장

### 수식 관리 도메인 확장 계획

#### 6. Formula (수식 집합) 도메인 - **신규 추가**
- **Aggregate Root**: `FormulaSet` - 순서가 있는 수식 그룹 관리
- **Entities**: `Formula`, `VariableDefinition`, `FormulaExecution`
- **Value Objects**: `FormulaSetId`, `FormulaType`, `FormulaDependency`
- **Services**: `FormulaExecutionService`, `FormulaDependencyService`
- **통합 전략**: Calculator 도메인과 협업하여 수식 집합 실행

### 도메인 간 의존 관계
```
Formula Domain (신규)
    ↓ 의존
Calculator Domain (확장)
    ├── Lexer Domain (재활용)
    ├── Parser Domain (재활용)  
    ├── AST Domain (재활용)
    ├── Evaluator Domain (재활용)
    └── Expresser Domain (재활용)

Global Domain (재활용)
    ↑ 모든 도메인이 의존
```

### 하드코딩 최소화 전략

casper-application은 **체계적인 하드코딩 방지 아키텍처**를 구축하고 있습니다:

#### 1. 상수 관리 계층화
```kotlin
// 전역 상수 통합 관리
object ErrorCodes {
    val Common = CommonErrorCodes      // 공통 에러 (CMN001-CMN999)
    val Lexer = LexerErrorCodes       // 렉서 에러 (LEX001-LEX999)  
    val Calculator = CalculatorErrorCodes // 계산기 에러 (CAL001-CAL999)
    // ... 각 도메인별 에러 코드 분리
}

// 명명 규칙 표준화
object NamingConventions {
    object Factory { const val SUFFIX = "Factory" }
    object Specification { const val SUFFIX = "Spec" }
    object Service { const val SUFFIX = "Service" }
    // 일관된 네이밍 패턴으로 하드코딩 방지
}
```

#### 2. Configuration 기반 설정 관리
```kotlin
// 도메인별 설정 외부화
@ConfigurationProvider
class CalculatorConfiguration(
    val maxFormulaLength: Int = 5000,
    val maxVariables: Int = 100,
    val timeout: Duration = Duration.ofSeconds(30)
)

@ConfigurationProvider  
class ASTConfiguration(
    val maxDepth: Int = 100,
    val maxNodes: Int = 1000,
    val optimizationLevel: OptimizationLevel = OptimizationLevel.BASIC
)
```

#### 3. ErrorCode 체계적 관리
```kotlin
// 도메인별 에러 코드 분리 및 중앙화
enum class ErrorCode(val code: String, val description: String) {
    // 체계적 코드 체계 (도메인접두사 + 번호)
    FORMULA_EXPRESSION_EMPTY("FOR001", "수식 표현식은 비어있을 수 없습니다"),
    FORMULA_NAME_INVALID("FOR002", "수식 이름이 유효하지 않습니다"),
    FORMULA_ORDER_INVALID("FOR003", "수식 순서는 양수여야 합니다")
}

// 하드코딩된 문자열 메시지 제거
require(expression.isNotBlank()) { ErrorCode.FORMULA_EXPRESSION_EMPTY.description }
require(order > 0) { ErrorCode.FORMULA_ORDER_INVALID.description }
```

#### 4. Validation Strategy 패턴
```kotlin
// 검증 로직을 Strategy로 분리
interface ValidationStrategy<T> {
    fun validate(target: T): ValidationResult
}

class FormulaValidationStrategy : ValidationStrategy<Formula> {
    override fun validate(formula: Formula): ValidationResult {
        return ValidationResult.builder()
            .check("expression.notBlank") { formula.expression.isNotBlank() }
            .check("name.notBlank") { formula.name.isNotBlank() }  
            .check("order.positive") { formula.order > 0 }
            .build()
    }
}
```

#### 5. 정책 기반 제약 관리
```kotlin
// 비즈니스 규칙을 Policy로 외부화
@Policy(domain = "formula", scope = VALIDATION)
class FormulaConstraintPolicy {
    companion object {
        const val MAX_EXPRESSION_LENGTH = 1000
        const val MAX_NAME_LENGTH = 100
        const val MIN_ORDER = 1
        const val MAX_ORDER = 999
    }
}
```

### 기존 구조 보존 원칙
1. **설정 외부화**: 모든 상수를 Configuration 클래스로 분리
2. **에러 코드 중앙화**: 체계적 ErrorCode enum으로 메시지 통합 관리  
3. **검증 전략화**: Validation을 Strategy 패턴으로 분리하여 재사용성 확보
4. **명명 표준화**: NamingConventions으로 일관된 코딩 스타일 유지
5. **정책 기반 설계**: Policy 패턴으로 비즈니스 규칙 외부화

## 🔍 코드 분석 도구

### 정적 분석
```bash
# Ktlint 실행
./gradlew ktlintCheck

# KDoc 문서화 검사
./gradlew checkAllDocs

# 전체 검증 (테스트 + 정적분석)
./gradlew check
```

### IDE 통합
- **IntelliJ IDEA**: Kotlin 플러그인으로 최적화된 환경
- **ktlint**: IDE 플러그인으로 실시간 스타일 검사
- **KDoc**: 문서화 힌트 및 자동완성

## 📚 참고 자료

### 아키텍처 패턴
- [Domain-Driven Design](https://domainlanguage.com/ddd/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

### Kotlin 가이드
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)

### 함수형 프로그래밍
- [Functional Kotlin](https://arrow-kt.io/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

## 🎯 동적 수식 시스템

### 핵심 기능
casper-application은 기존 하드코딩된 입시 전형 계산을 동적 수식 시스템으로 대체합니다.

#### 주요 컴포넌트
1. **FormulaSet (수식 집합)**: 전형별 계산 수식들을 그룹화하여 관리
2. **Variable System (변수 시스템)**: 사용자 입력 변수와 계산 결과 변수 매핑
3. **Dynamic Calculator (동적 계산기)**: DB 저장된 수식을 실시간으로 실행

### 사용 시나리오

#### 1. 수식 집합 등록
```http
POST /api/v1/formula-sets
{
  "name": "일반전형_교과점수",
  "description": "일반전형 교과점수 계산",
  "formulas": [
    {
      "name": "3학년_1학기_평균",
      "expression": "(korean_3_1 + math_3_1 + english_3_1 + science_3_1 + social_3_1 + history_3_1 + tech_3_1) / 7",
      "order": 1
    },
    {
      "name": "2학년_2학기_평균", 
      "expression": "(korean_2_2 + math_2_2 + english_2_2 + science_2_2 + social_2_2 + history_2_2 + tech_2_2) / 7",
      "order": 2
    },
    {
      "name": "교과점수",
      "expression": "(__step_1 * 8 + __step_2 * 4) * COMMON_GRADE_RATE",
      "order": 3
    }
  ],
  "variables": [
    {
      "name": "COMMON_GRADE_RATE",
      "value": 1.75,
      "description": "일반전형 교과점수 배율"
    }
  ]
}
```

#### 2. 계산 실행
```http
POST /api/v1/calculate
{
  "formulaSetId": "uuid-here",
  "userInputs": {
    "korean_3_1": 4,
    "math_3_1": 5,
    "english_3_1": 3,
    "science_3_1": 4,
    "social_3_1": 3,
    "history_3_1": 4,
    "tech_3_1": 3,
    "korean_2_2": 3,
    "math_2_2": 4,
    "english_2_2": 3,
    "science_2_2": 4,
    "social_2_2": 3,
    "history_2_2": 4,
    "tech_2_2": 3
  }
}
```

#### 3. 응답
```json
{
  "success": true,
  "results": [
    {
      "step": 1,
      "name": "3학년_1학기_평균",
      "result": 3.714,
      "formula": "(korean_3_1 + math_3_1 + english_3_1 + science_3_1 + social_3_1 + history_3_1 + tech_3_1) / 7"
    },
    {
      "step": 2,
      "name": "2학년_2학기_평균",
      "result": 3.429,
      "formula": "(korean_2_2 + math_2_2 + english_2_2 + science_2_2 + social_2_2 + history_2_2 + tech_2_2) / 7"
    },
    {
      "step": 3,
      "name": "교과점수",
      "result": 75.855,
      "formula": "(__step_1 * 8 + __step_2 * 4) * COMMON_GRADE_RATE"
    }
  ]
}
```

### 장점
1. **즉시 반영**: 정책 변경 시 DB 수정만으로 즉시 적용
2. **오류 방지**: 단일 진실 공급원으로 중복 정의 문제 해결
3. **확장 가능**: 새로운 전형 추가 시 수식만 등록하면 완료
4. **검증 가능**: 복잡한 수식도 단계별로 추적 및 검증 가능

### 기술적 특징
- **안전한 수식 실행**: 샌드박스 환경에서 수식 실행
- **캐시 최적화**: 자주 사용되는 계산 결과 캐싱
- **성능 모니터링**: 계산 시간 및 복잡도 추적
- **버전 관리**: 수식 변경 이력 및 롤백 지원

## 🤝 기여하기

### 코드 품질 기준
1. **KDoc 작성**: 모든 public API는 KDoc 문서화 필수
2. **테스트 작성**: 새로운 기능은 테스트 코드와 함께 제출
3. **DDD 패턴 준수**: 도메인 객체는 적절한 DDD 패턴 적용
4. **불변성 유지**: 가능한 모든 객체를 불변으로 설계
5. **함수형 스타일**: 고차 함수와 함수 합성 활용

### PR 체크리스트
- [ ] KDoc 문서화 완료
- [ ] 테스트 코드 작성 및 통과
- [ ] Ktlint 스타일 검사 통과
- [ ] 도메인 모델링 일관성 검증
- [ ] 성능 영향도 검토

---

**이 가이드는 Casper Application의 Kotlin/DDD 기반 개발을 위한 종합적인 참조 문서입니다.**