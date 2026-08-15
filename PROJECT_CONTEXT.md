# Smart Logistics Platform

## Project Vision
A full-stack logistics marketplace designed around return-load optimization. Businesses post load requirements, and truck drivers discover loads and place competitive bids. Includes real-time GPS tracking and AI-powered driver document verification.

## Architecture
- Frontend: React (Vite) + Tailwind CSS + Leaflet
- Backend: Java 21+ Spring Boot 3.3.x + Spring Data MongoDB + Spring Security (JWT) + Netty-SocketIO
- Microservice: FastAPI (Python) for OCR
- Real-time: Netty-SocketIO on port 5001

## Tech Stack
React, Java 21, Spring Boot, Spring Data MongoDB, MongoDB Atlas, Netty-SocketIO, Python, FastAPI, Tesseract OCR, EasyOCR.

## Repository Structure
- `/smart-logistics-frontend` - React Frontend (Vite)
- `/smart-logistics-backend` - Spring Boot Backend (Maven)
- `/doc_verify_service` - FastAPI Python OCR Service

## User Roles
- Business Owner
- Truck Driver
- Admin

## Authentication
JWT-based authentication with BCrypt password hashing. Spring Security RBAC filter for role protection.

## Marketplace Workflow
Business Creates Load -> Driver Browses Loads -> Driver Places Bid -> Business Accepts Bid -> Trip Created -> Trip Tracked -> Trip Completed.

## Real-time GPS Tracking
Powered by Netty-SocketIO on port 5001 with room-based pub/sub (`driver:start_trip`, `driver:location_update`, `business:join_trip_room`, `business:confirm_delivery`).

## Document Verification
FastAPI microservice running OCR (EasyOCR / Tesseract) for DL, Aadhaar, RC, PUC, Insurance, and Permit verification.

## Database
MongoDB Atlas (collections: `users`, `driverprofiles`, `businessprofiles`, `trucks`, `loads`, `bids`, `trips`, `locationhistories`, `documentstores`, `documents`).
