@echo off
SETLOCAL EnableDelayedExpansion

REM Get the scale value from the command line parameter
SET SCALE=%1

REM Open the docker-compose.yml file to write data
echo version: "3" > docker-compose.yml
echo services: >> docker-compose.yml

REM Add Redis services based on the scale
FOR /L %%i IN (1,1,%SCALE%) DO (
    echo. >> docker-compose.yml
    echo  redis%%i^: >> docker-compose.yml
    echo    image: redis:latest >> docker-compose.yml
    echo    command: redis-server --port 700%%i --cluster-enabled yes --cluster-config-file nodes.conf --cluster-node-timeout 5000 --appendonly yes >> docker-compose.yml
    echo    volumes: >> docker-compose.yml
    echo      - ./redis%%i:/data >> docker-compose.yml
    echo    ports: >> docker-compose.yml
    echo      - "700%%i:700%%i" >> docker-compose.yml
)
echo. >> docker-compose.yml
echo  redis-trib^: >> docker-compose.yml
echo    image: redis:latest >> docker-compose.yml

SET strHOSTPORT=
FOR /L %%i IN (1,1,%SCALE%) DO (
	SET PORT=700%%i
    SET "strHOSTPORT=!strHOSTPORT! redis%%i:!PORT!" 
)
echo    command: echo yes ^| redis-cli --cluster create !strHOSTPORT! --cluster-replicas 1 >> docker-compose.yml

echo    depends_on: >> docker-compose.yml
FOR /L %%i IN (1,1,%SCALE%) DO (
    echo      - redis%%i >> docker-compose.yml
)

REM Print completion message
echo Redis cluster has been deployed with a scale of %SCALE%

ENDLOCAL
