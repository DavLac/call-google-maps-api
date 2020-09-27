#!/bin/sh

./gradlew -Pprod bootJar jibDockerBuild
docker-compose -p container -f docker-compose.yml up -d
read -n 1 -s -r -p "Press any key to continue"
