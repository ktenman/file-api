# File API Project Summary

The File API project is a Kotlin-based Spring Boot application that allows users to manage files and their associated
metadata. It integrates with MongoDB for storing file metadata and MinIO for storing the actual files. The project
demonstrates the usage of various technologies and practices, including Testcontainers for integration testing.

## Key Points

1. The API endpoints in this project could be improved to better align with REST best practices.
2. The code structure and formatting lack clarity on certain design choices.
3. Integration tests are implemented using the `@IntegrationTest` annotation, which sets up MongoDB and MinIO containers
   using Testcontainers.
4. A scheduled job (`FileCleanupJob`) is implemented to remove expired files and their metadata daily at midnight.

## Challenges and Learnings

The most time-consuming part of the assignment was migrating to the latest Spring Boot version, which required
researching compatibility issues, examining documentation, and managing dependencies to ensure a smooth migration
process.

Throughout the project, several Kotlin features and concepts were learned and applied, such as:

* Using data classes for representing file metadata and request/response DTOs
* Utilizing companion objects for defining constants and utility methods
* Working with iterative functions like `associateBy`, `associate`, `filter`, and `map`
* Configuring the `@IntegrationTest` annotation using the `apply` method
* Also other Kotlin features
  like `lateinit`, `when`, `require`, `var`, `val`, `invoke`, `fun`, `companion`, `object`, `data`, `open`, `const`, `?`, `!!`, `init`, `runBlocking`, `reified`, `inline`
* Interacting with MinIO using the MinioClient library
* Defining multipart requests with JSON metadata in the API documentation using Swagger annotations
* Learned how to use validation annotations like `@NotBlank` as `@field:NotEmpty` in Kotlin
## Areas for Improvement

While the project provides a functional implementation, there are several areas that could be enhanced:

1. Migrating the authentication mechanism to use **JWT** tokens instead of basic authentication
2. Associating uploaded files with user information for better tracking and management
3. Implementing **distributed locking** or caching using Redis to improve performance and handle concurrency
4. Adding **idempotency keys** to API requests to prevent duplicate file uploads or modifications
5. Integrating SonarCloud into the GitHub pipeline for code quality analysis and continuous improvement
6. The exception handling could be improved in the StorageService class to provide more informative error messages
7. The **Pagenation** and **Sorting** could be implemented in the FileService class to handle large datasets
8. Implementing a rate-limiting mechanism to prevent abuse and ensure fair usage of the API
9. Enhancing the API documentation with more detailed descriptions, examples, and error responses
10. Implementing a file versioning system to track changes and allow users to revert to previous versions
11. Adding support for file search and filtering based on metadata fields
12. Implementing a file sharing feature to allow users to share files with others securely
13. Adding support for file encryption and decryption to enhance security and privacy
14. Implementing a file compression feature to reduce storage space and improve transfer speeds
15. Adding support for file streaming to enable users to download large files efficiently
16. Implementing a file preview feature done with AI/ML to generate previews for various file types

## Conclusion

The File API project serves as a valuable learning experience, showcasing the integration of Kotlin and Spring Boot with
storage systems like MongoDB and MinIO. It provides hands-on practice with various Kotlin features and demonstrates the
usage of Testcontainers for integration testing.

However, the project also highlights the importance of following REST best practices, maintaining clear code structure
and formatting, and implementing additional features to enhance security, performance, and user experience.

Overall, this project lays a foundation for working with Kotlin, Spring Boot, and MongoDB, while also identifying areas
for improvement and further exploration in future iterations.
