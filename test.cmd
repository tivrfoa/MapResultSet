@echo off

:: $env:Path += ";C:\Users\lia\Documents\leandro\zulu17.34.19-ca-jdk17.0.3-win_x64\zulu17.34.19-ca-jdk17.0.3-win_x64\bin"

javac ParseQuery.java

if errorlevel 1 (
	exit 1
)

java -ea ParseQuery