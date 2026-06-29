# SmartCart — E-Commerce Web Application

Full-stack e-commerce platform built with Java Servlets, MySQL, and Apache Tomcat.

## Features
- Secure authentication with BCrypt password hashing
- Session-based login with session fixation prevention
- HikariCP connection pooling for concurrent user handling
- Shopping cart with real-time stock validation
- Personalized product recommendations via AI chatbot
- Role-based routing (Admin / User)
- Admin dashboard for product and user management

## Tech Stack
- Java 17, Jakarta Servlets
- MySQL 8.0
- Apache Tomcat 10
- HikariCP 5.1.0
- BCrypt (jBCrypt 0.4)
- Maven

## Project Structure
- `src/main/java/com/ecommerce/` — Servlets + filters
- `src/main/webapp/` — JSP pages + CSS
- `src/main/resources/` — db.properties (excluded from Git)

## Setup
1. Create MySQL database: `CREATE DATABASE ecommerce;`
2. Run `database/schema.sql`
3. Copy `db.properties.example` and fill in your credentials
4. Build with Maven: `mvn clean package`
5. Deploy WAR to Tomcat 10+
