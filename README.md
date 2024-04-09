# File API

[![Build & Test](https://github.com/ktenman/file-api/actions/workflows/ci.yml/badge.svg)](https://github.com/ktenman/file-api/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ktenman_file-api&metric=alert_status)](https://sonarcloud.io/summary/overall?id=ktenman_file-api)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ktenman_file-api&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=ktenman_file-api)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ktenman_file-api&metric=security_rating)](https://sonarcloud.io/project/security_hotspots?id=ktenman_file-api)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ktenman_file-api&metric=sqale_rating)](https://sonarcloud.io/project/issues?resolved=false&id=ktenman_file-api)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ktenman_file-api&metric=code_smells)](https://sonarcloud.io/code?id=ktenman_file-api)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=ktenman_file-api&metric=sqale_index)](https://sonarcloud.io/summary/overall?id=ktenman_file-api)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ktenman_file-api&metric=vulnerabilities)](https://sonarcloud.io/code?id=ktenman_file-api)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=ktenman_file-api&metric=duplicated_lines_density)](https://sonarcloud.io/summary/overall?id=ktenman_file-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ktenman_file-api&metric=coverage)](https://sonarcloud.io/summary/overall?id=ktenman_file-api)

The File API is a RESTful service that allows users to upload, retrieve, and manage files. It provides endpoints for
uploading files with metadata, retrieving file metadata and content, and deleting files.

## Features

- Upload files with associated metadata
- Retrieve file metadata and content by token
- Delete files by token
- Automatically remove expired files based on the expiration time
- Secure file storage using MinIO object storage
- Integration with MongoDB for storing file metadata
- Authentication using Basic Auth
- API documentation using Swagger
- Logging and exception handling
- Integration and unit tests using JUnit 5 and Mockito
- Distributed locking using Redis

## Technologies Used

- Kotlin
- Spring Boot
- Spring Data MongoDB
- Spring Security
- MinIO
- Swagger
- JUnit 5
- Mockito
- Testcontainers
- Redis

## Getting Started

### Prerequisites

Before you begin, ensure your system meets the following requirements:

* Java: v21.0.2
* Maven: v3.9.5
* Docker: v25.0.3
* Docker Compose: v2.24.6

### Installation

1. Clone the repository:
    ```shell
    git clone https://github.com/ktenman/file-api.git
    ```

2. Navigate to the project directory:
    ```shell
    cd file-api
    ```

3. Build and run the project using Docker Compose:
    ```shell    
    docker-compose -f docker-compose.yml up
    ```

### Configuration

The application uses the following configuration properties:

* `spring.data.mongodb.uri`: MongoDB connection URI
* `minio.url`: MinIO server URL
* `minio.access-key`: MinIO access key
* `minio.secret-key`: MinIO secret key
* `minio.bucket-name`: MinIO bucket name for storing files

You can modify these properties in the application.properties file located in src/main/resources.

### API Documentation

The API documentation is available using Swagger. Once the application is running, you can access the Swagger UI
at http://localhost:6011/docs or http://localhost:6011/swagger-ui/index.html

### Running Tests

To run the unit and integration tests, use the following command:

```shell
mvn test
```

## Usage

### Authentication

The File API uses Basic Authentication for securing the endpoints. The default username is `admin` and the password is
`hunter2`. You can modify the authentication credentials in the `ApiAuthenticationProvider` class.

### Endpoints

* `POST /files`: Upload a file with metadata
* `GET /files`: Retrieve file metadata by tokens
* `GET /files/{token}/content`: Download a file by token
* `GET /files/{token}/meta`: Retrieve file metadata by token
* `DELETE /files/{token}`: Delete a file by token

For detailed information about the request and response formats, please refer to the API documentation.

---
This README file provides an overview of the File API project, including its features, technologies used, installation
instructions, configuration details, API documentation, and usage guidelines. The project aims to provide a simple and
secure file management service with support for file uploads, retrieval, and guidelines. It also demonstrates the
integration of various technologies and practices, such as Spring Boot, MongoDB, MinIO, Swagger, and Testcontainers.
