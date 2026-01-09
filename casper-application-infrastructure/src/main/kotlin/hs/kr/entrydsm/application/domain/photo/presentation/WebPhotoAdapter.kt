package hs.kr.entrydsm.application.domain.photo.presentation

import hs.kr.entrydsm.application.domain.file.presentation.converter.ImageFileConverter
import hs.kr.entrydsm.application.domain.file.presentation.dto.response.UploadImageWebResponse
import hs.kr.entrydsm.application.domain.photo.usecase.UploadPhotoUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/photo")
class WebPhotoAdapter(
    private val uploadPhotoUseCase: UploadPhotoUseCase
) {

    @PostMapping
    fun uploadFile(
        @RequestPart(name = "image") file: MultipartFile,
    ): UploadImageWebResponse {
        return UploadImageWebResponse(
            uploadPhotoUseCase.execute(
                file.let(ImageFileConverter::transferTo),
            ),
        )
    }
}