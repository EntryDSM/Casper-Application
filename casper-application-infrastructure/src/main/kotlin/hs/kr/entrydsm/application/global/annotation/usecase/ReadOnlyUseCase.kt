package hs.kr.entrydsm.application.global.annotation.usecase

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *
 * 조회 기능을 담당하는 사용자 UseCase를 나타내는 어노테이션
 *
 * @author 박주원
 **/
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Transactional(readOnly = true)
@Service
annotation class ReadOnlyUseCase()
