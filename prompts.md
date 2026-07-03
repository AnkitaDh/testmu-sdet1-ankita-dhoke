# Task 2 Raw Prompts

This file preserves the actual prompt sequence used for Task 2, including the refinement prompts that followed the first pass.

## Login module
### Prompt 1
Generate QA test cases for a web-based login flow in Gherkin. Cover valid login, invalid credentials, forgot password behavior, session expiry, and brute-force lockout.

### Prompt 2
The first pass was too broad. Generate a more structured set of login scenarios in Gherkin that prioritizes the core regression risks: valid login, invalid credentials, forgot password, session expiry, and lockout after repeated failures.

### Prompt 3
Add edge cases for the login flow that are easy to miss in a first draft, including stale form values, expired tokens, and repeated failed attempts that should trigger a temporary lockout.

## Dashboard module
### Prompt 1
Generate dashboard test cases in Gherkin that cover widget loading, data accuracy, filter-and-sort behavior, responsive layout, and role-based visibility.

### Prompt 2
Refine the dashboard prompt so the generated scenarios emphasize realistic UI failures: missing widget content, incorrect summary values, filter results that do not update, and role-based panels that appear when they should not.

### Prompt 3
Expand the dashboard prompt with layout-oriented scenarios for small viewports, hidden admin-only sections, and state changes triggered by refresh or filter actions.

## REST API module
### Prompt 1
Generate REST API test cases in Gherkin for auth token validation, CRUD operations, 4xx/5xx error handling, rate limiting, and schema validation.

### Prompt 2
Refine the API prompt so the scenarios focus on the most failure-prone behaviors: invalid auth headers, malformed payloads, server errors, throttling, and schema mismatches.

### Prompt 3
Add follow-up API cases for repeated requests, partial updates, and response structure expectations so the generated suite is closer to a production regression pack.
