@echo off
set MAVEN_OPTS=-Xmx2048m -XX:MaxPermSize=256m
call mvn -Dwebstart=true package
pause
