---
sidebar_position: 4
---

# Background jobs

Tikito uses a background job system to process long-running calculations without blocking the UI.

## When jobs are created

| Trigger | Jobs created |
|---|---|
| Import money transactions | Recalculate historical money values for the affected holdings |
| Import security transactions | Enrich new securities, fetch historical prices, recalculate historical security values |
| New security holding detected | Enrich security (resolve ISIN → market symbol, fetch metadata) |
| Manual action from Admin panel | Enrich, update prices, or recalculate historical values for a specific security |
| Nightly schedule (2 am) | Refresh currency exchange rates |
| Nightly schedule (3 am) | Update security prices and recalculate all historical values |

## Job lifecycle

Jobs are persisted in the database and processed asynchronously. Each job has a status:

- **Pending** — queued and waiting to be picked up
- **Running** — currently being processed
- **Completed** — finished successfully
- **Failed** — an error occurred; details are logged

## Viewing jobs

Go to **Admin → Jobs** to see the list of recent jobs, their status, and any error messages. This is useful for troubleshooting if security prices or historical values are not updating as expected.

## Logs

Tikito also writes detailed logs for each job. Go to **Admin → Logs** to inspect what happened during processing.
