#!/bin/sh

export TZ='America/New_York'

cd /apps/AlternateImages

pkill -f AlternateImages
unzip -oq AlternateImages-0.0.1-SNAPSHOT.jar
java -Dspring.profiles.active=prod -cp ./:./lib/*:./:./BOOT-INF/classes:./BOOT-INF/lib/* com.joshzook.alternateimages.AlternateImagesApplication
