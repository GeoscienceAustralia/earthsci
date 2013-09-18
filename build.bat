@echo off
set MAVEN_OPTS=-Xmx2048m -XX:MaxPermSize=256m
call mvn package -Dtycho.localArtifacts=ignore
pause
