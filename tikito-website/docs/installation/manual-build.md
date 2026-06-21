---
sidebar_position: 3
---

# Manual build

## Tikito backend
```bash title="Build Tikito backend"
cd tikito-backend
mvn clean package
docker build -t tikito-api .
```

## Tikito ui
```bash title="Build Tikito ui"
cd tikito-ui
npm i
ng build --configuration=production
docker build -t tikito-ui .
```
