{
  "workflows": [
    {
      "name": "Room Rental App",
      "command": "cd CarRentalSystem && nix-shell -p jdk11 --command 'java -jar target/eserent-0.0.1-SNAPSHOT.jar'",
      "runOnOpen": true,
      "restartOn": {
        "files": [
          "CarRentalSystem/target/eserent-0.0.1-SNAPSHOT.jar"
        ]
      }
    }
  ]
}