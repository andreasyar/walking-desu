if (! (Test-Path server/javatestserver/logs)) {
    echo "Logs directory not exist. Lets create ..."
    mkdir server/javatestserver/logs
}
if (! (Test-Path server/javatestserver/JavaTestServer.class)) {
    echo "Looks like JavaTestServer not compiled. Lets do it ..."
    wdscomp.cmd
}
echo "Starting Java Test Server."
java server/javatestserver/JavaTestServer 2>&1 | tee server/javatestserver/logs/$(date -uformat "%Y-%m-%d-%H-%M-%S").log
