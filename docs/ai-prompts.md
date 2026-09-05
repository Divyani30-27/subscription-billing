# AI Prompts

AI tools were used during development to speed up implementation, debugging, validation and documentation.

## 1. Project structure and implementation

### Prompt
"Help me implement the Subscription Billing assignment using Spring Boot. Start with the required entities, repositories and REST controllers and keep the implementation simple and maintainable."

### Purpose
Used to establish the initial project structure and identify the main components required for the billing workflow.

---

## 2. Authentication and role-based access

### Prompt
"Implement login using email and password with two roles: BILLING_ADMIN and ACCOUNT_MANAGER. Enforce the permissions on the server so that account managers cannot perform billing-admin-only actions."

### Purpose
Used to implement the authentication flow and server-side role checks.

---

## 3. Subscription access and collaborators

### Prompt
"Add subscription ownership and collaborators. An account manager should be able to access a subscription only when they own it or are a collaborator. Only a billing admin should be able to add or remove collaborators."

### Purpose
Used to implement the subscription access-control rules.

---

## 4. Invoice lifecycle validation

### Prompt
"Implement the invoice lifecycle Draft → Issued → Paid and enforce the rules for editing, issuing, paying and voiding invoices. Paid invoices must become immutable."

### Purpose
Used to implement and verify invoice state transitions.

---

## 5. Credit note validation

### Prompt
"Add credit notes for paid invoices. Require a reason, require a positive amount, prevent the amount from exceeding the invoice amount, and keep the credit note as a separate record."

### Purpose
Used to implement the credit-note correction workflow without modifying the original paid invoice.

---

## 6. Invoice search and filtering

### Prompt
"Add invoice search, status filtering, overdue filtering, owner filtering, sorting and pagination. The filtering and pagination should happen on the server."

### Purpose
Used to implement invoice discovery functionality.

---

## 7. Dashboard and reporting

### Prompt
"Create a billing dashboard that calculates invoices issued this month, collected revenue, outstanding receivables, overdue invoices, status breakdown and plan breakdown."

### Purpose
Used to implement the dashboard reporting endpoint.

---

## 8. Debugging and correction

### Prompt
"Review the current implementation against the assignment requirements and identify why a requested billing operation is not behaving correctly. Suggest the smallest safe code change and explain what should be tested afterwards."

### Purpose
Used during debugging when an implementation needed correction instead of adding unnecessary code.

---

## 9. Documentation

### Prompt
"Review the implemented Subscription Billing project and draft concise documentation covering architecture, database schema, development plan, technical decisions and AI usage. Do not claim features that are not implemented."

### Purpose
Used to prepare the repository documentation.

---

## 10. Example of an incorrect AI output and what I did

### Prompt
"Implement the complete Subscription Billing assignment and make sure all ten required goals are finished."

### Problem with the output
The initial generated solution could describe or suggest functionality that was not actually present in the current implementation.

### What I did
I did not treat the generated response as proof that a feature was implemented. I checked the actual project code and tested the relevant endpoints before considering a requirement complete.

### Lesson
AI-generated code and explanations were treated as development assistance rather than as a substitute for verifying the actual application behavior.

---

## How AI was used

AI was mainly used for:

- Initial implementation assistance
- Debugging
- Understanding assignment requirements
- Reviewing business rules
- Drafting documentation
- Identifying missing or incorrect behavior
- Suggesting simple implementation approaches

The final implementation was checked against the project code and the assignment requirements before being treated as complete.