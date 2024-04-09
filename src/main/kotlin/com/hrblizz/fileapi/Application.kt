package com.hrblizz.fileapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories(basePackages = ["com.hrblizz.fileapi.data.repository"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
