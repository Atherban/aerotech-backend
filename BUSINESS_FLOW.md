# CED Operations Management System

Business Workflow Documentation

Version 1

---

> **Related documents:** [PROJECT_BLUEPRINT.md](PROJECT_BLUEPRINT.md) (architecture),
> [API_DOCUMENTATION.md](API_DOCUMENTATION.md) (API reference),
> [CURRENT_STATE.md](CURRENT_STATE.md) (current implementation snapshot).
>
> **Implementation status:** this is a functional specification describing how
> the system is designed to be used. The current backend implements all of it
> **except** editing/resuming an existing draft and the edit/resubmit path for
> rejected reports — there is **no** report update endpoint. Sections marked with
> ⚠️ carry an implementation note; see `CURRENT_STATE.md` for details.

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [User Roles](#2-user-roles)
3. [Overall Business Workflow](#3-overall-business-workflow)
4. [Initial System Setup](#4-initial-system-setup)
5. [Daily Staff Workflow](#5-daily-staff-workflow)
6. [Report Lifecycle](#6-report-lifecycle)
7. [Approval Workflow](#7-approval-workflow)
8. [Master Data Workflow](#8-master-data-workflow)
9. [Parameter Configuration](#9-parameter-configuration)
10. [Automatic Shift Detection](#10-automatic-shift-detection)
11. [Dashboard Workflow](#11-dashboard-workflow)
12. [Global Search Workflow](#12-global-search-workflow)
13. [Data Flow](#13-data-flow)
14. [Screen Flow](#14-screen-flow)
15. [Business Rules](#15-business-rules)
16. [Version 1 Scope](#16-version-1-scope)
17. [Future Scope (Version 2+)](#17-future-scope-version-2)
18. [Frontend Integration Notes](#18-frontend-integration-notes)
19. [Frequently Asked Questions](#19-frequently-asked-questions)
20. [Conclusion](#20-conclusion)

---

# 1. Introduction

## Why this system exists

Manufacturing facilities generate operational data every single day — machine
observations, chemical consumption, startup checklists, inspection results.
Before this system, that data was collected on paper: clipboards, handwritten
logbooks, and carbon copies that ended up in a filing cabinet.

Paper reporting has serious problems:

- Records are lost, smudged, or illegible.
- There is no way to confirm when a report was written or who wrote it.
- Approval happens in person or not at all.
- No one can search years of history in seconds.
- Management cannot see, in real time, how the plant is performing.
- The same observations are re-entered into spreadsheets manually, wasting time
  and creating errors.

## What problems this system solves

The CED Operations Management System replaces paper-based manufacturing reports
with a single digital platform that:

- Captures daily operational reports at the point of work.
- Applies structured, configurable templates so every report is complete and
  consistent.
- Routes reports through a clear draft → submit → approve workflow.
- Gives management an instant, live view of plant activity through a dashboard.
- Makes every report searchable and retrievable, forever.

## How it replaces paper-based manufacturing reports

Each paper form becomes a **digital report type**. The observations recorded on
that form become **parameters** the facility configures once. Every day, staff
fill the digital form, submit it, and a supervisor approves or rejects it — the
whole lifecycle happens inside the system. Nothing is handwritten, nothing is
lost, and the record is permanent.

## The goals of Version 1

1. Digitize the six standard daily report types with a working approval
   workflow.
2. Let each factory configure its own shifts, production lines, and report
   parameters without software changes.
3. Give staff a fast daily entry experience.
4. Give management live dashboards, analytics, and global search.
5. Establish a documented, auditable record of every report.

## The expected users

- **Staff (Operators)** — create and submit the daily reports.
- **Admins** — supervise, approve, and manage master data.
- **Super Admins** — own the system: users, master data, and full configuration.

---

# 2. User Roles

The system has three roles. Each role is a distinct set of responsibilities and
access.

## 2.1 Super Admin

| Aspect | Detail |
|--------|--------|
| **Responsibilities** | Own the system end to end: create and manage users, configure all master data, approve reports, and manage system settings and integrations. |
| **Permissions** | Every capability in the system. The only role that can delete users, master data records, and reports. |
| **Daily activities** | Onboard new users, adjust shifts/lines/parameters, approve reports when needed, monitor the dashboard and analytics, manage settings and integrations. |
| **Accessible modules** | All: auth, users, roles, shifts, lines, report types, parameters, reports, approval, dashboard, analytics, global search, audit logs, settings, integrations, attachments, notifications. |
| **Restrictions** | None within the system. (Practical guardrails: deletes are permanent and only available to this role.) |

## 2.2 Admin

| Aspect | Detail |
|--------|--------|
| **Responsibilities** | Manage daily operations: approve or reject submitted reports, maintain master data, and monitor plant performance. |
| **Permissions** | Full read access to the system plus the authority to create and update master data and to approve/reject reports. **Cannot delete** users, master data, or reports. |
| **Daily activities** | Review the approval queue, approve or reject reports, view the dashboard and analytics, update shifts/lines/parameters, monitor activity. |
| **Accessible modules** | All except deletions: reports + approval, dashboard, analytics, master data (create/update), users (view only), settings (view only), audit logs, search, notifications, attachments. |
| **Restrictions** | No deletion rights. This is intentional: master data and users are protected from accidental removal. |

## 2.3 Staff (Operator)

| Aspect | Detail |
|--------|--------|
| **Responsibilities** | Record the daily operational observations accurately and on time, and submit them for approval. |
| **Permissions** | Create and submit reports, view their own reports and history, view the dashboard, search, and manage their own attachments and notifications. |
| **Daily activities** | Log in, see the shift auto-detected, pick a report type and line, fill the form, save as draft, then submit for approval. |
| **Accessible modules** | Reports (create/submit/view), dashboard, global search, attachments, notifications, lines (view), shifts (view only for auto-detection), report types (view), change own password. |
| **Restrictions** | Cannot approve, reject, or delete reports. Cannot manage users or master data. Cannot edit a report once submitted. |

## Role summary

| Capability | Super Admin | Admin | Staff |
|------------|:-----------:|:-----:|:-----:|
| Create / submit reports | ✓ | ✓ | ✓ |
| Approve / reject reports | ✓ | ✓ | — |
| Delete reports | ✓ | — | — |
| Manage users | ✓ | view only | — |
| Manage master data (shifts, lines, parameters) | ✓ | create/update | — |
| Delete master data | ✓ | — | — |
| View dashboard & analytics | ✓ | ✓ | ✓ |
| Global search | ✓ | ✓ | ✓ |
| Manage settings / integrations | ✓ | view only | — |
| View audit logs | ✓ | ✓ | — |

---

# 3. Overall Business Workflow

The system supports one continuous business cycle: configure once, operate daily.

```
Super Admin
     │
     ▼
System Configuration
     │
     ▼
Staff Daily Operations
     │
     ▼
Report Submission
     │
     ▼
Approval Workflow
     │
     ▼
Dashboard ──► History ──► Search
```

## Stage 1 — Super Admin (one-time / as needed)

The Super Admin is the first user. On day one they configure the system for the
factory: create users, define shifts, define production lines, and configure
the parameters for each report type.

## Stage 2 — System Configuration

Master data is established and refined over time:

- **Users** — who can log in and what role they hold.
- **Shifts** — the working hours the system uses to auto-assign reports.
- **Production lines** — where work happens.
- **Report parameters** — the exact fields on each report form.

Once configured, the system is ready for daily operation. Configuration only
changes when the factory changes.

## Stage 3 — Staff Daily Operations

Every day, staff log in and record observations. The system knows which shift is
active, so staff never pick a shift manually. They select a report type and a
production line, the system loads the configured form, and they fill it in.

## Stage 4 — Report Submission

Staff complete the full form in one step (create), which stores it as a
**draft**, then submit it when complete. A submitted report is locked and queued
for approval. Submission records the shift, line, and operator behind the report.

> ⚠️ **V1 limitation:** a report is created in one request and cannot be
> reopened/edited afterwards — there is no "save and continue later" or draft
> update endpoint. If a draft is wrong it must be deleted (SUPER_ADMIN) and
> recreated.

## Stage 5 — Approval Workflow

Admins and Super Admins review the queue. A report is either **approved**
(becomes permanent and read-only) or **rejected** (marked `REJECTED` with
feedback). ⚠️ In Version 1 a rejected report is **terminal** — the designed
"correct and resubmit" step is not implemented (no report update endpoint).

## Stage 6 — Dashboard, History & Search

Every completed report flows into the management dashboard, the historical
record, and the global search:

- **Dashboard** — live totals, today's reports, pending approvals, approval
  summary, recent activity.
- **History** — the permanent, chronological record of every report.
- **Search** — anyone can instantly find any report by number, employee,
  parameter, shift, line, status, type, or date.

---

# 4. Initial System Setup

When a new factory starts using the system, it follows this setup:

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
Configure Report Parameters
     │
     ▼
System Ready
```

## Step 1 — Create Users

The Super Admin creates an account for every person who will use the system and
assigns a role (Super Admin, Admin, or Staff). Each user has a unique login and
changes their own password on first use.

## Step 2 — Configure Shifts

The Super Admin defines the factory's shift schedule: each shift's name, start
time, and end time. These times drive automatic shift assignment, so staff never
select a shift manually.

## Step 3 — Configure Production Lines

The Super Admin defines the physical production lines the factory operates.
Every report is tied to the line it was recorded on, so data can be filtered and
analyzed per line.

## Step 4 — Configure Report Parameters

For each of the six report types, the Super Admin (or an Admin) defines the
fields the form will contain: what each field is called, what type of input it
accepts, whether it is mandatory or visible, its order on the form, and any
limits or default values.

## Step 5 — System Ready

The configuration is saved and the system is live. Staff can begin entering
reports on their next shift, and Admins can begin approving.

---

# 5. Daily Staff Workflow

This is what an operator does on a normal day:

```
Login
     │
     ▼
System determines current shift automatically
     │
     ▼
Select Report Type
     │
     ▼
Select Production Line
     │
     ▼
System loads report parameters
     │
     ▼
Staff fills observations
     │
     ▼
Create (stored as DRAFT)
     │
     ▼
Submit
```

## 1. Login

The operator signs in with their credentials. The system knows who they are and
what role they hold, and shows them only what they are allowed to do.

## 2. System determines current shift automatically

The system compares the current time against the configured shift schedule and
assigns the active shift to the report. The operator does not choose a shift —
it is always correct, including across midnight for overnight shifts.

## 3. Select Report Type

The operator picks which of the six standard reports they are completing (for
example, Process Monitoring or Daily Inspection).

## 4. Select Production Line

The operator chooses the production line the observations relate to.

## 5. System loads report parameters

The system loads the exact form the factory configured for that report type. The
form order, mandatory fields, defaults, and input types are all respected
automatically. The factory's configuration — not the operator — decides what the
form looks like.

## 6. Staff fills observations

The operator enters their readings and observations. Mandatory fields must be
completed; values respect any configured minimum/maximum limits.

## 7. Create (stored as Draft)

Creating the report stores it as a **DRAFT** — the state it is in before
submission. There is no separate "save draft" step: the report is created in a
single request and stays in `DRAFT` until submitted.

> ⚠️ **V1 limitation:** a draft cannot be edited, resumed, or updated after
> creation — there is no report update endpoint. Drafts that are wrong must be
> deleted (SUPER_ADMIN only) and recreated.

## 8. Submit

When the report is complete, the operator submits it. The report is locked and
sent to the approval queue. From this moment it can no longer be edited by the
operator.

---

# 6. Report Lifecycle

Every report moves through a defined lifecycle:

```
            ┌──────────────────────┐
            │       DRAFT         │
            │   (just created)    │
            └─────────┬────────────┘
                      │ Submit
                      ▼
            ┌──────────────────────┐
            │      SUBMITTED      │  ──► locked; in approval queue
            └─────────┬────────────┘
                      │ Approve / Reject
          ┌───────────┴────────────┐
          ▼                        ▼
   ┌──────────────┐        ┌──────────────┐
   │   APPROVED   │        │  REJECTED    │
   │ (permanent,  │        │  (terminal   │
   │  read-only)  │        │   in V1)     │
   └──────────────┘        └──────────────┘
```

> ⚠️ **V1 limitation:** there is **no** edit/resume/re-submit path. The intended
> future "REJECTED → edit → resubmit → SUBMITTED" loop is **not implemented** —
> a rejected report has no terminal transition in the current backend. See
> `CURRENT_STATE.md` and `FEATURES_ROADMAP.md`.

## Draft

- **Who:** Staff (operator).
- **What:** The report has just been created and not yet submitted. It has no
  effect on dashboards or approval queues.
- **Restrictions:** A draft cannot be edited, resumed, or updated after creation
  (no update endpoint). Deleting a draft requires the `SUPER_ADMIN` role.
  Unsubmitted drafts are simply incomplete.

## Submitted

- **Who:** Staff submits; Admin/Super Admin reviews.
- **What:** The report is complete and locked. It enters the approval queue and
  counts toward pending approvals.
- **Restrictions:** The operator can no longer edit or delete it. Only
  approve/reject is possible from this point.

## Approved

- **Who:** Admin or Super Admin.
- **What:** The report is confirmed and becomes part of the permanent record —
  visible on the dashboard, in history, analytics, and search.
- **Restrictions:** Approved reports are **read-only** for everyone. They can
  never be edited or deleted.

## Rejected

- **Who:** Admin or Super Admin.
- **What:** The report has a problem (missing value, wrong reading, incomplete
  entry). It is marked `REJECTED` with remarks explaining why.
- **Restrictions:** In Version 1 `REJECTED` is a **terminal** state — there is
  no edit/resubmit endpoint to return it to the approval queue. This is a
  documented gap pending client scope decision.

## ⚠️ Edit → Resubmit → Approved (not implemented)

This section documents the **designed** behavior only. It is **not supported**
in Version 1:

- **Designed:** Staff edits a rejected report; Staff resubmits; Admin/Super
  Admin re-approves.
- **Status:** ❌ **Not supported in Version 1.** There is no report update
  endpoint (`PUT`/`PATCH`) and no resubmit transition in the current backend.
  This is the single capability gap found in the business-workflow verification;
  see `CURRENT_STATE.md` and `FEATURES_ROADMAP.md`. ⚠️ Planned for a future
  version pending client scope decision.

---

# 7. Approval Workflow

## Who approves

Reports are approved or rejected by **Admins** and **Super Admins**. Staff
create and submit; they never approve their own work. This separation of duties
keeps the record trustworthy.

## When approval happens

Approval happens after a report is submitted. The report moves from the
operator's working area into the **pending approval** queue, where reviewers see
it alongside everything else awaiting a decision.

## When rejection happens

A reviewer rejects a report when it is incomplete or incorrect — for example, a
mandatory reading is missing, a value is out of range, or the wrong line was
selected. The reviewer attaches remarks so the operator knows exactly what to
fix.

## What staff should do after rejection

> ⚠️ **Not supported in Version 1.** The designed flow — the operator opens the
> rejected report, reads the remarks, corrects the issue, and submits it again —
> requires a report update endpoint that does not exist in the current backend.
> In V1 a rejected report is **terminal**: there is no edit/resubmit path. This
> is the single capability gap pending client scope decision — see
> `CURRENT_STATE.md`.

## What happens after approval

The report becomes the approved, permanent record. It is:

- Locked and read-only for all users.
- Counted in dashboard and analytics totals.
- Available in history and global search.
- Final — it can never be changed.

---

# 8. Master Data Workflow

Master data is the configuration that makes the system fit a specific factory.

## Users

- **Configured by:** Super Admin (create, update, activate/deactivate, delete).
- **How:** Each user gets a login, a name, and a role. Deactivated users cannot
  log in but their historical reports remain intact.
- **Effect on reports:** Every report records who created, submitted, and
  approved it — so users are the foundation of accountability.

## Shifts

- **Configured by:** Super Admin and Admin (create/update); Super Admin only
  (delete).
- **How:** Each shift has a name, start time, and end time.
- **Effect on reports:** Shift definitions drive automatic shift assignment, so
  every report is stamped with the correct shift without staff choosing it.

## Lines

- **Configured by:** Super Admin and Admin (create/update); Super Admin only
  (delete).
- **How:** Each line is a production line the factory operates.
- **Effect on reports:** Every report is tied to a line, enabling per-line
  filtering, dashboards, and analytics.

## Report Types

- **Configured by:** None (fixed for Version 1).
- **How:** The system ships with six predefined report types. They cannot be
  created, renamed, or removed in Version 1.
- **Effect on reports:** Report types are the categories of reports the factory
  produces; each has its own configured template.

## Parameters

- **Configured by:** Super Admin and Admin (create/update); Super Admin only
  (delete).
- **How:** Each parameter defines one field of a report type's form — its name,
  order, input type, mandatory/visible flags, defaults, and limits.
- **Effect on reports:** Parameters are the actual fields staff fill in. They
  determine what each report captures and how it is validated.

---

# 9. Parameter Configuration

Each report type's form is built from **parameters**. The factory configures
each parameter once, and every report of that type then renders exactly as
configured. The attributes are:

| Attribute | Meaning | Effect on report generation |
|-----------|---------|------------------------------|
| **Display Order** | The position of the field on the form | Controls the reading order of the form and the saved report. |
| **Input Type** | What kind of input the field accepts (text, number, etc.) | Determines how staff enter the value and how it is validated. |
| **Mandatory** | Whether the field must be filled | If mandatory, the report cannot be saved/submitted with this field empty. |
| **Visible** | Whether the field appears on the form | Hidden fields are not shown to staff (e.g., an internally tracked value). |
| **Default Value** | The value pre-filled when the form opens | Speeds up entry for values that are usually the same. |
| **Minimum** | The lowest acceptable value | Values below the minimum are rejected. |
| **Maximum** | The highest acceptable value | Values above the maximum are rejected. |
| **Unit** | The unit of measurement (e.g., °C, kg, bar) | Displayed beside the field so readings are unambiguous. |
| **Active** | Whether the parameter is currently in use | Inactive parameters are excluded from new reports; historical values remain. |

## How parameters influence report generation

When staff open a report type, the system builds the form from the active
parameters **in display order**. Mandatory fields are enforced, default values
are pre-filled, limits are checked, and units are shown. Because the form is
data-driven, changing the configuration changes every future report of that
type — with no software change.

---

# 10. Automatic Shift Detection

## How shifts work

Shifts are time windows the factory defines: each shift has a name, a start
time, and an end time (for example, Day 06:00–14:00, Evening 14:00–22:00, Night
22:00–06:00).

## How startTime/endTime are used

When staff create a report, the system looks at the **current time** and finds
the shift whose window contains it. That shift is automatically attached to the
report. Staff never choose a shift — the system always assigns the correct one.

## How overnight shifts work

Shifts that cross midnight are fully supported. A Night shift of 22:00–06:00
correctly captures reports created just after midnight, because the system
understands that the end time is on the following day.

## Why users don't manually select shifts

Automatic detection guarantees accuracy:

- No forgotten or mistaken shift selections.
- Consistent shift labels across every report and every department.
- Reports always sort and filter correctly by shift.
- Staff save time — one less thing to fill in.

---

# 11. Dashboard Workflow

The dashboard is the management cockpit — a live view of everything happening in
the plant. It is available to all roles.

## Dashboard Overview

High-level totals: how many reports exist, and how they break down by type,
shift, and line.

## Today's Reports

The reports created today, so management can see that daily reporting is
happening on time.

## Pending Approvals

The reports sitting in the approval queue awaiting a decision. This is the
approver's primary work queue — it tells them exactly what needs their action.

## Approval Summary

Approval health at a glance: how many reports are pending, approved, or
rejected, how many were decided today, and the approval rate. Management can see
whether the team is keeping up with approvals.

## Recent Activity

A live feed of recent report lifecycles — created, approved, rejected — in
newest-first order. Management sees the pulse of the operation as it happens.

## How management uses the dashboard

- **Operational control:** spot whether reports are being created on every shift
  and every line.
- **Workload management:** clear the pending-approval queue promptly.
- **Quality monitoring:** watch approval/rejection rates for signals about
  reporting quality.
- **Quick navigation:** recent activity and today's reports link directly into
  the underlying reports.

---

# 12. Global Search Workflow

The system provides one search box across everything: reports, users, and
parameters. Any authenticated user can search from anywhere in the application.

## How users search

The user types a keyword or applies filters, and the system instantly returns
matching reports, users, and parameters. Results are grouped by type, can be
narrowed to a single category, and are paginated for easy browsing.

## Search examples

| You want to find... | You search by... |
|---------------------|------------------|
| The operator who logged a specific reading | **Employee** name |
| A specific document for the files | **Report Number** (e.g. PMR-20260612-00001) |
| Every report that mentioned a particular value | **Parameter** |
| What happened on the night shift last week | **Shift** |
| All output from Line 2 this month | **Line** |
| Reports still waiting for approval | **Status** (pending/approved/rejected) |
| All Process Monitoring reports | **Report Type** |
| Activity between two dates | **Date range** |

Results link directly to the full report so staff can open details, and
approvers can act on them, straight from search.

---

# 13. Data Flow

Data flows through the system in one continuous stream, from configuration to
permanent record:

```
Parameter Configuration
     │
     ▼
Report Creation
     │
     ▼
Validation
     │
     ▼
Submission
     │
     ▼
Approval
     │
     ▼
Dashboard
     │
     ├──► Search
     │
     └──► History
```

## Parameter Configuration

The factory defines the fields for each report type. This configuration is the
template every future report is built from.

## Report Creation

Staff open a report type, the configured template is loaded, and observations
are entered. The report is tied to a line, the auto-detected shift, and its
creator.

## Validation

The system enforces the configured rules: mandatory fields, value limits, input
types. Incomplete or out-of-range reports cannot move forward.

## Submission

The completed report is locked and enters the approval queue.

## Approval

An Admin or Super Admin reviews it. Approval finalizes the record; rejection
sends it back for correction and resubmission.

## Dashboard / Search / History

Once approved, the report flows into the dashboard totals, becomes searchable,
and is stored permanently in history. Every approved report is permanently
traceable back through this chain — from dashboard to report to the parameters
and configuration that produced it.

---

# 14. Screen Flow

Expected frontend navigation, from launch to daily use:

```
Splash
  │
  ▼
Login
  │
  ▼
Dashboard
  │
  ▼
Role Based Navigation
  │
  ├──► Reports
  ├──► Approval
  ├──► Settings
  └──► Search
```

## Splash

The application opens with the product identity and a transition into login.

## Login

The user signs in with their credentials. After authentication, the user is
directed to the dashboard with navigation tailored to their role.

## Dashboard

The main landing screen: overview totals, today's reports, pending approvals,
approval summary, and recent activity. It is the starting point for everyone.

## Role Based Navigation

Navigation adapts to the logged-in role:

- **Staff** see Reports, Dashboard, and Search.
- **Admins** additionally see Approval, master data (shifts, lines, parameters),
  and read-only views of users, settings, and audit logs.
- **Super Admins** see everything, including users, settings, integrations, and
  audit logs.

## Reports

Create, view, submit, and browse report history. Staff live here; approvers land
here when they want details on a specific report.

> ⚠️ **V1 limitation:** existing drafts and submitted reports cannot be edited —
> there is no update endpoint. A report is created in one step (as DRAFT) and
> then only submitted / approved / rejected / deleted.

## Approval

The approver's queue: pending reports awaiting a decision, each openable for
review with approve/reject actions and remarks.

## Settings

Master data and system configuration: users, shifts, lines, report parameters,
system settings, and integrations (Super Admin; Admins get read/limited views).

## Search

The global search screen: one search box across reports, users, and parameters,
with filters for type, status, shift, line, and date range.

---

# 15. Business Rules

The system operates on a fixed set of business rules:

1. **Only the Super Admin manages users** — creates, updates, deactivates, and
   deletes accounts.
2. **Only the Super Admin deletes master data** — shifts, lines, parameters, and
   reports. Admins can create/update but not delete.
3. **Staff create reports** — operators are the ones recording daily
   observations. In the current backend any authenticated user may create
   reports; in practice Staff are the operators who do so.
4. **Only Admins and Super Admins approve reports** — staff never approve their
   own or anyone's reports.
 5. **Approved reports become read-only** — a permanent record that cannot be
    edited or deleted by anyone.
 6. **Rejected reports are terminal in Version 1** — there is no edit/resubmit
    endpoint. ⚠️ *The designed "edit and resubmit" loop is pending client scope
    decision — see `CURRENT_STATE.md`.*
 7. **Submitted reports are locked** — an operator cannot edit or delete a report
    once submitted.
8. **The shift is determined automatically** — staff never select a shift;
   the system assigns the current shift, including overnight.
9. **Parameters are loaded dynamically** — report forms are built from the
   factory's configuration, not hard-coded.
10. **Display order controls UI order** — parameters render in their configured
    sequence.
11. **Mandatory parameters must be filled** — a report cannot be submitted with
    a mandatory field empty.
12. **Value limits are enforced** — readings must respect configured minimums
    and maximums.
13. **Report numbers are unique per day and type** — each report gets a
    sequential number under its report type's prefix and date.
14. **Deactivated users keep their history** — accounts can be disabled without
    losing the reports they created.
15. **Report types are fixed in Version 1** — the six standard types cannot be
    created, renamed, or removed.

---

# 16. Version 1 Scope

Version 1 delivers the complete daily operation cycle.

## Included in Version 1

- ✓ **Authentication** — secure login for all users with role-based access.
- ✓ **User & Master Data Management** — users, shifts, production lines, report
  types, and report parameters.
- ✓ **Standard Reports** — the six predefined report types with configurable
  templates and full create → draft → submit flow.
- ✓ **Approval Workflow** — submit, approve, and reject with remarks.
- ✓ **Dashboard** — overview, today's reports, pending approvals, approval
  summary, recent activity.
- ✓ **Analytics** — management statistics across reports.
- ✓ **Global Search** — search across reports, users, and parameters.
- ✓ **Attachments** — attach files to reports.
- ✓ **Notifications** — the notification inbox for reading system messages.
- ✓ **System Settings & Integrations** — configuration and external integration
  records.
- ✓ **Documentation** — API documentation, this business specification, and the
  project blueprint.

## Report capabilities at a glance

| Capability | Version 1 |
|---|---|
| Create report (stored as DRAFT) | ✓ Implemented |
| View / list / search / filter reports | ✓ Implemented |
| Submit draft for approval | ✓ Implemented |
| Approve / reject with remarks | ✓ Implemented |
| Delete draft | ✓ Implemented (SUPER_ADMIN only) |
| Dashboard, analytics, history | ✓ Implemented |
| **Edit an existing draft** | ✗ **Not supported** — no update endpoint |
| **Resume / save draft changes later** | ✗ **Not supported** — no update endpoint |
| **Edit rejected report and resubmit** | ✗ **Not supported** — no update/resubmit endpoint (⚠️ planned for a future version) |

## Not in Version 1

- ✗ **Custom report creation** — the six standard report types are fixed.
  Factories configure their templates, but cannot define brand-new report types.
- ✗ **Backend export** — report export is generated by the frontend from
  structured data, not by the backend.
- ✗ **Edit/resume drafts** — no report update endpoint (see table above).
- ✗ **Reject → edit → resubmit** — no resubmit transition (see table above).

---

# 17. Future Scope (Version 2+)

The following are future enhancements only. They are documented here so the
roadmap is explicit; none of them are required for Version 1 operation.

| Future feature | Description |
|----------------|-------------|
| **Notifications** | Automated notifications triggered by workflow events (e.g., "your report was rejected", "new report awaiting approval"). |
| **Audit Logs** | Automatic recording of every write action for a complete, tamper-evident audit trail. |
| **AI Insights** | Anomaly detection and reading trend analysis over report data. |
| **ERP Integration** | Push/pull integration with ERP systems for production data. |
| **Offline Sync** | Capture reports with no connectivity and sync when back online. |
| **Attachments** | Attach files per report entry (currently files attach to reports as a whole). |
| **Advanced Analytics** | Parameter-level analytics and per-report-type dashboard widgets. |
| **Custom Report Builder** | Let factories define brand-new report types without software changes. |
| **Versioned Templates** | History of template/parameter changes so configuration evolution is traceable. |

---

# 18. Frontend Integration Notes

Guidance for how the frontend should consume the backend to deliver the business
workflow described in this document.

## Dynamic parameter loading

Report forms must be **rendered from the configuration**, not hard-coded. When a
user selects a report type, fetch that type's parameters and render fields in
**display order**, respecting input type, mandatory/visible flags, default
values, units, and min/max limits. This is how a factory's template changes take
effect without a release.

## Automatic shift handling

The backend resolves the current shift automatically when a report is created.
The frontend should **not** prompt staff to pick a shift; it displays the
assigned shift as informational only.

## Pagination

All list endpoints return data in pages. The frontend should implement
paging controls (page number, page size) and persist them consistently across
lists, dashboards, and search results.

## Filtering

Lists support filtering (keyword, role, status, shift, line, type, date range).
The frontend should expose these as user-friendly filters on each list and
combine them with pagination.

## Search

One global search across reports, users, and parameters. The search screen
should let users narrow by category and apply the same filters as the lists.

## Approval

The approval queue drives the reviewer's day. The frontend should make pending
items prominent, allow viewing the full report, and offer approve/reject with a
remarks field on rejection. Approved/rejected states must update the queue and
dashboard immediately.

## Role-based navigation

Navigation must be driven by the logged-in role: hide or disable screens the
user cannot act on. Use the role to decide whether to show Approval, Settings,
master data management, and user management.

## State management expectations

The frontend should manage these key states:

- **Report states** — draft, submitted, approved, rejected; UI must reflect the
  allowed actions for each state. In Version 1:
  - `DRAFT` → submit; delete (SUPER_ADMIN only). **No edit/resume.**
  - `SUBMITTED` → view only (approve/reject are reviewer actions).
  - `APPROVED` → read-only.
  - `REJECTED` → read-only (terminal in V1; edit/resubmit is not implemented).
- **Auth state** — login/logout and token refresh; re-route to login on expiry.
- **Configuration state** — parameters and master data are the source of truth
  for forms; refresh when configuration changes.
- **Dashboard state** — live refresh so pending counts and recent activity
  stay current.

---

# 19. Frequently Asked Questions

**Who creates users?**
Only the Super Admin. Users are created with a role and can be deactivated
without losing their history.

**Can staff edit approved reports?**
No. Approved reports are permanent and read-only for every role.

**How are shifts determined?**
Automatically. The system compares the current time to the configured shift
schedule and assigns the active shift — including overnight shifts — so staff
never choose one.

**Can parameters be hidden?**
Yes. A parameter's **visible** flag controls whether it appears on the form.
Hidden parameters can still hold values managed elsewhere.

**Can reports be searched?**
Yes. Global search covers reports, users, and parameters, filterable by type,
status, shift, line, employee, report number, and date range.

**Can rejected reports be edited?**
No, not in Version 1. A rejected report is terminal — there is no edit/resubmit
endpoint in the current backend. Editing a rejected report and resubmitting it
is ⚠️ planned for a future version.

**Can users create new report types?**
No, not in Version 1. The six standard report types are fixed. Factories
configure the templates (parameters) of those six types, but cannot create new
ones. A custom report builder is planned for a future version.

**Who can approve reports?**
Admins and Super Admins. Staff submit; they never approve.

**Who can delete master data or reports?**
Only the Super Admin. Admins can create and update but are intentionally denied
delete rights.

**What happens if a mandatory field is empty?**
The report cannot be submitted. Validation blocks the action until the required
value is entered.

**Are values validated against limits?**
Yes. Values must respect the configured minimum and maximum for each parameter.

---

# 20. Conclusion

The CED Operations Management System digitizes the entire manufacturing
reporting process: configuration, daily entry, submission, approval, and
permanent record. Paper forms become structured, validated, searchable digital
reports; manual shift guessing becomes automatic; and ad-hoc clipboards become a
live management dashboard.

The business workflow is designed to be flexible by construction. Master data —
shifts, lines, and report parameters — is configuration, so each factory can
shape the system to how it actually works, without software changes. Staff fill
the forms their factory configured; approvers clear a queue; management watches
live.

The architecture is built to scale. The six Version 1 report types are served by
a single shared **report engine**, so every report type behaves consistently —
same lifecycle, same validation, same workflow, same dashboard and search
behavior. Adding a future report type means registering a new template on that
engine: the new report immediately inherits the full lifecycle, approval,
dashboard, analytics, and search behavior that every existing report has. The
system grows by adding reports, not by reworking the workflow.

The paper era is over. Reporting is now instant, accurate, approved, and
permanent.
