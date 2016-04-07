#!/bin/bash
set -x

APP_KEY='EyZMaqRrMEOrylKjgNluYwlOmOqaqn42'
TITLE=$1
MESSAGE=$2

curl -H "X-ZUMO-APPLICATION: $APP_KEY" \
     -H 'Content-Type: application/json' \
     https://shoppyservice.azure-mobile.net/api/broadcast \
     --data "{\"title\":\"$TITLE\", \"message\":\"$MESSAGE\"}"
