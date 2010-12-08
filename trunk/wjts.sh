#!/bin/sh

LOG_DIR=log/server/javatestserver
WJTS=bin/wjts.jar

# Check if log directory not exist create it.
if !(test -d $LOG_DIR); then
    echo "Logs directory $LOG_DIR not exist. Lets create ..."
    mkdir -p $LOG_DIR
fi

# Check if Wand java test server jar file not exist make it.
if !(test -a $WJTS); then
    echo "Looks like java test server $WJTS not maked. Lets make ..."
    ./mkwjtsjar.sh
fi

echo Starting Wand Java Test Server.
java -jar $WJTS 2>&1 | tee $LOG_DIR/`date +%Y-%m-%d-%H-%M-%S`.log
