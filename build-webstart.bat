@echo off
set argC=0
for %%x in (%*) do Set /A argC+=1
IF %argC% GTR 0 (
    IF %argC% NEQ 4 (
        echo "Usage: build-webstart.bat <keystore_file> <keystore_alias> <keystore_password> <key_password>, or no arguments for self-signed certificate"
    ) else (
        set KEYSTORE_FILE=%1
        set KEYSTORE_ALIAS=%2
        set KEYSTORE_PASSWORD=%3
        set KEY_PASSWORD=%4
        echo Building with GA's certificate...
        set MAVEN_OPTS=-Xmx2048m -XX:MaxPermSize=256m
        call mvn package -Dtycho.localArtifacts=ignore -Dwebstart=true
    )
) else (
    echo Building with self-signed certificate...
    set KEYSTORE_FILE=keystore
    set KEYSTORE_ALIAS=selfsigned
    set KEYSTORE_PASSWORD=password
    set KEY_PASSWORD=password
    set MAVEN_OPTS=-Xmx2048m -XX:MaxPermSize=256m
    call mvn package -Dtycho.localArtifacts=ignore -Dwebstart=true
)

pause