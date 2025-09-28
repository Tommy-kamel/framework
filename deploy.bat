@echo off
echo Starting deployment process...

echo Compiling framework project...
cd /d "D:\PROG\JAVA\S5\Mr Naina\Sprint1\Framework\framework"
mvn clean package
if %errorlevel% neq 0 (
    echo Framework compilation failed!
    pause
    exit /b 1
)

echo Creating lib directory if not exists...
mkdir "D:\PROG\JAVA\S5\Mr Naina\Sprint1\test\src\main\webapp\WEB-INF\lib" 2>nul

echo Copying JAR to test project lib...
copy target\mini-framework-1.0-SNAPSHOT.jar "D:\PROG\JAVA\S5\Mr Naina\Sprint1\test\src\main\webapp\WEB-INF\lib"
if %errorlevel% neq 0 (
    echo JAR copy failed!
    pause
    exit /b 1
)

echo Compiling test project...
cd /d "D:\PROG\JAVA\S5\Mr Naina\Sprint1\test"
mvn clean compile
if %errorlevel% neq 0 (
    echo Test compilation failed!
    pause
    exit /b 1
)

echo Deploying to Tomcat...
xcopy "D:\PROG\JAVA\S5\Mr Naina\Sprint1\test\src\main\webapp" "C:\apache-tomcat-10.1.28\webapps\test" /E /I /H /Y
if %errorlevel% neq 0 (
    echo Deployment to Tomcat failed!
    pause
    exit /b 1
)

echo Deployment completed successfully!
pause