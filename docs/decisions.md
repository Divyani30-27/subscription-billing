# Decisions

This document records the implementation decisions that shaped the Subscription Billing System.

## Decision 1 — Use Spring Boot with a layered backend

- **Chose:** Spring Boot with separate controller, repository and model/entity responsibilities.
- **Rejected:** Putting all API, validation and database logic into a single controller class.
- **Why:** A layered structure makes the code easier to understand, test and extend. It also keeps HTTP handling separate from persistence and business rules.

## Decision 2 — Use BigDecimal for billing amounts

- **Chose:** `BigDecimal` for subscription prices, invoice amounts and credit note amounts.
- **Rejected:** `double` or `float`.
- **Why:** Billing amounts require exact decimal representation. Floating-point types can introduce precision problems, so `BigDecimal` is safer for monetary calculations.

## Decision 3 — Use session-based authentication and role checks

- **Chose:** Use `HttpSession` to store the logged-in user's ID and role and perform authorization checks on protected endpoints.
- **Rejected:** Only hiding or disabling actions in the frontend.
- **Why:** The assignment explicitly requires the role difference to be enforced on the server. Session data gives the backend enough information to reject unauthorized requests.

## Decision 4 — Represent collaborators using user IDs

- **Chose:** Store collaborator user IDs as an `@ElementCollection` on the subscription.
- **Rejected:** Building a more complex user/subscription entity relationship for the first implementation.
- **Why:** The main requirement is to identify which account managers can access a subscription. Storing IDs keeps the implementation simple and makes ownership/collaborator access checks straightforward.

## Decision 5 — Keep paid invoices immutable

- **Chose:** Treat a paid invoice as immutable and use a separate credit note for corrections.
- **Rejected:** Allowing users to edit the amount or other fields of a paid invoice.
- **Why:** The assignment requires a correction to leave its own record instead of silently changing billing history. A separate credit note preserves the original invoice and provides a clear audit trail.

## Decision 6 — Use an explicit invoice status lifecycle

- **Chose:** Use the statuses `DRAFT`, `ISSUED`, `PAID` and `VOID` with server-side validation around transitions.
- **Rejected:** Allowing arbitrary status changes from one state to another.
- **Why:** Billing operations have a natural lifecycle. Restricting transitions prevents invalid operations such as voiding a paid invoice.

## Decision 7 — Later reversed: filtering approach

- **Chose initially:** Use repository results and perform some filtering and dashboard calculations in application code because it was faster to implement for the first working version.
- **Rejected initially:** Writing database-specific filtering queries for every combination of search, status, owner, overdue state and sorting.
- **Why initially:** The first priority was to get the billing workflow working and verify the business rules.

- **Later reversed:** I recognized that the assignment explicitly requires invoice search, filtering, sorting and pagination to happen on the server, and that application-side processing would become less suitable as the dataset grows.
- **What changed my mind:** The requirement for server-side pagination and the need to handle larger datasets efficiently made database-level filtering the better long-term approach.
- **New direction:** Keep the API server-side and move heavier filtering/pagination work into repository/database queries as the dataset grows.

## Decision 8 — Prioritize required goals over stretch features

- **Chose:** Focus development time on the required billing workflow and documentation before optional features.
- **Rejected:** Spending the remaining time on features such as multi-currency, proration or a customer self-service portal.
- **Why:** The assignment states that the ten required goals are the cutoff and that stretch ideas should only be attempted after the required work is complete.