$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
New-Item -ItemType Directory -Force build/classes | Out-Null
$files = (Get-ChildItem src/course/*.java).FullName
& javac --release 21 --add-modules jdk.httpserver -d build/classes $files
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& java --add-modules jdk.httpserver -cp build/classes course.SelfTest
exit $LASTEXITCODE
