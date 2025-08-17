# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an AI-powered autobiography interview system built with Spring Boot. The system guides users through structured conversations to create personalized autobiographies using a 5-chapter framework with intelligent question generation.

## Core Architecture

### Domain Structure
The application follows Domain-Driven Design with these main domains:
- **episode** - Core interview conversations and episode management
- **book** - Personal book creation and management  
- **communityBook** - Community sharing of books
- **groupbook** - Collaborative group books
- **group** - User groups and membership
- **member** - User authentication and profiles
- **ai** - OpenAI integration for question generation
- **stt** - Speech-to-text using Whisper API
- **rtc** - Real-time communication with LiveKit

### Technology Stack
- **Backend**: Spring Boot 3.5.3 with Java 17
- **Database**: MySQL 8.0 + JPA/Hibernate
- **Cache**: Redis for session management
- **AI Services**: OpenAI GPT for conversations, Whisper for STT
- **RTC**: LiveKit for real-time communication
- **Storage**: AWS S3 for file uploads
- **Authentication**: Spring Security with OAuth2 (Google)

## Development Commands

### Running the Application
```bash
# Start the application
./gradlew bootRun

# Run with Docker Compose (MySQL, Redis, LiveKit)
docker compose up -d
./gradlew bootRun
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.c203.autobiography.domain.auth.*"
```

### Building
```bash
# Build the project
./gradlew build

# Clean and build
./gradlew clean build
```

## Key Features

### Chapter-Based Interview System
The system uses a 5-chapter structure:
1. **Basic Information** - Profile, birth, family background
2. **Growth Process** - Childhood, school years, education
3. **Social Activities** - Career, achievements, failures  
4. **Personal Life** - Relationships, travel, hobbies, health
5. **Reflection & Legacy** - Life lessons, future vision

### Intelligent Question Generation
- **Static Follow-ups**: Pre-defined high-quality questions for consistency
- **Dynamic Follow-ups**: AI-generated personalized questions based on user responses
- **Duplicate Prevention**: AI analyzes previous answers to avoid repetitive questions
- **Progress Tracking**: Real-time chapter and overall progress monitoring

### Multi-Modal Interaction
- **Voice Input**: Speech-to-text using Whisper API
- **Text Chat**: Traditional text-based conversations
- **Real-time Collaboration**: LiveKit integration for group sessions

## Configuration

### Environment Setup
Required environment configuration in `application.properties`:
- **Database**: MySQL connection details
- **Redis**: Cache configuration  
- **AWS S3**: File storage credentials
- **OpenAI**: API key for AI services
- **OAuth2**: Google authentication setup
- **LiveKit**: RTC server configuration

### External Dependencies
- MySQL 8.0 (port 3307)
- Redis (port 6379)
- LiveKit Server (ports 7880, 7881)

## API Structure

### Core Endpoints
- `/api/conversation/*` - Interview conversation management
- `/api/books/*` - Personal book operations
- `/api/community-books/*` - Community book sharing
- `/api/groups/*` - Group management
- `/api/stt/*` - Speech-to-text services
- `/api/rtc/*` - Real-time communication

### Key Features
- **SSE Streaming**: Real-time question delivery via Server-Sent Events
- **File Upload**: Image and audio file handling via S3
- **Authentication**: JWT + OAuth2 integration
- **Progress Tracking**: Chapter-based progress with detailed metrics

## Development Notes

### Database Schema
The application uses JPA entities with 30+ tables covering:
- User management and authentication
- Book and episode structures
- Community features and ratings
- Group collaboration
- Template-based question system

### AI Integration
- OpenAI GPT for dynamic question generation
- Whisper API for speech-to-text conversion
- Context-aware conversation management
- Intelligent duplicate question prevention

### Security
- Spring Security with OAuth2
- JWT token management
- File upload validation
- CORS configuration for web clients

## Testing Strategy

Test files are organized under `src/test/java/` with focus areas:
- Unit tests for service layers
- Integration tests for API endpoints  
- S3 file storage testing
- Authentication flow testing

Main application class: `AutobiographyApplication.java`