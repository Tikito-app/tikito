---
sidebar_position: 2
---

# Docker Compose

[Docker Compose](https://docs.docker.com/engine/install/) is the recommended way to install and run Tikito.

## Step 1 - Create files

Create a directory that will contain the `docker-compose.yaml` and `.env` files:

```bash title="Create the directory"
mkdir ./tikito
cd ./tikito
```

Download the `docker-compose.yaml` and `.env` files:

```bash title="Get docker-compose.yaml"
wget -O docker-compose.yaml https://raw.githubusercontent.com/Tikito-app/tikito/refs/heads/main/docker-compose.yaml
```

```bash title="Get .env"
wget -O .env https://raw.githubusercontent.com/Tikito-app/tikito/refs/heads/main/example.env
```

## Step 2 - Configuration

Open the `.env` file and fill in the values:

```env
TIKITO_API_HOSTNAME=http://localhost
TIKITO_API_PORT=4242
TIKITO_UI_PORT=8080

DB_USER=tikito
DB_PASSWORD=fill-in-a-random-password-here

FINNHUB_TOKEN=your-finnhub-token-here
```

| Variable | Description |
|---|---|
| `TIKITO_API_HOSTNAME` | The hostname at which the backend is reachable from the browser. Use `http://localhost` for local installs. |
| `TIKITO_API_PORT` | Port the backend API listens on (default: `4242`) |
| `TIKITO_UI_PORT` | Port the frontend UI is served on (default: `8080`) |
| `DB_USER` | MariaDB username |
| `DB_PASSWORD` | MariaDB password — choose something random and strong |
| `FINNHUB_TOKEN` | API token from [Finnhub](https://finnhub.io/) — used to resolve ISINs to market symbols. A free token is sufficient. |

## Step 3 - Start

```bash title="Start containers"
docker compose up -d
```

## Next steps

Read the [Post installation](/docs/installation/post-install) steps.
