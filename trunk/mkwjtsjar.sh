#!/bin/sh

CLASSES_DIR=bin/classes

# Check if classes directory not exist create it.
if !(test -d $CLASSES_DIR); then
    echo "Classes directory $CLASSES_DIR not exist. Lets create ..."
    mkdir -p $CLASSES_DIR
fi

javac -sourcepath src \
      -d $CLASSES_DIR \
      -encoding "UTF-8" \
      -Xlint:unchecked -Xlint:deprecation \
      src/common/*.java

javac -sourcepath src \
      -d $CLASSES_DIR \
      -encoding "UTF-8" \
      -Xlint:unchecked -Xlint:deprecation \
      src/server/javatestserver/*.java

jar cmf wjtsmanifest.txt \
    bin/wjts.jar \
    -C ./bin/classes common \
    -C ./bin/classes server/javatestserver
