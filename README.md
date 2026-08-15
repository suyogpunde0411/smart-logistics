# 🚛 Smart Logistics Platform
### *AI-Powered Freight Marketplace, Return-Load Optimization & Real-Time Fleet Tracking*

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk&logoColor=white)](https://jdk.java.net/21/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React 19](https://img.shields.io/badge/React-19-blue.svg?logo=react&logoColor=white)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8.1-646CFF.svg?logo=vite&logoColor=white)](https://vitejs.dev/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.111-009688.svg?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![MongoDB Atlas](https://img.shields.io/badge/MongoDB-Atlas-47A248.svg?logo=mongodb&logoColor=white)](https://www.mongodb.com/atlas)
[![Netty-SocketIO](https://img.shields.io/badge/Socket.IO-Netty_2.0-010101.svg?logo=socketdotio&logoColor=white)](https://socket.io/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3.4-38B2AC.svg?logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![Docker Ready](https://img.shields.io/badge/Docker-Ready-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 📌 Table of Contents
- [Overview & The Return-Trip Problem](#-overview--the-return-trip-problem)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [AI Document Verification Pipeline](#-ai-document-verification-pipeline)

---

## 📖 Overview & The Return-Trip Problem

In the modern freight and supply chain industry, commercial truck drivers often face high operating costs due to **deadhead miles**—the journey back to origin with an empty trailer after delivering cargo. 

**Smart Logistics Platform** solves this fundamental logistics inefficiency by providing an open, real-time marketplace engineered for **return-load discovery and optimization**:

1. **Businesses** post shipments and load requirements specifying origin, destination, cargo type, tonnage, and budget.
2. **Truck Drivers** search for matching freight along their return routes and place competitive bids.
3. **Automated Contracts & Live Execution**: Once a bid is accepted, an active trip is initiated with turn-by-turn **real-time GPS streaming** and milestone verification.
4. **Instant Onboarding**: Trust is established instantly via an automated **AI-powered OCR microservice** that validates driver licenses, vehicle RCs, PUCs, insurance policies, and permits.

---

## 🌟 Key Features

### 📦 1. Return-Load Marketplace & Competitive Bidding
- Multi-parameter load discovery (route origin/destination, cargo weight, freight category, pricing).
- Dynamic bidding mechanism allowing drivers to negotiate and quote competitive rates.
- Multi-bid comparison dashboard for businesses to evaluate drivers based on rate, ratings, and vehicle compliance.

### 📍 2. High-Performance Real-Time GPS Tracking
- Powered by high-throughput **Netty-SocketIO** for non-blocking telemetry streaming.
- Room-based pub/sub architecture (`tripId` rooms) ensuring low latency updates between drivers and tracking businesses.
- Real-time **Leaflet interactive map** visualizing driver breadcrumbs, speed, timestamps, and route milestones.
- Immutable location history stored in MongoDB for trip auditing and delivery dispute resolution.

### 🤖 3. AI-Powered OCR Document Verification
- Autonomous KYC and vehicle compliance pipeline running on a dedicated **FastAPI Python microservice**.
- Dual-engine OCR with **PyTesseract** and **EasyOCR** for Indian identification and vehicular documents.
- Automatic extraction and verification for:
  - **Identity**: Aadhaar Card (front/back), Driving License (DL).
  - **Fleet Compliance**: Registration Certificate (RC), Pollution Under Control (PUC), Commercial Insurance, State & National Carriage Permits.

### 👥 4. Role-Based Access Control (RBAC) & Enterprise Security
- Stateless **Spring Security 6** architecture secured with **JWT (JSON Web Tokens)**.
- Strictly isolated workflows for:
  - **Truck Drivers**: Fleet management, load discovery, bidding, live trip execution.
  - **Businesses**: Load creation, bid acceptance, real-time tracking, proof of delivery confirmation.
  - **Platform Admins**: System-wide auditing, manual verification overrides, platform analytics.

### 🌐 5. Modern UI & Localization (i18n)
- Responsive frontend built with **React 19**, **Vite**, and **Tailwind CSS**.
- Multi-language support (**English**, **Hindi**, and regional languages) through `i18next` to empower truck drivers across diverse regions.

---

## 🏗 System Architecture

```mermaid
flowchart TD
    subgraph ClientLayer["🖥️ Frontend Client"]
        UI["React 19 + Vite + Tailwind CSS"]
        LeafletMap["Leaflet Map Engine"]
        SocketClient["Socket.IO Client"]
    end

    subgraph BackendLayer["⚙️ Spring Boot Core Backend"]
        Security["Spring Security 6 (JWT Filter & RBAC)"]
        RESTControllers["REST Controllers (/api/v1/*)"]
        NettySocket["Netty-SocketIO Server"]
        Services["Business / Driver / Auth Services"]
        DocClient["DocVerifyClient (REST Client)"]
    end

    subgraph AIServiceLayer["🧠 AI / OCR Microservice"]
        FastAPI["FastAPI App (Python 3.10+)"]
        OCREngine["Tesseract & EasyOCR Pipeline"]
        DocExtractors["DL / Aadhaar / RC / PUC Extractors"]
    end

    subgraph DatabaseLayer["🗄️ Database & Storage"]
        MongoDB[("MongoDB Atlas Database")]
    end

    %% Client Interactions
    UI -->|HTTPS / REST API| Security
    Security --> RESTControllers
    RESTControllers --> Services
    Services --> MongoDB
    
    SocketClient <-->|WebSocket GPS Telemetry| NettySocket
    NettySocket --> Services
    
    %% AI Verification Flow
    Services -->|Multipart Document Upload| DocClient
    DocClient -->|HTTP REST| FastAPI
    FastAPI --> OCREngine
    OCREngine --> DocExtractors
    DocExtractors -->|Extracted JSON Metadata| DocClient
```

---

## 💻 Tech Stack

| Layer | Technologies |
|---|---|
| **Frontend** | React 19, Vite, Tailwind CSS, Leaflet Maps, Axios, Lucide React, i18next, Socket.io-client |
| **Backend Core** | Java 21, Spring Boot 3.3.4, Spring Security 6, Spring Data MongoDB, Lombok, Dotenv Java |
| **Real-time Server** | Netty-SocketIO 2.0.12 (Asynchronous event-driven network application framework) |
| **AI / OCR Microservice** | Python 3.10+, FastAPI, Uvicorn, PyTesseract, EasyOCR, Pillow, PDFPlumber, Pydantic v2 |
| **Database** | MongoDB Atlas (Multi-document collections, indexes, and location history) |
| **DevOps & Containerization** | Docker, Docker Compose, Multi-Stage Builds |

---

## 🔍 AI Document Verification Pipeline

The **Document Verification Microservice** automatically parses identity and vehicular documents to accelerate driver onboarding while preventing fraud:

```
Driver Uploads Docs ──► Spring Boot API ──► FastAPI OCR Pipeline ──► Validation & Score ──► Driver Verified ✅
```

### Supported Documents & Validations:
- **Driving License (DL)**: License number, driver name, validity dates, vehicle class endorsements.
- **Aadhaar Card**: 12-digit UID masking, name, DOB, gender, and address extraction.
- **Vehicle RC**: Registration number, chassis number, engine number, vehicle class, and gross vehicle weight (GVW).
- **PUC Certificate**: Emission norms compliance, test date, and expiry validity.
- **Commercial Insurance**: Policy number, insurer name, and active coverage window.
- **State / National Permits**: Permit authorization number and All-India validity dates.

---

<p align="center">
  Built with ❤️ for smarter, greener, and efficient logistics.
</p>
