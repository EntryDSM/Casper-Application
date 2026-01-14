package hs.kr.entrydsm.application.domain.graduationInfo.domain.mapper

import hs.kr.entrydsm.application.domain.graduationInfo.domain.entity.GraduationJpaEntity
import hs.kr.entrydsm.application.domain.graduationInfo.domain.entity.vo.StudentNumber as EntityStudentNumber
import hs.kr.entrydsm.application.domain.graduationInfo.model.Graduation
import hs.kr.entrydsm.application.domain.graduationInfo.model.vo.StudentNumber as DomainStudentNumber
import hs.kr.entrydsm.application.global.mapper.GenericMapper
import org.springframework.stereotype.Component

@Component
class GraduationMapper : GenericMapper<GraduationJpaEntity, Graduation> {
    override fun toEntity(model: Graduation): GraduationJpaEntity {
        return GraduationJpaEntity(
            id = model.id,
            graduateDate = model.graduateDate,
            isProspectiveGraduate = model.isProspectiveGraduate,
            receiptCode = model.receiptCode,
            studentNumber = model.studentNumber?.let {
                EntityStudentNumber(
                    gradeNumber = it.gradeNumber,
                    classNumber = it.classNumber,
                    studentNumber = it.studentNumber
                )
            },
            schoolCode = model.schoolCode,
            teacherName = model.teacherName,
            teacherTel = model.teacherTel
        )
    }

    override fun toDomain(entity: GraduationJpaEntity?): Graduation? {
        return entity?.let {
            Graduation(
                id = it.id,
                graduateDate = it.graduateDate,
                isProspectiveGraduate = it.isProspectiveGraduate,
                receiptCode = it.receiptCode,
                studentNumber = it.studentNumber?.let { student ->
                    DomainStudentNumber(
                        gradeNumber = student.gradeNumber,
                        classNumber = student.classNumber,
                        studentNumber = student.studentNumber
                    )
                },
                schoolCode = it.schoolCode,
                teacherName = it.teacherName,
                teacherTel = it.teacherTel
            )
        }
    }

    override fun toDomainNotNull(entity: GraduationJpaEntity): Graduation {
        return toDomain(entity)!!
    }
}
