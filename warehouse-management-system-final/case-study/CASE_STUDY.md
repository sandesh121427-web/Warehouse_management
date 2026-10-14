# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking

**Questions you may have and considerations:**

- Which cost categories must be captured: labor, storage, inventory handling, transportation, utilities, rent, depreciation, and shared overhead?
- What is the source of truth for each category, and how frequently is each source updated?
- Should costs be allocated by a measurable driver such as pallet-days, order lines, units shipped, square footage, labor hours, or actual transport distance?
- How should shared costs, returns, damaged stock, idle capacity, and inter-warehouse transfers be treated?
- What granularity is required: business unit, warehouse, store, product, shipment, or accounting period?
- Which currency, tax treatment, accounting calendar, and cost-center hierarchy apply?
- How will finance reconcile allocated totals to the general ledger and investigate missing or duplicated transactions?

I would define allocation rules with Finance and Operations before implementing calculations. Each allocation should be traceable to its source records, rule version, driver quantity, and calculation period. This makes the results explainable and allows historical reports to be reproduced after rules change.

## Scenario 2: Cost Optimization Strategies

**Questions you may have and considerations:**

- Which service-level targets are non-negotiable (delivery time, order accuracy, availability, and returns)?
- Where is the baseline cost, and how is cost per order, unit, shipment, or pallet currently measured?
- Are there under-utilized warehouses, avoidable transfers, excessive transport distance, overtime, or recurring stock imbalances?
- Which actions require capital expenditure or changes to contracts, staffing, network design, or inventory policy?
- How will savings be measured against a baseline, and how will volume or seasonality be normalized?

I would establish a baseline, identify the largest cost drivers, and rank opportunities by expected annual benefit, implementation effort, risk, and impact on service. I would pilot the highest-value low-risk changes, define success metrics in advance, and compare results with a control or historical baseline. Examples include improving warehouse utilization, consolidating shipments, reducing avoidable transfers, optimizing pick paths, and improving demand forecasts. A saving should not be accepted if it merely moves cost to another unit or degrades service quality.

## Scenario 3: Integration with Financial Systems

**Questions you may have and considerations:**

- Which finance systems and interfaces are involved, and do they support APIs, events, or scheduled files?
- Which system owns each field and transaction, and what identifiers connect warehouse events to financial records?
- What latency is actually required for each data type?
- How are retries, duplicates, out-of-order updates, corrections, and unavailable systems handled?
- How will authentication, authorization, encryption, audit logs, reconciliation, and monitoring be implemented?

I would agree a versioned data contract and ownership model first. For event-driven integration, I would use stable event IDs, idempotent consumers, retries with backoff, a dead-letter path, and reconciliation reports. If the database commit and event publication must be reliable together, I would use a transactional outbox. Dashboards should expose freshness, failed messages, reconciliation differences, and end-to-end processing time. Real-time synchronization should be used where it creates business value; batch processing may be adequate for some accounting summaries.

## Scenario 4: Budgeting and Forecasting

**Questions you may have and considerations:**

- What horizon and granularity are needed (monthly by warehouse, weekly by region, or daily for labor)?
- Which historical actuals, planned volumes, seasonality, inflation, wage rates, fuel prices, lease costs, and planned network changes are available?
- Who owns assumptions and approves budget changes?
- How will forecast accuracy and budget variance be measured?
- Should the tool support multiple scenarios, such as base, high-volume, and disruption cases?

I would separate actuals, approved budget, and forecast, and keep assumptions explicit and versioned. Forecasts should combine volume drivers with cost rates rather than extrapolating totals alone. I would report forecast error and variance by cost category, explain material deviations, and allow controlled scenario comparisons. Forecasts are decision-support tools, so owners need to understand the assumptions and limitations rather than treating a point estimate as certain.

## Scenario 5: Cost Control in Warehouse Replacement

**Questions you may have and considerations:**

- What event triggers replacement, who approves it, and what is the cutover date?
- Which costs must remain attributable to the archived warehouse, and which costs belong to the replacement?
- How will open purchase orders, leases, assets, stock, labor commitments, and in-flight shipments be handled?
- Does reusing the Business Unit Code affect financial-system keys, reports, or downstream integrations?
- How will we prove that the old cost history remains immutable and that new costs are assigned to the new warehouse instance?
- What budget, service, and operational metrics determine whether the replacement is successful?

The Business Unit Code is a reusable business identifier, not a unique identifier for a physical warehouse instance. I would preserve a separate immutable database ID and effective dates for each instance. Replacement should archive the old instance and create the new one in one transaction, with an audit event linking the two. Historical costs should reference the old instance ID, while future costs reference the new instance ID. Before cutover, I would reconcile outstanding costs and stock, approve the new operating budget, and monitor actual-versus-budget results after launch.

## Scope questions to resolve before implementation

1. Which finance and operational systems are authoritative for cost, stock, and location data?
2. What are the required freshness, availability, security, retention, and audit requirements?
3. Which cost drivers and allocation rules have Finance approved?
4. What volumes, concurrency, reporting latency, and expected growth should the system support?
5. Who owns data quality, exceptions, reconciliation, and budget approvals?
6. What measurable business outcomes define success, and what baseline will be used to evaluate them?
