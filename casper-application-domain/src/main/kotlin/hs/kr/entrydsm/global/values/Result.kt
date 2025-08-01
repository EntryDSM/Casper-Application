package hs.kr.entrydsm.global.values

/**
 * 도메인 연산의 결과를 나타내는 값 객체입니다.
 *
 * DDD Value Object 패턴을 적용하여 성공과 실패를 명시적으로 표현하며,
 * 함수형 프로그래밍의 Result 타입과 유사한 인터페이스를 제공합니다.
 * 예외 대신 명시적인 결과 타입을 사용하여 안전하고 예측 가능한 코드를 작성할 수 있습니다.
 *
 * @param T 성공 시 반환되는 값의 타입
 * @param E 실패 시 반환되는 오류의 타입
 *
 * @see <a href="https://devblog.kakaostyle.com/ko/2025-03-21-1-domain-driven-hexagonal-architecture-by-example/">코드 사례로 보는 Domain-Driven 헥사고날 아키텍처</a>
 *
 * @author kangeunchan
 * @since 2025.07.20
 */
sealed class Result<out T, out E> {
    
    /**
     * 성공 결과를 나타내는 클래스입니다.
     *
     * @param value 성공 시 반환되는 값
     */
    data class Success<out T>(val value: T) : Result<T, Nothing>() {
        override fun toString(): String = "Success($value)"
    }
    
    /**
     * 실패 결과를 나타내는 클래스입니다.
     *
     * @param error 실패 시 반환되는 오류
     */
    data class Failure<out E>(val error: E) : Result<Nothing, E>() {
        override fun toString(): String = "Failure($error)"
    }
    
    /**
     * 결과가 성공인지 확인합니다.
     *
     * @return 성공이면 true, 실패면 false
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * 결과가 실패인지 확인합니다.
     *
     * @return 실패면 true, 성공이면 false
     */
    fun isFailure(): Boolean = this is Failure
    
    /**
     * 성공 값을 반환하거나 null을 반환합니다.
     *
     * @return 성공 시 값, 실패 시 null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }
    
    /**
     * 실패 오류를 반환하거나 null을 반환합니다.
     *
     * @return 실패 시 오류, 성공 시 null
     */
    fun errorOrNull(): E? = when (this) {
        is Success -> null
        is Failure -> error
    }
    
    /**
     * 성공 값을 반환하거나 기본값을 반환합니다.
     *
     * @param defaultValue 기본값
     * @return 성공 시 값, 실패 시 기본값
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> defaultValue
    }
    
    /**
     * 성공 값을 반환하거나 람다 함수의 결과를 반환합니다.
     *
     * @param onFailure 실패 시 실행할 람다 함수
     * @return 성공 시 값, 실패 시 람다 함수 결과
     */
    inline fun getOrElse(onFailure: (E) -> @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> onFailure(error)
    }
    
    /**
     * 성공 값을 변환합니다.
     *
     * @param transform 변환 함수
     * @return 변환된 결과
     */
    inline fun <R> map(transform: (T) -> R): Result<R, E> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }
    
    /**
     * 실패 오류를 변환합니다.
     *
     * @param transform 변환 함수
     * @return 변환된 결과
     */
    inline fun <F> mapError(transform: (E) -> F): Result<T, F> = when (this) {
        is Success -> this
        is Failure -> Failure(transform(error))
    }
    
    /**
     * 성공 값에 다른 Result를 반환하는 함수를 적용합니다.
     *
     * @param transform 변환 함수
     * @return 평면화된 결과
     */
    inline fun <R> flatMap(transform: (T) -> Result<R, @UnsafeVariance E>): Result<R, E> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }
    
    /**
     * 성공 시 지정된 동작을 수행합니다.
     *
     * @param action 수행할 동작
     * @return 현재 Result 인스턴스
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T, E> {
        if (this is Success) action(value)
        return this
    }
    
    /**
     * 실패 시 지정된 동작을 수행합니다.
     *
     * @param action 수행할 동작
     * @return 현재 Result 인스턴스
     */
    inline fun onFailure(action: (E) -> Unit): Result<T, E> {
        if (this is Failure) action(error)
        return this
    }
    
    /**
     * 결과를 폴드합니다.
     *
     * @param onSuccess 성공 시 변환 함수
     * @param onFailure 실패 시 변환 함수
     * @return 폴드된 결과
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (E) -> R
    ): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(error)
    }
    
    /**
     * 성공 값에 대해 조건을 확인합니다.
     *
     * @param predicate 확인할 조건
     * @param errorProvider 조건이 false일 때 반환할 오류 제공자
     * @return 조건이 참이면 현재 결과, 거짓이면 실패 결과
     */
    inline fun <F> filter(
        predicate: (T) -> Boolean,
        errorProvider: () -> F
    ): Result<T, F> = when (this) {
        is Success -> if (predicate(value)) Success(value) else Failure(errorProvider())
        is Failure -> Failure(errorProvider())
    }
    
    /**
     * 다른 Result와 결합합니다.
     *
     * @param other 결합할 다른 Result
     * @param combiner 결합 함수
     * @return 결합된 결과
     */
    inline fun <U, R> zip(
        other: Result<U, @UnsafeVariance E>,
        combiner: (T, U) -> R
    ): Result<R, E> = when {
        this is Success && other is Success -> Success(combiner(this.value, other.value))
        this is Failure -> this
        other is Failure -> other
        else -> throw IllegalStateException("Unreachable")
    }
    
    /**
     * Result를 List로 변환합니다.
     *
     * @return 성공 시 값을 포함한 리스트, 실패 시 빈 리스트
     */
    fun toList(): List<T> = when (this) {
        is Success -> listOf(value)
        is Failure -> emptyList()
    }
    
    /**
     * 예외를 발생시키며 성공 값을 반환합니다.
     *
     * @return 성공 시 값
     * @throws IllegalStateException 실패 시
     */
    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw IllegalStateException("Result is failure: $error")
    }
    
    /**
     * 예외를 발생시키며 성공 값을 반환합니다.
     *
     * @param exceptionProvider 예외 제공자
     * @return 성공 시 값
     * @throws Exception 실패 시 제공된 예외
     */
    inline fun getOrThrow(exceptionProvider: (E) -> Exception): T = when (this) {
        is Success -> value
        is Failure -> throw exceptionProvider(error)
    }
    
    companion object {
        /**
         * 성공 결과를 생성합니다.
         *
         * @param value 성공 값
         * @return Success 인스턴스
         */
        fun <T> success(value: T): Result<T, Nothing> = Success(value)
        
        /**
         * 실패 결과를 생성합니다.
         *
         * @param error 실패 오류
         * @return Failure 인스턴스
         */
        fun <E> failure(error: E): Result<Nothing, E> = Failure(error)
        
        /**
         * 예외를 포착하여 Result로 변환합니다.
         *
         * @param block 실행할 코드 블록
         * @return 성공 또는 예외 결과
         */
        inline fun <T> catch(block: () -> T): Result<T, Exception> = try {
            Success(block())
        } catch (e: Exception) {
            Failure(e)
        }
        
        /**
         * 조건에 따라 Result를 생성합니다.
         *
         * @param condition 조건
         * @param value 성공 값 제공자
         * @param error 실패 오류 제공자
         * @return 조건에 따른 Result
         */
        inline fun <T, E> of(
            condition: Boolean,
            value: () -> T,
            error: () -> E
        ): Result<T, E> = if (condition) Success(value()) else Failure(error())
        
        /**
         * nullable 값을 Result로 변환합니다.
         *
         * @param value nullable 값
         * @param error null일 때 반환할 오류
         * @return 값이 있으면 Success, null이면 Failure
         */
        fun <T, E> fromNullable(value: T?, error: E): Result<T, E> = 
            if (value != null) Success(value) else Failure(error)
        
        /**
         * Result 리스트를 하나의 Result로 결합합니다.
         *
         * @param results Result 리스트
         * @return 모든 결과가 성공이면 성공 리스트, 하나라도 실패면 첫 번째 실패
         */
        fun <T, E> combine(results: List<Result<T, E>>): Result<List<T>, E> {
            val values = mutableListOf<T>()
            for (result in results) {
                when (result) {
                    is Success -> values.add(result.value)
                    is Failure -> return result
                }
            }
            return Success(values)
        }
        
        /**
         * Result 시퀀스를 하나의 Result로 결합합니다.
         *
         * @param results Result 시퀀스
         * @return 모든 결과가 성공이면 성공 리스트, 하나라도 실패면 첫 번째 실패
         */
        fun <T, E> combineSequence(results: Sequence<Result<T, E>>): Result<List<T>, E> = 
            combine(results.toList())
    }
}

/**
 * Result 확장 함수들
 */

/**
 * 두 Result를 결합합니다.
 */
infix fun <T, U, E> Result<T, E>.and(other: Result<U, E>): Result<Pair<T, U>, E> = 
    zip(other) { a, b -> a to b }

/**
 * Result 체이닝을 위한 확장 함수
 */
infix fun <T, U, E> Result<T, E>.then(next: (T) -> Result<U, E>): Result<U, E> = flatMap(next)

/**
 * 조건부 Result 변환
 */
inline fun <T, E> Result<T, E>.takeIf(predicate: (T) -> Boolean): Result<T?, E> = 
    map { value -> value.takeIf(predicate) }

/**
 * 조건부 Result 변환 (부정)
 */
inline fun <T, E> Result<T, E>.takeUnless(predicate: (T) -> Boolean): Result<T?, E> = 
    map { value -> value.takeUnless(predicate) }