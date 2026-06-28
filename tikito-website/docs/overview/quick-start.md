---
sidebar_position: 3
---

# Quick start

Get Tikito running in minutes using Docker Compose.

## Requirements

- Any system (Linux recommended)
- [Docker](https://docs.docker.com/engine/install/) with the Compose plugin
- A free [Finnhub](https://finnhub.io/) API token (only needed if you track stocks/ETFs)

## Step 1 - Create files

```bash title="Create a directory and download the config files"
mkdir ./tikito && cd ./tikito
wget -O docker-compose.yaml https://raw.githubusercontent.com/Tikito-app/tikito/refs/heads/main/docker-compose.yaml
wget -O .env https://raw.githubusercontent.com/Tikito-app/tikito/refs/heads/main/example.env
```

## Step 2 - Configure

Open `.env` and fill in the values:

```env
TIKITO_API_HOSTNAME=http://localhost
TIKITO_API_PORT=4242
TIKITO_UI_PORT=8080

DB_USER=tikito
DB_PASSWORD=some-random-password

FINNHUB_TOKEN=your-token-here
```

## Step 3 - Start

```bash title="Start containers"
docker compose up -d
```

## Step 4 - Set up

Open [http://localhost:8080](http://localhost:8080) and register the admin user.

![Initial installation screen](/tikito-screenshots/initial-installation.png)

## Next steps

- [Create an account and import banking transactions](/docs/installation/import-money)
- [Import your stock portfolio](/docs/installation/import-security)
- Read [Post installation](/docs/installation/post-install) for more details
