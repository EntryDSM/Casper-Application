package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.SubmitApplicationWebRequest
import hs.kr.entrydsm.application.domain.application.presentation.mapper.toSubmitApplicationRequest
import hs.kr.entrydsm.application.domain.application.usecase.GetMyApplicationStatusUseCase
import hs.kr.entrydsm.application.domain.application.usecase.SubmitApplicationUseCase
import hs.kr.entrydsm.application.domain.application.usecase.UploadPhotoUseCase
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationStatusResponse
import hs.kr.entrydsm.application.domain.file.presentation.converter.ImageFileConverter
import hs.kr.entrydsm.application.domain.file.presentation.dto.response.UploadImageWebResponse
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/application")
class WebApplicationAdapter(
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val getMyApplicationStatusUseCase: GetMyApplicationStatusUseCase,
    private val submitApplicationUseCase: SubmitApplicationUseCase
) {

    @PostMapping
    fun submitApplication(@RequestBody @Valid request: SubmitApplicationWebRequest) =
        runBlocking { submitApplicationUseCase.execute(request.toSubmitApplicationRequest()) }

    @PostMapping("/photo")
    fun uploadFile(@RequestPart(name = "image") file: MultipartFile): UploadImageWebResponse {
        return UploadImageWebResponse(
            uploadPhotoUseCase.execute(
                file.let(ImageFileConverter::transferTo)
            )
        )
    }

    @GetMapping("/status")
    fun getMyApplicationStatus(): GetApplicationStatusResponse =
        runBlocking { getMyApplicationStatusUseCase.execute() }
}
