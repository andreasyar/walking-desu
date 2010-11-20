#!/bin/sh
if !(test -d server/javatestserver/logs); then
    echo Logs directory not exist. Lets create ...
    mkdir server/javatestserver/logs
fi
if !(test -a server/javatestserver/JavaTestServer.class); then
    echo Looks like JavaTestServer not compiled. Lets do it ...
    ./wdscomp.sh
fi
echo Starting Java Test Server.
java server/javatestserver/JavaTestServer 2>&1 | tee server/javatestserver/logs/`date +%Y-%m-%d-%H-%M-%S`.log
