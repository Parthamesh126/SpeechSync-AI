
# SpeechSync AI — Speech-to-Text Application

SpeechSync AI is a web application that converts speech into text and stores transcription history in a MySQL database. Users can record speech, select a recognition language, edit transcripts, and manage saved records through a dashboard.

## Features

- Speech recognition using the browser's Web Speech API.
- Language selection for Hindi, English, Marathi, and other configured languages.
- Live/interim transcription where supported by the browser.
- Save and view transcription history.
- Create, read, update, and delete transcription records.
- REST API built with Spring Boot and Java 21.
- MySQL persistence using Spring Data JPA.
- Input validation and centralized exception handling.
- Swagger UI and OpenAPI API documentation.
- Unit tests using JUnit 5 and Mockito.

## Technology Stack

### Frontend
- Angular
- TypeScript
- HTML and CSS
- Browser Web Speech API

### Backend
- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- Maven
- Bean Validation
- Springdoc OpenAPI

### Database
- MySQL

## Project Structure

```text
SpeechSync-AI/
├── speech-sync-ui/
│   └── src/
├── speech-engine/
│   └── # Optional experimental Python service
├── src/
│   ├── main/
│   └── test/
└── README.md
```

Adjust the backend paths above to match your actual project layout.

## Prerequisites

- Java 21
- Node.js and npm
- Angular CLI compatible with your Angular project
- MySQL Server
- Maven, or the included Maven wrapper

## Database Configuration

Create the database:

```sql
CREATE DATABASE speech_sync_db;
```

Configure your backend's `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/speech_sync_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

Set `DB_USERNAME` and `DB_PASSWORD` in your environment before starting the backend. Never commit real credentials.

## Run the Backend

From the backend project directory:

```powershell
.\mvnw.cmd spring-boot:run
```

If Maven is installed but the wrapper is unavailable:

```powershell
mvn spring-boot:run
```

The backend runs on `http://localhost:8080` unless configured otherwise.

## Run the Frontend

Open another terminal:

```powershell
cd speech-sync-ui
npm install
ng serve
```

Open `http://localhost:4200`.

Allow microphone access when prompted. Speech recognition depends on browser support and may rely on a browser-provided remote service.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transcriptions` | Create a transcription |
| GET | `/api/transcriptions` | List transcription history |
| GET | `/api/transcriptions/{id}` | Get a transcription |
| PUT | `/api/transcriptions/{id}` | Update a transcription |
| DELETE | `/api/transcriptions/{id}` | Delete a transcription |

## API Documentation

When the backend is running:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

## Run Tests

From the backend project directory:

```powershell
.\mvnw.cmd test
```

## Limitations

- Speech recognition support and accuracy depend on the browser and selected language.
- Mixed-language recognition accuracy can vary.
- Browser speech recognition is not the same as a self-hosted open-source transcription engine.

## Future Improvements

- Integrate and deploy an open-source speech recognition model.
- Improve continuous multilingual transcription.
- Add authentication and user-specific transcription history.
- Deploy the frontend, backend, and database.

## License

Add an appropriate open-source license if you intend to publish the project for reuse.
