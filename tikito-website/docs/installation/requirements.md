---
sidebar_position: 1
---

# Requirements

## Hardware

Tikito is lightweight. Any modern laptop or a small home server (e.g. a Raspberry Pi 4) can run it comfortably.

## Operating system

Any OS that supports Docker works. A Linux system is recommended for the smoothest experience.

- **Linux** — native Docker support; recommended
- **Windows** — [Docker Desktop on Windows](https://docs.docker.com/desktop/setup/install/windows-install/) or [WSL](https://docs.docker.com/desktop/features/wsl/)
- **macOS** — [Docker Desktop on Mac](https://docs.docker.com/desktop/setup/install/mac-install/)

## Docker

Tikito uses [Docker](https://docs.docker.com/get-started/get-docker/) with the **Docker Compose** plugin. Make sure both are installed and that the Compose plugin is available (`docker compose version`).

## Finnhub token

Tikito uses the [Finnhub](https://finnhub.io/) API to resolve an ISIN to a market symbol when you first enrich a security. A **free** Finnhub account is sufficient — sign up and copy your API token.

You only need the Finnhub token if you import security (stock/ETF) transactions. It is not required for money-only setups.
