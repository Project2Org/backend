# Backend Setup

This backend is built with:
- Spring Boot
- Java 21
- Gradle

Follow these steps to get the backend running with Docker

## Setup

Make sure the following are installed:

-   Java 21
-   Docker
-   Git

## Clone the Repository

    git clone <repository-url>
    cd backend

## Build the Application

Mac / Linux:

    ./gradlew build

Windows:

    .\gradlew.bat build

## Build the Docker Image

From the `backend` directory run:

    docker build -t calendar-backend .

## Run the Backend

    docker run -p 8080:8080 calendar-backend

The backend will be available at:

    http://localhost:8080
