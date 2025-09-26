package hs.kr.entrydsm.application.global.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import hs.kr.entrydsm.application.global.storage.AwsCredentialsProperties
import hs.kr.entrydsm.application.global.storage.AwsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AwsProperties::class, AwsCredentialsProperties::class)
class AwsS3Config(
    private val awsProperties: AwsProperties,
    private val awsCredentialsProperties: AwsCredentialsProperties
) {

    @Bean
    fun amazonS3Client(): AmazonS3Client {
        val credentials = BasicAWSCredentials(awsCredentialsProperties.accessKey, awsCredentialsProperties.secretKey)

        return AmazonS3ClientBuilder.standard()
            .withRegion(awsProperties.region)
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .build() as AmazonS3Client
    }
}
