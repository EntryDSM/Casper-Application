package hs.kr.entrydsm.application.global.storage

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.internal.Mimetypes
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import hs.kr.entrydsm.domain.file.spi.GenerateFileUrlPort
import hs.kr.entrydsm.domain.file.spi.UploadFilePort
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.util.Date

@Component
class AwsS3Adapter(
    private val amazonS3Client: AmazonS3Client,
    private val awsProperties: AwsProperties
) : UploadFilePort, GenerateFileUrlPort {

    companion object {
        const val EXP_TIME = 1000 * 60 * 2
    }

    override fun upload(file: File, path: String): String {
        runCatching { inputS3(file, path) }
            .also { file.delete() }

        return getS3Url(path)
    }

    private fun inputS3(file: File, path: String) {
        try {
            val inputStream = file.inputStream()
            val objectMetadata = ObjectMetadata().apply {
                contentLength = file.length()
                contentType = Mimetypes.getInstance().getMimetype(file)
            }

            amazonS3Client.putObject(
                PutObjectRequest(
                    awsProperties.bucket,
                    path,
                    inputStream,
                    objectMetadata
                ).withCannedAcl(CannedAccessControlList.PublicRead)
            )
        } catch (e: IOException) {
            throw IllegalArgumentException("File Exception")
        }
    }

    private fun getS3Url(path: String): String {
        return amazonS3Client.getUrl(awsProperties.bucket, path).toString()
    }

    override fun generateFileUrl(fileName: String, path: String): String {
        val expiration = Date().apply {
            time += EXP_TIME
        }
        return amazonS3Client.generatePresignedUrl(
            GeneratePresignedUrlRequest(
                awsProperties.bucket,
                "${path}$fileName"
            ).withMethod(HttpMethod.GET).withExpiration(expiration)
        ).toString()
    }
}