# SpeechSync AI — Real-Time Speech Transcription

A web application for real-time speech-to-text transcription, with support for multilingual speech, text-to-speech playback, and transcription history management.

## Features

- Real-time speech transcription using the Gemini Live API.
- Browser Speech Recognition as a fallback option.
- Language selection for supported languages.
- Text-to-speech playback with Listen and Stop Audio controls.
- Edit transcript text before saving.
- Create, view, update, and delete saved transcriptions.
- MySQL database persistence through Spring Boot REST APIs.
- Angular frontend with a responsive dashboard.

## Technology Stack

**Frontend:** Angular, TypeScript, HTML, CSS  
**Backend:** Java 21, Spring Boot  
**Database:** MySQL  
**Speech-to-Text:** Gemini Live API and browser Web Speech API fallback  
**Text-to-Speech:** Browser Speech Synthesis API  
**API Documentation:** Springdoc OpenAPI, if configured

## Architecture

1. Angular captures microphone audio and streams it to Gemini Live for transcription.
2. Gemini returns transcription results to the frontend.
3. Users can review or edit the transcript before saving.
4. Spring Boot REST APIs store and manage transcripts in MySQL.
5. Users can browse, edit, and delete saved transcription records.
6. Browser speech synthesis can read the transcript aloud.

## Prerequisites

- Java 21
- Node.js and npm
- MySQL
- Google AI Studio API key with access to the configured Gemini Live model
- Angular CLI, if running `ng serve` directly

## Configuration

Create a MySQL database:

```sql
CREATE DATABASE speech_sync_db;
```

Configure the database connection in your Spring Boot application. Use environment variables for credentials:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/speech_sync_db}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Set your Gemini API key as an environment variable before starting the backend:

**Windows PowerShell**

```powershell
$env:GEMINI_API_KEY="YOUR_API_KEY"
```

In `application.properties`:

```properties
gemini.api-key=${GEMINI_API_KEY:}
```

Never commit real API keys, database passwords, or other secrets.

## Run the Application

### Backend

From the Spring Boot backend directory:

```bash
mvn spring-boot:run
```

If your project includes the Maven wrapper, you can use it instead:

```powershell
.\mvnw.cmd spring-boot:run
```

### Frontend

From the Angular frontend directory:

```bash
npm install
ng serve
```

Open `http://localhost:4200`.

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/transcriptions` | Create a transcript |
| GET | `/api/transcriptions` | List transcript history |
| GET | `/api/transcriptions/{id}` | Retrieve a transcript |
| PUT | `/api/transcriptions/{id}` | Update a transcript |
| DELETE | `/api/transcriptions/{id}` | Delete a transcript |
| POST | `/api/gemini/token` | Request a short-lived Gemini session token |

If Springdoc OpenAPI is configured, visit `/swagger-ui/index.html` to explore the REST API.

## Testing

Run backend tests from the backend directory:

```bash
mvn test
```

## Limitations

- Recognition quality and supported languages depend on the selected speech engine and its language capabilities.
- Gemini Live requires internet connectivity and valid API access.
- Browser speech recognition and speech synthesis depend on browser and operating-system support.
- The Gemini integration uses a hosted API; it is not a self-hosted open-source speech model.

## Future Improvements

- Automated integration tests for real-time transcription.
- Improved reconnect and network error handling.
- Deployment with secure environment-variable configuration.
- Additional multilingual transcription testing.

## Author

Developed as a speech-to-text application project.
