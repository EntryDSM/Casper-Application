package hs.kr.entrydsm.application.domain.application.presentation

import hs.kr.entrydsm.application.domain.application.presentation.dto.request.SubmissionApplicationWebRequest
import hs.kr.entrydsm.application.domain.application.presentation.dto.request.toSubmissionApplicationRequest
import hs.kr.entrydsm.application.domain.application.usecase.GetMyApplicationStatusUseCase
import hs.kr.entrydsm.application.domain.application.usecase.SubmitApplicationUseCase
import hs.kr.entrydsm.application.domain.application.usecase.UploadPhotoUseCase
import hs.kr.entrydsm.application.domain.application.usecase.dto.response.GetApplicationStatusResponse
import hs.kr.entrydsm.application.domain.file.presentation.converter.ImageFileConverter
import hs.kr.entrydsm.application.domain.file.presentation.dto.response.UploadImageWebResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid

@RestController
@RequestMapping("/application")
class WebApplicationAdapter(
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val getMyApplicationStatusUseCase: GetMyApplicationStatusUseCase,
    private val submitApplicationUseCase: SubmitApplicationUseCase
) {

    @PostMapping
    fun submitApplication(@RequestBody @Valid request: SubmissionApplicationWebRequest) =
        submitApplicationUseCase.execute(request.toSubmissionApplicationRequest())

    @PostMapping("/photo")
    fun uploadFile(@RequestPart(name = "image") file: MultipartFile): UploadImageWebResponse {
        return UploadImageWebResponse(
            uploadPhotoUseCase.execute(
                file.let(ImageFileConverter::transferTo)
            )
        )
    }

    @GetMapping("/status")
    fun getMyApplicationStatus(): GetApplicationStatusResponse = getMyApplicationStatusUseCase.execute()
}
