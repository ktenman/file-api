package com.hrblizz.fileapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig {

    companion object {
        const val ONE_MONTH_CACHE: String = "one-month-cache"
        const val GET_FILE_METADATA: String = "get-file-metadata"
        private val DEFAULT_TTL: Duration = Duration.ofMinutes(1)
    }

    @Bean
    fun cacheManager(
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): RedisCacheManager {
        val cacheConfigurations: MutableMap<String, RedisCacheConfiguration> = HashMap()
        cacheConfigurations[GET_FILE_METADATA] = defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(99))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(objectMapper)
                )
            )
        cacheConfigurations[ONE_MONTH_CACHE] = defaultCacheConfig()
            .entryTtl(Duration.ofDays(30))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(objectMapper)
                )
            )
        val defaultConfig = defaultCacheConfig()
            .entryTtl(DEFAULT_TTL)
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(objectMapper)
                )
            )
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

}
