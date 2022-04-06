#!/bin/bash
docker pull postgres
docker network create mynetwork
docker run --name cashinDatabase --net mynetwork -e POSTGRES_PASSWORD=chashin --expose 5432 -p 5432:5432 -d postgres
docker exec -u postgres -it cashinDatabase psql -d cashin -c "create table transaction('id' int, 'nameOfTerminal' varchar, 'nameOfClient' varchar, 'amount' double precision);"
sbt docker:publishLocal
docker run --name cashin --net mynetwork --expose 8080 -p 8080:8080 -d cashin:0.1.0