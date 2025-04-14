#!/bin/bash
cd CarRentalSystem
nix-shell -p jdk11 --command "java -jar target/eserent-0.0.1-SNAPSHOT.jar --server.address=0.0.0.0"