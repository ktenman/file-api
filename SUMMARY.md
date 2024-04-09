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

1. **Implement JWT Authentication:** Migrate the authentication mechanism from basic authentication to JWT tokens for
   enhanced security and better user management.
2. **Improve API Design:** Refactor the API endpoints to better align with REST best practices, ensuring clear and
   consistent naming conventions, proper HTTP methods, and meaningful status codes.
3. **Enhance Exception Handling:** Improve the exception handling in the `StorageService` class to provide more
   informative error messages and ensure appropriate HTTP status codes are returned for different error scenarios.
4. **Add Pagination and Sorting:** Implement pagination and sorting functionality in the `FileService` class to handle
   large datasets efficiently and provide a better user experience when retrieving file metadata.
5. **Implement Rate Limiting:** Add a rate-limiting mechanism to prevent abuse and ensure fair usage of the API,
   protecting against excessive requests from a single client.
6. **Enhance API Documentation:** Improve the API documentation by providing more detailed descriptions, examples, and
   error responses using Swagger annotations, making it easier for developers to understand and integrate with the API.
7. **Associate Files with Users:** Modify the file upload process to associate uploaded files with user information,
   allowing for better tracking, management, and access control based on user roles and permissions.
8. **Implement Idempotency Keys:** Add support for idempotency keys in API requests to prevent duplicate file uploads or
   modifications, ensuring data consistency and reliability.
9. **Add File Search and Filtering:** Implement file search and filtering functionality based on metadata fields,
   allowing users to easily find and retrieve specific files based on their attributes.
10. **Improve Code Structure and Formatting:** Refactor the codebase to improve clarity, maintainability, and adherence
    to coding standards. This includes consistent naming conventions, proper indentation, and clear separation of
    concerns.

## Conclusion

The File API project serves as a valuable learning experience, showcasing the integration of Kotlin and Spring Boot with
storage systems like MongoDB and MinIO. It provides hands-on practice with various Kotlin features and demonstrates the
usage of Testcontainers for integration testing.

However, the project also highlights the importance of following REST best practices, maintaining clear code structure
and formatting, and implementing additional features to enhance security, performance, and user experience.

Overall, this project lays a foundation for working with Kotlin, Spring Boot, and MongoDB, while also identifying areas
for improvement and further exploration in future iterations.
