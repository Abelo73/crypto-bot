# Crypto Trading Platform Backend

A production-ready, non-custodial crypto trading platform backend with Bybit Spot Exchange integration. Built with Spring Boot 3.2, Java 17, and PostgreSQL.

## Features

- **Non-Custodial**: Connects directly to exchange via API keys.
- **Secure API Key Management**: AES-256-GCM encryption for exchange credentials.
- **Exchange-Agnostic Architecture**: Adapter pattern allows easy addition of new exchanges.
- **Reactive Bybit Integration**: Uses Spring WebFlux WebClient for non-blocking REST API calls.
- **Spot Trading**: Support for MARKET and LIMIT orders.
- **Real-time Sync**: Synchronize balances and track order lifecycles.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2**
- **Spring Data JPA** & **PostgreSQL**
- **Spring WebFlux WebClient** (for external APIs)
- **Flyway** (database migrations)
- **MapStruct** (entity/DTO mapping)
- **SpringDoc OpenAPI** (Swagger)
- **AES-256-GCM** (encryption)

## Project Structure

```text
src/main/java/com/cryptobot/
├── adapter/         # Exchange integration (Bybit, etc.)
├── api/             # REST API Layer (Controllers, DTOs)
├── config/          # Spring Configuration
├── domain/          # Core Business Logic (Entities, VOs)
├── repository/      # Data Persistence Layer
├── security/        # Encryption and Security Logic
└── service/         # Application Service Layer
```

## Setup & Running

### Prerequisites

- Java 21 (Optimized for Java 21)
- Docker & Docker Compose
- Maven 3.8+

### 1. Environment Configuration

The application uses the `dev` profile for local testing. A default encryption key for AES-GCM is pre-configured in `src/main/resources/application-dev.yml`.

```bash
# Database (Note: Port 5433 is used to avoid local conflicts)
DB_HOST=localhost
DB_PORT=5433
DB_NAME=cryptobot_db
DB_USERNAME=cryptobot
DB_PASSWORD=cryptobot123
```

### 2. Start Infrastructure

```bash
docker-compose up -d
```

### 3. Seed Test Data (Optional)

Populate your database with a test user, dummy API keys, and history for immediate testing:

```bash
docker exec -i crypto-bot-postgres psql -U cryptobot -d cryptobot_db < seed-data.sql
```

### 4. Build and Run

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will be available at `http://localhost:8080`.

## Testing Guide

### Postman Collections
A comprehensive Postman collection is provided: **[`crypto-bot-api-collection.json`](./crypto-bot-api-collection.json)**. Import this into Postman to start testing.

### A. Testing with Seed Data
1.  **Get User**: Call `User Management -> Get User`. (Expects `userId: 1`)
2.  **Check Balances**: Call `Balances -> Get All User Balances`.
3.  **View Logs**: Observe background tasks in the terminal (Order Sync).

### B. Live Exchange Testing (Bybit Testnet)
To test real trading and synchronization:
1.  **Add API Key**: Use `API Key Management -> Add API Key` in Postman. Provide your **real Bybit Testnet** credentials.
2.  **Sync Balances**: Call `Balances -> Refresh Balances`. This will fetch real data from Bybit.
3.  **Place Orders**: Use `Trading -> Place LIMIT Order`. Observe the order being persisted in your local DB and updated automatically by the background sync task.

## API Documentation

Access Swagger UI (OpenAPI 3) at: `http://localhost:8080/swagger-ui.html`

## Development

### Running Tests

```bash
./mvnw test
```

### Database Migrations

Migrations are located in `src/main/resources/db/migration` and managed by Flyway.

## Security Considerations

- **Encryption**: API keys are encrypted at rest with AES-256-GCM.
- **Resilience**: The adapter layer implements exponential backoff and custom timeouts (30s) for high reliability.
- **Validation**: Strict pre-order validation ensures symbols and quantities meet exchange constraints.
- **Non-Custodial**: No user private keys or seed phrases are stored.
