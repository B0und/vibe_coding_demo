# Vibe Coding Demo

A full-stack application for managing events and subscriptions with real-time messaging capabilities.

## 🚀 Technology Stack

### Frontend

- **React 18** with TypeScript
- **Vite** for fast development and building
- **TailwindCSS** for styling
- **React Router** for navigation
- **Zustand** for state management
- **TanStack Query** for server state management

### Backend

- **Java 17** with Spring Boot 3.4.1
- **Spring Web** for REST APIs
- **Spring Data JPA** for database operations
- **Spring Kafka** for message processing
- **PostgreSQL** as the primary database

### Infrastructure

- **Docker & Docker Compose** for containerization
- **Apache Kafka** for event streaming
- **PostgreSQL** for data persistence

## 📋 Prerequisites

Before running this application, make sure you have the following installed:

- [Git](https://git-scm.com/)
- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)

## 🛠️ Quick Start

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd vibe_coding_demo
   ```

2. **Start all services**

   ```bash
   docker-compose up --build
   ```

   This single command will:

   - Build and start the React frontend (accessible at http://localhost:3000)
   - Build and start the Spring Boot backend (accessible at http://localhost:8080)
   - Start PostgreSQL database
   - Start Apache Kafka and Zookeeper

3. **Access the application**
   - **Frontend**: http://localhost:3000
   - **Backend API**: http://localhost:8080
   - **Database**: localhost:5432 (vibecodingdemo/vibeuser/vibepass)
   - **Kafka**: localhost:9092

## 🏗️ Project Structure

```
vibe_coding_demo/
├── frontend/                 # React frontend application
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── pages/          # Page components
│   │   └── ...
│   ├── package.json
│   └── Dockerfile.dev
├── backend/                 # Spring Boot backend application
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Configuration files
│   ├── build.gradle        # Gradle build configuration
│   └── Dockerfile
├── docker-compose.yml      # Docker services orchestration
└── README.md              # This file
```

## 🔧 Development

### Frontend Development

```bash
cd frontend
pnpm install
pnpm dev
```

### Backend Development

```bash
cd backend
./gradlew bootRun
```

### Database Access

```bash
# Connect to PostgreSQL
docker exec -it vibe_coding_demo_postgres_1 psql -U vibeuser -d vibecodingdemo
```

## 🧪 Testing

### Frontend Tests

```bash
cd frontend
pnpm test
```

### Backend Tests

```bash
cd backend
./gradlew test
```

## 📝 Environment Variables

The application uses the following environment variables (configured in docker-compose.yml):

### Backend

- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/vibecodingdemo`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092`

### Frontend

- `VITE_API_URL=http://localhost:8080`

## 🚦 Health Checks

- **Backend Health**: http://localhost:8080/actuator/health
- **Database**: Check container logs with `docker-compose logs postgres`
- **Kafka**: Check container logs with `docker-compose logs kafka`

## 🛑 Stopping the Application

```bash
docker-compose down
```

To also remove volumes (database data):

```bash
docker-compose down -v
```

## 📖 Additional Documentation

- [Frontend Documentation](./frontend/README.md)
- [Backend API Documentation](./backend/README.md) (Coming soon)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
