# Module Notes

## Login
- The first pass focused on the happy path only and did not yet cover the expiry or lockout scenarios.
- The login fixture initially left default values in the form fields, so the tests were appending to existing data instead of replacing it.
- I changed the tests to clear the form before entering credentials and adjusted the page behavior so session expiry and lockout assertions became consistent.

## Dashboard
- The initial dashboard approach was too dependent on inline JavaScript and produced unstable DOM behavior during test execution.
- The page script first caused parsing issues in the headless browser, which made widget and filter assertions unreliable.
- I simplified the fixture to a predictable UI flow and adjusted the tests to verify rendering, filtering, and role-based visibility against that simpler structure.

## REST API
- The first API draft was too dependent on external services, which would have made the project harder to run and evaluate locally.
- The mock server initially needed more coverage for error and validation paths, so the tests were incomplete for some edge cases.
- I changed the implementation to use a lightweight local server and expanded the tests for auth, CRUD, 4xx/5xx handling, rate limiting, and schema validation.
