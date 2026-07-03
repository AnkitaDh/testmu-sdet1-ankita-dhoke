# AI Usage Log

- Task 1 — Tool: GitHub Copilot
  - Used for: scaffolding the Maven/Selenium Java project structure and initial test layout.
  - Produced: the repository skeleton, Maven configuration, and initial Java test classes.
  - Workflow note: this was the initial prompt used to get the project started and define the basic test architecture.

- Task 2 — Tool: GitHub Copilot
  - Used for: drafting prompting strategies for the Login, Dashboard, and REST API modules and generating the initial Gherkin-style cases.
  - Produced: the raw prompts in `prompts.md` and the generated scenarios in `generated-tests.md`.
  - Workflow note: the first prompt was broad and needed refinement; later prompts focused on edge cases, regression risks, and more realistic UI/API failures.
  - Accepted output: the core Gherkin structure and coverage ideas for each module.
  - Rejected/changed output: the first pass was too shallow for some modules, so it was refined to emphasize lockout, layout, and schema-related scenarios.

- Task 3 — Tool: GitHub Copilot
  - Used for: implementing the failure-explainer integration and shaping the report output format.
  - Produced: the LLM integration code, the sample explanation artifact, and the structured report written during test execution.
  - Workflow note: the initial implementation was adjusted to make the LLM requirement explicit and avoid a misleading fallback path.
  - Accepted output: the Option A failure-explainer approach and the JSON report format.
  - Rejected/changed output: the first draft included a fallback explanation path, which was removed to keep the integration clearly tied to a real API call.
