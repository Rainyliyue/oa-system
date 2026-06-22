@echo off
set "ENV_FILE=%~dp0..\.env"
if not exist "%ENV_FILE%" (
  set "ENV_FILE=%~dp0..\.env.example"
)

if not exist "%ENV_FILE%" (
  echo Cannot find .env or .env.example.
  exit /b 1
)

for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%ENV_FILE%") do (
  if not "%%A"=="" set "%%A=%%B"
)

echo Loaded OA environment from %ENV_FILE%
echo SENTINEL_DASHBOARD=%SENTINEL_DASHBOARD%
echo SEATA_ENABLED=%SEATA_ENABLED%
echo SEATA_SERVER_ADDR=%SEATA_SERVER_ADDR%
