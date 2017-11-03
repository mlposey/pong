#!/bin/bash

REMOTE=marcusposey@srv.marcusposey.com

docker login

docker build -t mlposey/pong-server -f server/Dockerfile server/
docker push mlposey/pong-server

scp -r client docker-compose.yaml $REMOTE:/home/marcusposey/pong/

ssh $REMOTE << EOF
    cd ~/pong
    sudo docker-compose down
    sudo docker-compose pull
    sudo -E docker-compose up -d
EOF