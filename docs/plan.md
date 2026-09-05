# Development Plan

## 1. How I broke the work into sessions

I divided the work into short implementation and verification sessions rather than trying to build everything at once.

### Session 1 — Project setup and core models

I started by setting up the Spring Boot application and the basic project structure.

I created the main entities and repositories required for the billing workflow:

- User
- Subscription
- Invoice
- CreditNote

I also configured the application so that it could run locally and persist the application data.

### Session 2 — Authentication and roles

I implemented login using email and password and introduced the two required roles:

- BILLING_ADMIN
- ACCOUNT_MANAGER

I added session-based authentication information so that later endpoints could enforce permissions.

### Session 3 — Subscription management

I implemented subscription creation and editing and then added:

- Ownership
- Archive
- Restore
- Collaborators
- Access checks

This established the ownership model needed for invoice access.

### Session 4 — Invoice lifecycle

I implemented invoice creation and the main lifecycle:

`DRAFT → ISSUED → PAID`

I then added validation around editing, issuing and paying invoices.

### Session 5 — Invoice discovery and bulk operations

I added server-side invoice listing functionality including:

- Search
- Status filtering
- Overdue filtering
- Owner filtering
- Sorting
- Pagination

I also implemented bulk invoice generation for active subscriptions.

### Session 6 — Billing reporting and corrections

I implemented:

- Dashboard billing metrics
- Receivables CSV export
- Invoice voiding
- Credit notes for paid invoices

I verified the important validation rules after adding these features.

### Session 7 — Documentation and packaging

I prepared the repository documentation and organized the project for GitHub publication and deployment.

The documentation covers architecture, schema, plan, decisions and AI usage.

## 2. What order I built in, and why

I built the system from the foundation upward.

First, I created the data model because subscriptions and invoices depend on a stable representation of users and billing data.

Next, I implemented authentication and roles because authorization is required by almost every protected operation.

After that, I implemented subscriptions because invoices belong to subscriptions.

I then implemented the invoice lifecycle and its validation rules.

Once the core billing workflow worked, I added invoice search, filtering, sorting, pagination and bulk generation.

Finally, I added dashboard/reporting features and credit notes.

This order reduced the amount of rework because each later feature depended on an already working part of the system.

## 3. Estimated versus actual time

I used the assignment's approximately 12-hour budget as the overall target and prioritized the required billing workflow first.

The implementation time was concentrated on the following areas:

| Area | Approximate time |
|---|---:|
| Project setup and data model | 1.5 hours |
| Authentication and roles | 1.5 hours |
| Subscription management | 2 hours |
| Invoice lifecycle and validation | 2.5 hours |
| Search, filtering and bulk generation | 1.5 hours |
| Dashboard, CSV and credit notes | 1.5 hours |
| Testing, debugging and documentation | 1.5 hours |
| **Total** | **Approximately 12 hours** |

The actual effort was not perfectly linear because debugging and verification took longer than expected in some areas.

## 4. What I cut when I ran short

I prioritized the required billing workflow over optional features.

The first areas I left for later were the more advanced reporting and history/alert functionality, because they required additional data structures and UI work.

I also did not build any of the optional stretch ideas such as:

- Usage-based add-ons
- Proration
- Customer self-service billing portal
- Reminder emails
- Multi-currency
- Tax calculation

These features were intentionally deferred because the assignment states that the ten required goals should take priority over stretch ideas.

## 5. Next steps

With the core application in place, the next priority is to publish the project to a public GitHub repository, complete the required documentation, configure a free deployment and verify the live application.

After the required submission workflow is stable, additional features can be added incrementally.