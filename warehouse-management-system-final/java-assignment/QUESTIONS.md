# Questions

## 1. Database access and manipulation

**Answer:**

I would keep the repository boundary, but make persistence behavior consistent. The warehouse code already has a domain-facing `WarehouseStore` port and a Panache adapter, while Store and Product resources call Panache entities/repositories more directly. Over time I would move persistence queries and mutations behind repositories for all three aggregates. This makes transaction boundaries easier to reason about and allows use cases to be unit-tested without a database.

I would also avoid mixing HTTP concerns into domain rules. The use cases should report meaningful business failures, and the REST layer should translate them into stable HTTP responses. For warehouse replacement, archiving the old row and inserting the new row must happen in one transaction. The old row should be retained as history rather than physically deleted.

I would add database constraints where they protect invariants (for example, indexes for active business-unit lookups and a constraint strategy that permits archived history while preventing duplicate active codes). Application validation improves error messages, but it cannot alone protect against concurrent requests.

## 2. OpenAPI-generated endpoints versus hand-written endpoints

**Answer:**

OpenAPI-first development gives consumers a contract before implementation, consistent schemas and response documentation, and generated interfaces that reduce repetitive endpoint code. The trade-offs are generated code to understand during debugging, a build step to maintain, and the need to keep the specification accurate.

Hand-written endpoints are quick to start and can be easier for small APIs, but the contract can drift from behavior and validation/error handling can become inconsistent.

For this implementation I keep the Warehouse OpenAPI YAML as the documented contract and use a small hand-written JAX-RS adapter so the application can compile in an IDE without generated-source indexing. The trade-off is that the YAML and adapter must be kept in sync, so I would add contract checks in CI if the API grows. In a team that standardizes on generated interfaces, I would keep generation in the build and test the generated contract against the implementation. Either way, I would verify actual HTTP status codes, request validation, and response schemas.

## 3. Test strategy and maintaining effective coverage

**Answer:**

I would start with unit tests for domain rules because they are fast and cover edge cases: unknown locations, duplicate active business-unit codes, capacity and stock limits, maximum warehouses per location, replacement stock matching, and archive history. Repository tests should verify persistence mapping and active-versus-archived queries. API tests should then cover status codes and end-to-end behavior for create, retrieve, replace, and archive.

For Store synchronization, I would test that the legacy gateway is invoked only after a successful transaction commit and is not invoked when the transaction rolls back. A transactional outbox would be a stronger long-term option if delivery must be retried reliably, because a process can fail after the database commits but before the external call completes.

CI should run the same Maven test/build command on every pull request, use a known Java version, and publish test results. I would use coverage as a signal rather than a target by itself: prioritize business-critical branches and regression tests for every production defect.
