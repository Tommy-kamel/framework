Write-Host "Starting deployment process..."

Write-Host "Compiling framework project..."
Set-Location "D:\PROG\JAVA\S5\Mr Naina\Sprint1\Framework\framework"
& mvn clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host "Framework compilation failed!"
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Creating lib directory if not exists..."
New-Item -ItemType Directory -Path "D:\PROG\JAVA\S5\Mr Naina\Sprint1\test\src\main\webapp\WEB-INF\lib" -Force | Out-Null

Write-Host "Copying JAR to test project lib..."
Copy-Item "target\mini-framework-1.0-SNAPSHOT.jar" "D:\PROG\JAVA\S5\Mr Naina\Sprint1\test\src\main\webapp\WEB-INF\lib"
if ($LASTEXITCODE -ne 0) {
    Write-Host "JAR copy failed!"
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Compiling and packaging test project..."
Set-Location "D:\PROG\JAVA\S5\Mr Naina\Sprint1\test"
& mvn clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host "Test packaging failed!"
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Deploying WAR to Tomcat..."
Get-ChildItem "target\*.war" | ForEach-Object { Copy-Item $_.FullName "C:\apache-tomcat-10.1.28\webapps" }
if ($LASTEXITCODE -ne 0) {
    Write-Host "Deployment to Tomcat failed!"
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Deployment completed successfully!"
Read-Host "Press Enter to exit"