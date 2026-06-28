---
sidebar_position: 7
---

# Aggregated historical value

The aggregated historical value is the sum of all [historical values](/docs/concepts/historical-value) for all holdings of the same type (money or securities) on a specific date. It gives you a single number representing your total position across all accounts on any given day.

## How it works

For each day, Tikito sums the [historical value](/docs/concepts/historical-value) of every individual holding (for that type) and stores the result. This makes the overview chart fast to render — no recalculation is needed at query time.

Aggregated values are recalculated automatically whenever underlying historical values change, for example after a new import or a nightly price update.

## Overview dashboard

The overview dashboard shows aggregated historical values for:

- **Total money** — the combined balance across all money holdings
- **Total securities** — the combined market value across all security holdings

You can filter by date range and choose which accounts to include.
