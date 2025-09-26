package hs.kr.entrydsm.application.global.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import hs.kr.entrydsm.application.global.storage.AwsCredentialsProperties
import hs.kr.entrydsm.application.global.storage.AwsProperties
import hs.kr.entrydsm.application.global.storage.AwsRegionProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AwsProperties::class, AwsCredentialsProperties::class)
class AwsS3Config(
    private val awsCredentialsProperties: AwsCredentialsProperties,
    private val awsRegionProperties: AwsRegionProperties
) {

    @Bean
    fun amazonS3Client(): AmazonS3Client {
        val credentials = BasicAWSCredentials(awsCredentialsProperties.accessKey, awsCredentialsProperties.secretKey)

        return AmazonS3ClientBuilder.standard()
            .withRegion(awsRegionProperties.static)
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .build() as AmazonS3Client
    }
}
