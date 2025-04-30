# RAG AI Application

A Spring Boot application implementing Retrieval-Augmented Generation (RAG) using Spring AI, OpenAI, and Neo4j for document processing and intelligent querying.

## Overview

This project implements a RAG (Retrieval-Augmented Generation) system that combines document processing capabilities with AI-powered querying. It uses Spring AI for document reading and processing, OpenAI for language model integration, and Neo4j for document storage and retrieval.

## Features

- Document processing and ingestion using Spring AI Tika
- Integration with OpenAI for language model capabilities
- Neo4j-based document storage and retrieval
- WebFlux-based reactive API endpoints
- Spring Boot 3.4.3 with Java 21

## Tech Stack

- **Framework**: Spring Boot 3.4.3
- **Language**: Java 21
- **AI Integration**: Spring AI 1.0.0-M6
- **Document Processing**: Spring AI Tika
- **Language Model**: OpenAI
- **Database**: Neo4j
- **API**: WebFlux (Reactive)
- **Build Tool**: Gradle

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── omnedu/
│   │           └── ai/
│   │               ├── api/         
│   │               ├── config/      
│   │               ├── rag/         
│   │               ├── service/    
│   │               └── RagaiApplication.java
│   └── resources/   
```

## Getting Started

### Prerequisites

- Java 21
- Gradle
- Neo4j database
- OpenAI API key

### Configuration

1. Set up your Neo4j database
2. Configure your OpenAI API key in the application properties
3. Update Neo4j connection details in the application properties

### Building and Running

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

 