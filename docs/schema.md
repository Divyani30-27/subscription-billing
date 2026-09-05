# Database Schema

## 1. Overview

The application uses a relational database through Spring Data JPA.

The main persistent entities are:

- User
- Subscription
- Invoice
- CreditNote
- Subscription collaborators

## 2. Users

Table: `users`

| Column | Type | Description |
|---|---|---|
| id | Long / BIGINT | Primary key |
| name | String / VARCHAR | User name |
| email | String / VARCHAR | Login email; unique and not null |
| password | String / VARCHAR | User password |
| role | String / VARCHAR | User role |

The `email` column is unique and cannot be null.

Supported roles include:

- BILLING_ADMIN
- ACCOUNT_MANAGER

## 3. Subscriptions

Table: `subscription`

| Column | Type | Description |
|---|---|---|
| id | Long / BIGINT | Primary key |
| customerName | String / VARCHAR | Customer name |
| billingEmail | String / VARCHAR | Customer billing email |
| planName | String / VARCHAR | Subscription plan |
| billingCycle | String / VARCHAR | Billing cycle |
| price | BigDecimal / DECIMAL | Subscription price |
| startDate | LocalDate / DATE | Subscription start date |
| ownerId | Long / BIGINT | Owning account manager ID |
| archived | boolean | Whether the subscription is archived |

A subscription also has collaborator IDs stored through JPA `@ElementCollection`.

## 4. Subscription Collaborators

The application stores collaborator user IDs as an element collection associated with a subscription.

Relationship:

- One subscription can have many collaborators.
- One account manager can collaborate on many subscriptions.

The assignment therefore represents this as a many-to-many business relationship between subscriptions and account managers.

The application uses the collaborator IDs to determine whether an account manager has access to a subscription.

## 5. Invoices

Table: `invoice`

| Column | Type | Description |
|---|---|---|
| id | Long / BIGINT | Primary key |
| subscriptionId | Long / BIGINT | ID of the related subscription |
| billingPeriodStart | LocalDate / DATE | Billing period start |
| billingPeriodEnd | LocalDate / DATE | Billing period end |
| amount | BigDecimal / DECIMAL | Amount owed |
| dueDate | LocalDate / DATE | Payment due date |
| status | String / VARCHAR | Invoice lifecycle status |

The invoice status starts as `DRAFT`.

The intended lifecycle is:

`DRAFT → ISSUED → PAID`

An invoice may also become `VOID` while it is eligible for voiding.

## 6. Credit Notes

Table: `credit_note`

| Column | Type | Description |
|---|---|---|
| id | Long / BIGINT | Primary key |
| invoiceId | Long / BIGINT | Related invoice ID |
| amount | BigDecimal / DECIMAL | Credit amount |
| reason | String / VARCHAR | Reason for the credit |
| createdAt | LocalDateTime / TIMESTAMP | Creation time |

A credit note is stored as its own record rather than changing the original paid invoice.

The application only allows a credit note for a paid invoice and validates that the credit amount does not exceed the invoice amount.

## 7. Relationships

### User → Subscription

A user acting as an account manager can own multiple subscriptions.

Business relationship:

`User (Account Manager) 1 → N Subscription`

The subscription stores the owner's user ID in `ownerId`.

### Subscription → Invoice

One subscription can have multiple invoices.

`Subscription 1 → N Invoice`

Each invoice stores the related subscription ID.

### Invoice → CreditNote

An invoice can have credit notes associated with it.

`Invoice 1 → N CreditNote`

Each credit note stores the related invoice ID.

### Subscription ↔ Collaborator

A subscription can have multiple collaborator account managers, and an account manager can collaborate on multiple subscriptions.

`Subscription N ↔ N User`

This is represented through the JPA element collection of collaborator IDs.

## 8. Database Constraints vs Application Rules

Some constraints are represented directly in the entity/database mapping.

Examples:

- User email is unique.
- User email cannot be null.
- Entity IDs are generated automatically.

Business rules are primarily enforced in the application layer.

Examples:

- Only permitted roles can perform billing actions.
- Account managers can only access subscriptions they own or collaborate on.
- Paid invoices cannot be modified.
- Paid invoices cannot be voided.
- Credit notes require a reason.
- Credit note amount must be positive.
- Credit note amount cannot exceed the invoice amount.

## 9. Deliberate Denormalisation

The subscription stores `ownerId` and collaborator user IDs directly instead of modelling a full user object relationship in every operation.

This keeps the current implementation simple and makes access checks straightforward.

The trade-off is that more complex reporting and relationship queries may require additional application-side processing.

## 10. Scaling Considerations

At 100x the current data volume, the first areas likely to require improvement would be invoice filtering and reporting.

The current implementation performs several filtering and access checks in application code. At larger scale, these operations should be moved into database queries with appropriate indexes.

Likely indexes would include:

- invoice status
- invoice due date
- invoice subscription ID
- subscription owner ID
- user email

Pagination should also remain server-side as the dataset grows.