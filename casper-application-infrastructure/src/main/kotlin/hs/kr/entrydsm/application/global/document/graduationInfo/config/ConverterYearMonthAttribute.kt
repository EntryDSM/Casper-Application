package hs.kr.entrydsm.application.global.document.graduationInfo.config

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.YearMonth

@Converter(autoApply = true)
class ConverterYearMonthAttribute : AttributeConverter<YearMonth?, String?> {
    override fun convertToDatabaseColumn(attribute: YearMonth?): String? {
        return attribute?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): YearMonth? {
        return dbData?.let { YearMonth.parse(it) }
    }
}
