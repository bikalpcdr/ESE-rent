#!/bin/bash
export JAVA_HOME=$(nix-build -E '(import <nixpkgs> {}).jdk11' --no-out-link)/lib/openjdk
export PATH=$JAVA_HOME/bin:$PATH
cd CarRentalSystem
# Set the server to listen on all interfaces (0.0.0.0) to be accessible via Replit
java -jar target/eserent-0.0.1-SNAPSHOT.jar --server.address=0.0.0.0