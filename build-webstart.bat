@echo off
set MAVEN_OPTS=-Xmx2048m
call mvn -Dwebstart=true package
pause
