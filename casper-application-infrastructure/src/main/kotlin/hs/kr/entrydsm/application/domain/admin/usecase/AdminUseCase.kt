package hs.kr.entrydsm.application.domain.admin.usecase

import hs.kr.entrydsm.application.domain.admin.usecase.result.CreateApplicationTypeResult
import hs.kr.entrydsm.application.domain.admin.usecase.result.CreateEducationalStatusResult
import hs.kr.entrydsm.domain.application.values.ApplicationType
import hs.kr.entrydsm.domain.application.values.EducationalStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 관리자 UseCase
 *
 * ApplicationType과 EducationalStatus는 이제 Enum으로 관리되므로
 * 동적 생성 기능은 더 이상 필요하지 않습니다.
 */
@Service
@Transactional
class AdminUseCase {
    /**
     * 지원 가능한 전형 타입 목록 조회
     */
    fun getApplicationTypes(): List<CreateApplicationTypeResult> {
        return ApplicationType.entries.map {
            CreateApplicationTypeResult(
                typeId = it.name,
                code = it.name,
                name = it.displayName,
            )
        }
    }

    /**
     * 지원 가능한 교육 상태 목록 조회
     */
    fun getEducationalStatuses(): List<CreateEducationalStatusResult> {
        return EducationalStatus.entries.map {
            CreateEducationalStatusResult(
                statusId = it.name,
                code = it.name,
                name = it.displayName,
            )
        }
    }
}
