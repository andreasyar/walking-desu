#!/bin/sh

javac -sourcepath src \
      -d build/classes \
      -encoding "UTF-8" \
      -Xlint:unchecked -Xlint:deprecation \
      src/client/*.java

javac -sourcepath src \
      -d build/classes \
      -encoding "UTF-8" \
      -Xlint:unchecked -Xlint:deprecation \
      src/common/*.java

javac -sourcepath src \
      -d build/classes \
      -encoding "UTF-8" \
      -Xlint:unchecked -Xlint:deprecation \
      src/server/javatestserver/*.java

jar cmf MANIFEST.MF \
    wand.jar \
    img \
    -C ./build/classes client \
    -C ./build/classes common \
    -C ./build/classes server/javatestserver
