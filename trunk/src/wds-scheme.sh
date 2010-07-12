#!/bin/sh
if !(test -d server/schemetestserver/logs); then
    echo Logs directory not exist. Lets create ...
    mkdir server/schemetestserver/logs
fi
echo Starting Scheme Test Server.
mzscheme --script server/schemetestserver/desu.scm 45001 2>&1 | tee server/schemetestserver/logs/`date +%Y-%m-%d-%H-%M-%S`.log
