---
sidebar_position: 3
---

# Quick start
This guide will provide you with the quickest default installation for Tikito.

## Requirements
- Any system will probably do
- [Docker](https://docs.docker.com/engine/install/)

> For a more detailed list of requirements, see the [requirements page](/docs/installation/requirements).

## Setup the backend

### Step 1 - Create files
Create a directory that will contain the `docker-compose.yaml` and `.env` files:

```bash title="Create the directory"
mkdir ./tikito
cd ./tikito
```

Download the `docker-compose.yaml` and `.env` files:

```bash title="Get docker-compose.yml file"
wget -O docker-compose.yaml https://raw.githubusercontent.com/Tikito-app/tikito/refs/heads/main/docker-compose.yaml
```

```bash title="Get .env file"
wget -O .env https://raw.githubusercontent.com/Tikito-app/tikito/refs/heads/main/example.env
```

### Step 2 - Enter the Finnhub token
Tikito uses Finnhub to get financial information. 
Don't worry, you can get a free token
In the .env file, you have to write the [Finnhub](https://finnhub.io/) token (e.g. 12345):
```
TIKITO_API_HOSTNAME=http://localhost
TIKITO_API_PORT=4242
TIKITO_UI_PORT=8080

DB_USER=tikito
DB_PASSWORD=some-random-password
FINNHUB_TOKEN=12345
```
Tikito uses Finnhub to convert an isin to a symbol for stock securities.

### Step 3 - Start
Start the containers:

```bash title="Start conatiners"
docker-compose up -d
```

### Step 4 - Setup of the system
Open [http://localhost:8080](http://localhost:8080) and you will see the screen below to register the admin user.

![alt text](/tikito-screenshots/initial-installation.png)

### Try Tikito
Try uploading an export of your banking or stock portfolio system.

