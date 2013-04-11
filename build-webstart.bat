@echo off
set MAVEN_OPTS=-Xmx1024m
call mvn -Dwebstart=true package
pause
