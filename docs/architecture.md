# Architecture

## 1. Overview

The Subscription Billing System is a Spring Boot backend application for managing customers, subscriptions, invoices, payments, receivables and credit notes.

The application exposes REST APIs for the main billing workflows and uses role-based access control for billing administrators and account managers.

## 2. Architecture Style

The application follows a layered architecture:

- Controller layer – handles HTTP requests and responses.
- Service layer – contains business logic and validation.
- Repository layer – handles database access.
- Model/Entity layer – represents application data.
- DTO layer – represents request and response data where applicable.

This separation keeps business logic independent from HTTP handling and database operations.

## 3. Main Components

### Authentication and Authorization

The authentication flow validates the user and stores the logged-in user information and role in the session.

The application supports:

- BILLING_ADMIN
- ACCOUNT_MANAGER

Role-based checks are applied to protected operations.

### Subscription Management

The subscription module supports:

- Creating subscriptions
- Updating subscriptions
- Archiving subscriptions
- Restoring subscriptions
- Managing collaborators
- Controlling subscription access

### Invoice Management

The invoice module supports:

- Creating invoices
- Listing invoices
- Searching invoices
- Filtering by status
- Filtering overdue invoices
- Filtering by owner
- Sorting
- Pagination
- Updating invoices
- Issuing invoices
- Paying invoices
- Bulk invoice generation
- Voiding invoices

### Receivables

Receivables provide invoice-level billing information and support CSV export for billing follow-up.

### Dashboard

The dashboard provides billing summaries including:

- Collected amount
- Outstanding amount
- Overdue amount
- Monthly issued amount
- Invoice status breakdown
- Subscription plan breakdown

### Credit Notes

Credit notes can be created against paid invoices.

The implementation validates that:

- The referenced invoice exists.
- The invoice is paid.
- The credit note amount does not exceed the invoice amount.

## 4. Request Flow

A typical API request follows this flow:

Client
  ↓
Controller
  ↓
Service / Business Logic
  ↓
Repository
  ↓
Database

The response then travels back through the same layers to the client.

## 5. Data Flow

The main billing flow is:

Customer
  ↓
Subscription
  ↓
Invoice
  ↓
Invoice Issued
  ↓
Payment
  ↓
Receivables / Dashboard

For a paid invoice, a credit note can also be created when required.

## 6. Security and Access Control

The application uses authenticated user information and roles to restrict protected operations.

Billing administrators have broader billing-management permissions, while account managers are restricted according to the application's ownership and access rules.

Credentials and environment-specific configuration should be supplied through environment variables when deployed.

## 7. Deployment Architecture

The intended deployment flow is:

GitHub Repository
        ↓
Backend Deployment Service
        ↓
Live Spring Boot API
        ↓
Database

Environment-specific database credentials and other secrets are configured through deployment environment variables rather than being committed to the repository.

## 8. Design Goals

The architecture is designed to provide:

- Clear separation of responsibilities
- Maintainable business logic
- Role-based access control
- Validation of important billing operations
- Reusable REST APIs
- Easy deployment
- Easy future feature development