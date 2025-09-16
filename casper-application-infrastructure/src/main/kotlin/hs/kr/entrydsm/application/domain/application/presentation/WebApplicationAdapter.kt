package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.ValidateScoreDataRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.PrototypeResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.SupportedTypesResponse
import hs.kr.entrydsm.application.domain.application.presentation.dto.response.ValidationResponse
import hs.kr.entrydsm.application.domain.application.usecase.ApplicationUseCase
import hs.kr.entrydsm.domain.application.values.ApplicationTypeFilter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class WebApplicationAdapter(
    private val applicationUseCase: ApplicationUseCase,
) {
    @GetMapping("/prototypes")
    fun getPrototype(
        @RequestParam applicationType: String,
        @RequestParam educationalStatus: String,
        @RequestParam(required = false) region: String?,
    ): ResponseEntity<PrototypeResponse> {
        val filter = ApplicationTypeFilter(applicationType, educationalStatus, region)
        val prototype = applicationUseCase.getPrototype(filter)

        val response =
            PrototypeResponse(
                success = true,
                data =
                    PrototypeResponse.PrototypeData(
                        applicationType = prototype.applicationType,
                        educationalStatus = prototype.educationalStatus,
                        region = prototype.region,
                        applicationFields =
                            prototype.application.mapValues { (_, fieldGroup) ->
                                fieldGroup.mapValues { (_, field) ->
                                    PrototypeResponse.FieldInfo(
                                        type = field.type,
                                        required = field.required,
                                        description = field.description,
                                    )
                                }
                            },
                        scoreFields =
                            prototype.score.mapValues { (_, fieldGroup) ->
                                fieldGroup.mapValues { (_, field) ->
                                    PrototypeResponse.FieldInfo(
                                        type = field.type,
                                        required = field.required,
                                        description = field.description,
                                    )
                                }
                            },
                        formulas =
                            prototype.formula.map { formula ->
                                PrototypeResponse.FormulaInfo(
                                    step = formula.step,
                                    name = formula.name,
                                    expression = formula.expression,
                                    resultVariable = formula.resultVariable,
                                )
                            },
                        constants = prototype.constant,
                    ),
            )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/types")
    fun getSupportedTypes(): ResponseEntity<SupportedTypesResponse> {
        val supportedTypes = applicationUseCase.getSupportedTypes()

        val response =
            SupportedTypesResponse(
                success = true,
                data =
                    SupportedTypesResponse.TypesData(
                        applicationTypes =
                            supportedTypes.applicationTypes.map { type ->
                                SupportedTypesResponse.TypeInfo(
                                    code = type.code,
                                    name = type.name,
                                )
                            },
                        educationalStatuses =
                            supportedTypes.educationalStatuses.map { status ->
                                SupportedTypesResponse.TypeInfo(
                                    code = status.code,
                                    name = status.name,
                                )
                            },
                    ),
            )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/validate")
    fun validateScoreData(
        @RequestBody request: ValidateScoreDataRequest,
    ): ResponseEntity<ValidationResponse> {
        val filter =
            ApplicationTypeFilter(
                request.applicationType,
                request.educationalStatus,
                null,
            )

        val validationResult = applicationUseCase.validateScoreData(filter, request.scoreData)

        val response =
            ValidationResponse(
                success = true,
                data =
                    ValidationResponse.ValidationData(
                        valid = validationResult.valid,
                        errors = validationResult.errors,
                        missingFields = validationResult.missingFields,
                        extraFields = validationResult.extraFields,
                    ),
            )

        return ResponseEntity.ok(response)
    }
}
