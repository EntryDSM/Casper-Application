package hs.kr.entrydsm.domain.user.aggregates

import hs.kr.entrydsm.global.annotation.aggregates.Aggregate
import java.util.UUID

/**
 * 사용자 정보를 나타내는 도메인 애그리게이트입니다.
 * 
 * 사용자의 기본 정보와 부모 여부를 관리하며,
 * 다른 마이크로서비스로부터 받아온 사용자 데이터를 도메인 내에서 활용하기 위한 모델입니다.
 */
@Aggregate(context = "user")
data class User(
    /**
     * 사용자의 고유 식별자
     */
    val id: UUID,
    
    /**
     * 사용자의 전화번호
     */
    val phoneNumber: String,
    
    /**
     * 사용자의 이름
     */
    val name: String,
    
    /**
     * 부모 여부를 나타내는 플래그
     * true인 경우 부모, false인 경우 학생
     */
    val isParent: Boolean
)
