# CED Operations — Business Workflow

> **Business functional specification.** Describes *how the system is used*, in
> business terms — deliberate no implementation details (endpoints, DTOs, table
> names). For the technical/API view see `PROJECT_BLUEPRINT.md` and
> `API_DOCUMENTATION.md`.

## Table of Contents

1. [Introduction](#1-introduction)
2. [User Roles](#2-user-roles)
3. [Overall Business Workflow](#3-overall-business-workflow)
4. [Setting Up the Factory](#4-setting-up-the-factory)
5. [Designing a Report (Module Configuration)](#5-designing-a-report-module-configuration)
6. [Publishing a Template Version](#6-publishing-a-template-version)
7. [Daily Staff Workflow](#7-daily-staff-workflow)
8. [Save & Next / Save & Submit](#8-save--next--save--submit)
9. [Report Lifecycle](#9-report-lifecycle)
10. [Approval Workflow](#10-approval-workflow)
11. [Dashboard & Analytics](#11-dashboard--analytics)
12. [Global Search](#12-global-search)
13. [Business Rules](#13-business-rules)
14. [Version 1 Scope](#14-version-1-scope)
15. [Version 1.x and Beyond](#15-version-1x-and-beyond)

---

# 1. Introduction

## Why this system exists

Manufacturing facilities generate operational data every single day — machine
observations, chemical consumption, startup checks, inspection results. Before
this system, that data was collected on paper: clipboards, handwritten logbooks,
and carbon copies that ended up in a filing cabinet.

Paper reporting has serious problems:

- Records are lost, smudged, or illegible.
- There is no way to confirm when a report was written or who wrote it.
- Approval happens in person or not at all.
- No one can search years of history in seconds.
- Management cannot see, in real time, how the plant is performing.
- The same observations are re-entered into spreadsheets manually.

## What this system solves

The CED Operations Management System replaces paper-based reporting with a
single digital platform that:

- Captures operational reports at the point of work.
- Applies structured, **configurable templates** so every report is complete and
  consistent.
- Records data step by step as the work actually happens.
- Gives management an instant, live view of plant activity.
- Makes every report searchable and retrievable, forever.

The key design principle: **report types are defined by configuration, not by
the software.** A factory that changes how it records work changes a template —
it never waits for a software change.

## The expected users

- **Staff (Operators)** — create and complete the daily reports.
- **Admins** — design and manage report templates, review the plant, and (in a
  future version) approve completed reports.
- **Super Admins** — own the system: users, master data, and full configuration.

---

# 2. User Roles

The system has three roles. Each role is a distinct set of responsibilities and
access.

## 2.1 Super Admin

| Aspect | Detail |
|--------|--------|
| **Responsibilities** | Own the system end to end: create and manage users, configure all master data, design report templates, and manage system settings and integrations. |
| **Permissions** | Every capability. The only role that deletes users and master data. |
| **Daily activities** | Onboard users, design/adjust templates, monitor the dashboard and analytics, manage settings and integrations. |
| **Restrictions** | None within the system. Deletes are permanent and only this role can perform them. |

## 2.2 Admin

| Aspect | Detail |
|--------|--------|
| **Responsibilities** | Manage daily operations: design/maintain report templates and master data, and monitor plant performance. |
| **Permissions** | Full read access plus the authority to create and update master data and templates. **Cannot delete** users or master data. |
| **Daily activities** | Configure templates, review dashboards and analytics, update shifts/lines/parameters, monitor activity. |
| **Restrictions** | No deletion rights — master data and users are protected from accidental removal. |

## 2.3 Staff (Operator)

| Aspect | Detail |
|--------|--------|
| **Responsibilities** | Record the daily operational observations accurately and on time. |
| **Permissions** | Start reports, fill process steps, save & submit, view their own reports and history, view the dashboard, search, manage their own attachments and notifications. |
| **Daily activities** | Log in, pick a module and line, see the shift already assigned, work through the process steps, and submit. |
| **Restrictions** | Cannot manage users, templates, or master data. Cannot delete reports. Cannot change a report once it is submitted. |

## Role summary

| Capability | Super Admin | Admin | Staff |
|------------|:-----------:|:-----:|:-----:|
| Fill & submit reports | ✓ | ✓ | ✓ |
| Design / publish templates | ✓ | ✓ | — |
| Delete templates / master data | ✓ | — | — |
| Manage users | ✓ | view only | — |
| Manage shifts / lines / parameters | ✓ | create/update | — |
| View dashboard & analytics | ✓ | ✓ | ✓ |
| Global search | ✓ | ✓ | ✓ |
| Manage settings / integrations | ✓ | view only | — |
| View audit logs | ✓ | ✓ | — |

---

# 3. Overall Business Workflow

The system supports one continuous business cycle: **design once, operate daily.**

```
Configure the Factory
        │
        ▼
Design a Module (template)
        │
        ▼
Publish the Template Version
        │
        ▼
Staff Daily Operations (Start → Save & Next → Save & Submit)
        │
        ▼
Completed Report (permanent record)
        │
        ▼
Dashboard / Analytics / History / Search
```

- **Design once:** Super Admin and Admin define the report templates as a
  hierarchy of module types, modules, versions, process steps, and fields.
- **Operate daily:** staff start a report from a module, step through its
  processes, and submit it. A **completed report** is permanent.
- **Review continuously:** management watches the live dashboard, digs into
  analytics, and searches any report ever recorded.

---

# 4. Setting Up the Factory

When a new factory starts using the system, the Super Admin configures:

```
Create Users
      │
      ▼
Configure Shifts
      │
      ▼
Configure Production Lines
      │
      ▼
Configure Modules & Parameters (report templates)
      │
      ▼
Publish Templates → System Ready
```

- **Users** — an account for every person, each with a role.
- **Shifts** — the factory's work windows (start/end), which the system uses to
  assign the correct shift automatically.
- **Production lines** — where the work happens; every report is tied to the
  line it was recorded on.
- **Modules (templates)** — the report designs staff will fill in each day.

---

# 5. Designing a Report (Module Configuration)

Rather than six fixed report types, the factory designs its own reports from a
**module hierarchy**. This hierarchy is the answer to "what fields are on this
form and in what order."

```
Module Type
   ▼
Module                 a report template (with a code prefix for report numbers)
   ▼
Template Version       a published snapshot of the module's design
   ▼
Process Step           one ordered stage of the workflow (e.g. "Bath Setup", "Line Check")
   ▼
Process Parameter      one field on a step (e.g. "Temperature", "Voltage")
   ▼
Parameter (global)     the reusable definition of a field (name, input type)
```

How a designer builds a module:

1. **Create a Module Type** — a category, e.g. Production or Quality.
2. **Create a Module** — a report template with a short unique prefix (used in
   report numbers) and a clear description.
3. **Define Template Versions** — each module can have several versions of its
   design. Staff always work against the latest **published** version.
4. **Add Process Steps** — the ordered stages of the work, in the order they
   happen.
5. **Add Parameters to each Step** — the fields, configured once:

   | Attribute | Meaning |
   |-----------|---------|
   | Field / Parameter | Which global field definition is used |
   | Display order | Where the field appears on the step |
   | Input type | Text, number, boolean, drop-down |
   | Mandatory / Visible | Whether it is required and whether staff see it |
   | Unit | Unit of measurement (e.g. °C, bar, kg) |
   | Minimum / Maximum | Acceptable range for numeric values |
   | Default value | Value pre-filled when the form opens |

Because this is configuration, a factory changes the shape of its reports by
editing the template — no software change is needed.

---

# 6. Publishing a Template Version

A report may only be started against a **published (active)** template version.
Publishing makes a version the live design:

- The version becomes `ACTIVE`.
- Any previously active version is superseded (archived as a historical design).
- If the module itself is still in draft, publishing also makes it active.

Once a version is superseded it can no longer be edited. This guarantees that a
report started against a version always reflects exactly that design — even if
the module is later redesigned. Staff reporting is never disrupted by template
maintenance.

> **Design rule:** old reports keep the version in use when they were started,
> so historical records never shift when a template changes.

---

# 7. Daily Staff Workflow

This is what an operator does on a normal day:

```
Login
   │
   ▼
Pick a Module
   │
   ▼
(Optional) choose the Line — the shift is assigned automatically
   │
   ▼
Start the report → first Process Step loads
   │
   ▼
Fill each step, working through the process
   │
   ▼
Save & Submit the final step
   │
   ▼
Completed Report recorded
```

- **Start.** The operator picks a module to report on. The system freezes the
  module's current published design, assigns the shift automatically
  (including overnight shifts), and presents the **first process step**.
- **Fill each step.** The operator enters the readings shown. The form respects
  the template: fields appear in the configured order, required fields must be
  filled, and numeric values must be within the configured range.
- **Step through.** Each step is recorded and the system advances to the next
  one. The operator cannot skip ahead, and a step is not complete until recorded.
- **Submit.** On the final step, the operator chooses to finish and submit the
  report.

> The shift is **never chosen by staff** — the system assigns the active shift
> automatically, including overnight shifts that cross midnight.

---

# 8. Save & Next / Save & Submit

Reports are captured **step by step**, mirroring the actual work:

- **Save & Next** — records the current process step's values and advances to the
  next step in the template's order.
- **Save & Submit** — records the final step and produces the **completed
  report** (a permanent record with a report number under the module's prefix).

Work-in-progress is kept safe between steps. If the operator stops partway, the
partial report is retained as a session and can be continued forward later. A
report becomes a permanent, immutable record only when it is **saved &
submitted**.

> **V1 limitation:** once submitted, a report is fixed — it cannot be edited or
> stepped backwards, and there is no approval step (see §10).

---

# 9. Report Lifecycle

A report starts when a staff member begins a session and ends as a completed
record:

```
Report Session (in progress) ── Save & Submit ──► Completed Report (submitted)
      │
      ├─── steps captured and saved one by one
      └─── frozen to the published template version at start
```

- **Session (work in progress)** — a report being filled. It is tied to a module
  and the template version that was published when it started. It can be left and
  resumed (forward).
- **Completed Report (permanent)** — produced on Save & Submit. It carries a
  unique report number and a frozen copy of the template, shift, line, and every
  recorded value. It is the final, auditable record.

- The report engine keeps approval statuses (`APPROVED` / `REJECTED`) available
  but **not implemented yet** — see §10.

---

# 10. Approval Workflow

**Status: not yet implemented.** A completed report is finished when submitted.

- In a future version, Admins and Super Admins will review submitted reports
  from an **approval queue**, then **approve** (making the record final) or
  **reject** (returning it with remarks for correction and resubmission).
- The data model already reserves the fields needed for approval, so the change
  is additive and will not disrupt the existing report data.

The V1 behavior today: a submitted report is final and read-only for everyone.

---

# 11. Dashboard & Analytics

Management gets a live view of plant activity, available to all roles.

## Dashboard

The cockpit shows:

- **Overview totals** — reports by module, shift, and line.
- **Today's reports** — reports recorded today, to confirm reporting is on time.
- **Pending approvals** — (once the approval workflow ships) the review queue.
- **Approval summary** — approval health (pending/approved/rejected, today's
  decisions, approval rate).
- **Recent activity** — a newest-first feed of report life events.

## Analytics & KPIs

Deeper aggregates for management:

- **Overview** — reports by module, status, shift, and line.
- **Quality** — approval and pass/fail rates, trends.
- **Consumption** — sums of numeric recorded readings, by time and line.
- **Process stability** — pass/fail per field, out-of-range parameters, failure
  frequency.
- **Productivity** — reports per day/shift/operator, average turnaround time.
- **Performance** — per line, per shift, and per operator.

## How management uses it

- **Operational control** — spot whether reporting is happening on every shift
  and line.
- **Workload management** — clear the approval queue (once implemented).
- **Quality monitoring** — watch pass/fail and approval signals to catch problems.
- **Quick navigation** — recent items link directly into the underlying reports.

---

# 12. Global Search

One search box across **reports, users, and parameters**. Any user can search
from anywhere in the application.

- Results are grouped by type (report / user / parameter) and can be narrowed to
  one category.
- Reports and parameters: filter by **report number, module, module type,
  employee, shift, line, status, and date**.
- Enterprise results are paginated for easy browsing and link directly into the
  full record.

| I want to find… | I search by… |
|-----------------|--------------|
| The operator who logged a reading | Employee name |
| A specific record | Report number (e.g. `PRODUCTION-20260701-00001`) |
| Where a particular measurement appears | Parameter |
| What happened overnight last week | Shift |
| Everything on Line 2 this month | Line |
| The records still awaiting decision | Status |
| All reports for one module | Module |
| Activity between two dates | Date range |

---

# 13. Business Rules

1. **Only the Super Admin manages users** — create, update, deactivate, delete.
2. **Only the Super Admin deletes master data** — modules, shifts, lines,
   parameters. Admins can create/update but not delete.
3. **Any authenticated user can start and submit reports** — in practice Staff
   (Operators) are the ones recording daily observations.
4. **Shift is assigned automatically** — never selected by staff, overnight
   handled correctly.
5. **Reports run against a published template version** — a report always uses
   the version active when it started, and is never mutated by later changes.
6. **Process order is configured** — the `displayOrder` of process steps and
   fields defines the form's order; the system never reorders on its own.
7. **Required fields must be filled; values must be in range** — an incomplete
   or out-of-range observation cannot be recorded/submitted.
8. **A completed report is permanent** — once submitted it is a read-only record.
9. **Report numbers are unique under a module's prefix** — each report gets a
   sequential number scoped by its module prefix and date.
10. **No approval in V1** — a submitted report is final pending the future
    approval workflow.
11. **Deactivated users keep their history** — accounts can be disabled without
    losing records they created.

---

# 14. Version 1 Scope

## Included in Version 1

- ✓ Authentication with role-based access.
- ✓ Factory setup — users, shifts, lines.
- ✓ Module configuration — types, modules, template versioning, processes,
  parameters.
- ✓ Session-driven daily reporting — start, save & next, save & submit.
- ✓ Dashboard, analytics, unified search.
- ✓ Attachments, notifications, system settings, integration center, audit logs.

## Report capabilities at a glance

| Capability | Version 1 |
|---|-------|
| Design & publish report templates | ✓ Implemented |
| Start a report (freeze template version, auto shift) | ✓ Implemented |
| Save & Next (process-by-process) | ✓ Implemented |
| Save & Submit (completed report) | ✓ Implemented |
| View / list / search / filter reports | ✓ Implemented |
| Resume a session forward | ✓ Supported (in-progress sessions persisted) |
| Approve / reject with remarks | ✗ **Not implemented** (planned V1.x) |
| Edit a process / step backward | ✗ **Not implemented** (planned) |
| Edit a completed report | ✗ **Not implemented** |

> **Proof:** the full capability ↔ endpoint mapping and the business-workflow
> coverage are in `CURRENT_STATE.md` and `API_DOCUMENTATION.md`.

---

# 15. Version 1 and Beyond

## Version 1.x (planned)

- Approval workflow (approve/reject with remarks).
- Report edit / re-run and reject → resubmit.
- Write-path audit logging.
- External notification channels (email/SMS/push).
- Attachments per recorded value.
- Parameter-level analytics and per-module dashboard widgets.

## Version 2 (ideas)

- AI insights / anomaly detection.
- ERP / MES integration.
- Offline sync.
- Advanced analytics.
- Backend export (PDF/Excel/CSV).

Because report shapes are configuration, adding a new report type is adding a
module — the new report immediately inherits the whole lifecycle, dashboard,
analytics, and search behavior. The system grows by adding templates, not by
reworking the workflow.

---

# Frequently Asked Questions

**Who can design a report?**
Super Admins and Admins. They build the module hierarchy and publish template
versions.

**Can staff change the form?**
No. The form is the published template; staff only fill it.

**Does staff pick a shift?**
No. The system assigns the active shift automatically, overnight shifts
included.

**Can parameters be hidden?**
Yes — a field's **visible** flag hides it from staff while the value is still
collected.

**Can completed reports be edited?**
No. Once saved & submitted, a report is a permanent, read-only record. Sessions
can only step forward while still in progress.

**Are values validated?**
Yes. Required fields and numeric min/max ranges are enforced per the template.

**Who approves reports?**
Approval is not yet implemented. A future version will give Admins/Super Admins
an approval queue with approve/reject + remarks.

**Can a new report type be added?**
Yes — by configuring a new module from the configuration. No code, and thus no
deployment, needed.