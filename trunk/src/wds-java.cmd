@echo OFF

if not exist server\javatestserver\JavaTestServer.class goto compile
goto startserver

:compile
echo Looks like JavaTestServer not compiled. Lets do it ...
wdscomp.cmd

:startserver
java server/javatestserver/JavaTestServer