---
sidebar_position: 2
---

# Docker Compose

[Docker compose](https://docs.docker.com/engine/install/) is the recommended way to install and run Tikito. 

## Step 1 - Create files
Create a directory that will contain the `docker-compose.yaml` and `.env` files:

```bash title="Create the directory"
mkdir ./tikito
cd ./tikito
```

Download the `docker-compose.yaml` and `.env` files:

```bash title="Get docker-compose.yaml file"
wget -O docker-compose.yaml https://raw.githubusercontent.com/Tikito-app/tikito/refs/heads/main/docker-compose.yaml
```

```bash title="Get .env file"
wget -O .env https://raw.githubusercontent.com/Tikito-app/tikito/refs/heads/main/example.env
```

## Step 2 - Configuration

```
TIKITO_API_HOSTNAME=http://localhost
TIKITO_API_PORT=4242
TIKITO_UI_PORT=8080

DB_USER=tikito
DB_PASSWORD=fill-in-a-random-password-here
```

## Step 3 - Start
Start the containers:

```bash title="Start conatiners"
docker-compose up -d
```

## Next steps

Read the [Post installation](/docs/installation/post-install) steps.