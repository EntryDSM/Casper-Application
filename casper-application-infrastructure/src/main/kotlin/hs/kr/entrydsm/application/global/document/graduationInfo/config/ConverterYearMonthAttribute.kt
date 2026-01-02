package hs.kr.entrydsm.application.global.document.graduationInfo.config

import java.time.YearMonth
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class ConverterYearMonthAttribute : AttributeConverter<YearMonth?, String?> {

    override fun convertToDatabaseColumn(attribute: YearMonth?): String? {
        return attribute?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): YearMonth? {
        return dbData?.let { YearMonth.parse(it) }
    }
}