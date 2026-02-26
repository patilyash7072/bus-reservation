🚌 ##Bus Reservation App

A Bus Reservation Web Application built using:

Spring Boot 3.5.11
Vaadin 24.9
PostgreSQL
Spring Security
Spring Data JPA
Java 21
Maven

This application allows users to manage bus reservations with a modern UI built using Vaadin and a secure backend powered by Spring Boot.

📌 Features

User authentication & authorization (Spring Security)
Bus management
Seat reservation system
Database integration with PostgreSQL
JPA-based persistence layer
Production-ready Vaadin frontend build
Developer-friendly setup with DevTools

🏗️ Tech Stack

Layer	Technology
Backend	Spring Boot 3.5.11
Frontend	Vaadin 24.9
Security	Spring Security
ORM	Spring Data JPA (Hibernate)
Database	PostgreSQL
Build Tool	Maven
Java Version	21


🔎 Configuration Notes

Uses schema: bus_reservation

Default DB:
Database: postgres
Username: postgres
Password: root

⚠️ Important: Change database credentials before deploying to production.

🗄️ Database Setup (PostgreSQL)

Install PostgreSQL
Create database (if not exists):
CREATE DATABASE postgres;
Create schema:
CREATE SCHEMA bus_reservation;

Ensure credentials match application.properties.

▶️ Running the Application

🔹 Run in Development Mode
mvn spring-boot:run

Application starts at:
http://localhost:8080

Browser auto-launch is enabled:
vaadin.launch-browser=true

🚀 Build for Production

To build optimized Vaadin frontend:
mvn clean package -Pproduction

Then run:

java -jar target/Bus-Reservation-App-0.0.1-SNAPSHOT.jar
📦 Maven Dependencies Overview

Key dependencies used:

spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-web
vaadin-spring-boot-starter
postgresql
lombok
spring-boot-starter-test

🔐 Security

The application uses Spring Security for:
Authentication
Authorization
Secured routes

You can configure custom login pages and role-based access.

🧪 Testing

Run tests using:
mvn test

Includes:
Spring Boot Test
Spring Security Test

🛠️ Development Notes

Uses HikariCP (default Spring Boot connection pool)
Uses Hibernate as JPA provider
Lombok is used to reduce boilerplate code
DevTools enabled for hot reload during development

📁 Project Structure (Typical)
src/
 ├── main/
 │   ├── java/com/dss/
 │   │   ├── config/
 │   │   ├── entity/
 │   │   ├── repository/
 │   │   ├── service/
 │   │   └── view/
 │   └── resources/
 │       └── application.properties
 └── test/
⚠️ Production Recommendations

Before deploying:

Change ddl-auto=create → validate or update
Move DB credentials to environment variables
Disable show-sql
Use strong passwords
Enable HTTPS
Configure proper user roles & access control

📄 License

This project is for educational/demo purposes.

👨‍💻 Author

Yash Patil
Developed using Spring Boot + Vaadin ecosystem.
