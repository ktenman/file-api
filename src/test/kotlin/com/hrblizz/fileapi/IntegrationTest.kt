package com.hrblizz.fileapi

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.containers.MongoDBContainer

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [IntegrationTest.Initializer::class])
annotation class IntegrationTest {
    companion object {
        const val DEFAULT_USERNAME = "admin"
        const val DEFAULT_PASSWORD = "hunter2"
        const val DEFAULT_ROLE = "USER"
        private const val MONGO_DB_NAME = "files"
        private const val MINIO_ACCESS_KEY = "minioaccess"
        private const val MINIO_SECRET_KEY = "miniosecret"
        private const val BUCKET_NAME = "test-bucket"
        private const val MINIO_HOST = "127.0.0.1"

        private val MONGO_DB_CONTAINER = MongoDBContainer("mongo:latest")
            .withExposedPorts(27017)
            .apply { start() }

        private val MINIO_CONTAINER = MinIOContainer("minio/minio:latest")
            .withExposedPorts(9000, 9001)
            .withUserName(MINIO_ACCESS_KEY)
            .withPassword(MINIO_SECRET_KEY)
            .withNetworkAliases(MINIO_HOST)
            .apply { start() }
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            val minioUrl = "http://$MINIO_HOST:${MINIO_CONTAINER.getMappedPort(9000)}"
            TestPropertyValues.of(
                "spring.data.mongodb.uri=${MONGO_DB_CONTAINER.connectionString}/$MONGO_DB_NAME",
                "minio.url=$minioUrl",
                "minio.access-key=$MINIO_ACCESS_KEY",
                "minio.secret-key=$MINIO_SECRET_KEY",
                "minio.bucket-name=$BUCKET_NAME"
            ).applyTo(applicationContext.environment)

            val minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(MINIO_ACCESS_KEY, MINIO_SECRET_KEY)
                .build()

            MinIOUtils.createBucketIfNotExists(minioClient, BUCKET_NAME)
        }
    }

    object MinIOUtils {
        private fun MinioClient.bucketExists(bucketName: String): Boolean =
            bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())

        private fun MinioClient.createBucket(bucketName: String) {
            if (!bucketExists(bucketName)) {
                makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
            }
        }

        fun createBucketIfNotExists(minioClient: MinioClient, bucketName: String) {
            minioClient.createBucket(bucketName)
        }
    }
}
