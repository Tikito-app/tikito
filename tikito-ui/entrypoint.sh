#!/bin/sh
envsubst < /usr/share/nginx/html/assets/env.js.template > /usr/share/nginx/html/assets/env.js
exec "$@"
