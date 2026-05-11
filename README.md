# ShareIt — Item Sharing Platform

A RESTful backend service for a peer-to-peer item sharing platform. Users can list items they are willing to lend, search for items they need, book them for specific dates, and request items that are not yet available on the platform.

## Features

**Items**
- Add and manage items available for sharing
- Search items by name or description
- View item availability

**Bookings**
- Book an item for specific dates
- Automatic availability blocking during active bookings
- Booking confirmation or rejection by item owner
- Booking status tracking — WAITING, APPROVED, REJECTED, CANCELLED

**Item Requests**
- Create requests for items not yet on the platform
- Other users can respond to requests by adding relevant items
- View own requests and all requests from other users

**Users**
- User registration and profile management

## Tech Stack

- **Java 21**
- **Spring Boot 3.3**
- **Spring Data JPA + Hibernate** — ORM-based data access on server module
- **PostgreSQL** — primary relational database
- **Feign Client** — HTTP communication between gateway and server
- **Docker & Docker Compose** — fully containerized environment
- **Lombok** — boilerplate reduction
- **JUnit 5** — unit and integration tests
- **JaCoCo** — code coverage enforcement (90% line, 60% branch)
- **SpotBugs** — static analysis for bug detection
- **Checkstyle** — enforced code style
- **Maven** — multi-module build

## Architecture

ShareIt is built as a two-module microservice application:

**Gateway** → **Server** → **PostgreSQL**

**Gateway module** (`shareit-gateway`)
- Entry point for all incoming HTTP requests
- Input validation before forwarding to server
- Feign clients for communication with server module
- Shields the server from invalid requests

**Server module** (`shareit-server`)
- Core business logic
- JPA repositories for data persistence
- Domain entities: User, Item, Booking, ItemRequest, Comment
- Service layer with interface/implementation pattern

This separation keeps validation concerns isolated in the gateway and business logic clean in the server.

## Getting Started

### Requirements
- Docker and Docker Compose installed

### Run locally

```bash
git clone https://github.com/alonazrnko/java-shareit.git
cd java-shareit
docker compose up --build
```

The API will be available at `http://localhost:8080`

### Stop the application

```bash
docker compose down
```

### Run tests

```bash
mvn test
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/users` | Create a user |
| GET | `/users/{id}` | Get user by ID |
| POST | `/items` | Add an item |
| GET | `/items` | Get all owner's items |
| GET | `/items/search` | Search available items |
| POST | `/items/{id}/comment` | Leave a comment after booking |
| POST | `/bookings` | Create a booking request |
| PATCH | `/bookings/{id}` | Approve or reject booking |
| GET | `/bookings` | Get user's bookings |
| POST | `/requests` | Create an item request |
| GET | `/requests` | Get own item requests |
| GET | `/requests/all` | Get all requests from other users |

## Testing

- Controller-layer tests in gateway — `BookingControllerTest`, `ItemControllerTest`, `UserControllerTest`, `ItemRequestControllerTest`
- Validation logic verified at gateway level before requests reach the server
- JaCoCo enforces minimum coverage thresholds on every build

## Key Design Decisions

- **Two-module architecture** — gateway handles validation and routing, server handles business logic and persistence; clear separation of concerns with independent Dockerfiles per module
- **Feign Client** — declarative HTTP client makes inter-service communication readable and maintainable
- **Service interface + implementation pattern** — `BookingService` / `BookingServiceImpl` pattern allows easy swapping of implementations and simplifies unit testing
- **JaCoCo + SpotBugs + Checkstyle** — quality gates enforced at build time, not as optional checks
- **Docker Compose** — both modules and PostgreSQL run in isolated containers, reproducible with a single command
