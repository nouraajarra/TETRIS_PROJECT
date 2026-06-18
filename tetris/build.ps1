# Build script for Tetris project
$ErrorActionPreference = "Stop"
$JAR_TOOL = "C:\Program Files\Java\jdk-17\bin\jar.exe"

$root  = $PSScriptRoot
$src   = "$root\src\main\java"
$out   = "$root\out"
$lib   = "$root\lib"
$jar   = "$root\tetris.jar"
$sqJar   = "$lib\sqlite-jdbc.jar"
$mailJar = "$lib\javax.mail.jar"
$actJar  = "$lib\activation.jar"

# ── Dependencies ─────────────────────────────────────────────────────────────
if (-not (Test-Path $lib)) { New-Item -ItemType Directory -Path $lib | Out-Null }

if (-not (Test-Path $sqJar)) {
    Write-Host "Downloading SQLite JDBC..."
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.43.0.0/sqlite-jdbc-3.43.0.0.jar" -OutFile $sqJar
    Write-Host "Done."
}

if (-not (Test-Path $mailJar)) {
    Write-Host "Downloading JavaMail..."
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar" -OutFile $mailJar
    Write-Host "Done."
}

if (-not (Test-Path $actJar)) {
    Write-Host "Downloading Java Activation..."
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/javax/activation/activation/1.1/activation-1.1.jar" -OutFile $actJar
    Write-Host "Done."
}

# ── Compile ───────────────────────────────────────────────────────────────────
if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Path $out | Out-Null

$sources = Get-ChildItem -Path $src -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
$tempFile = "$root\sources.txt"
[System.IO.File]::WriteAllLines($tempFile, $sources, [System.Text.Encoding]::ASCII)

Write-Host "Compiling $($sources.Count) source files..."
& javac -encoding UTF-8 -cp "$sqJar;$mailJar;$actJar" -d $out "@$tempFile"
if ($LASTEXITCODE -ne 0) { Write-Error "Compilation failed."; exit 1 }
Remove-Item $tempFile

# ── Package ───────────────────────────────────────────────────────────────────
# Extract dependencies into out/ so they're bundled
Write-Host "Packaging jar..."
Push-Location $out
& $JAR_TOOL xf $sqJar
& $JAR_TOOL xf $mailJar
& $JAR_TOOL xf $actJar
Pop-Location
# Only remove MANIFEST.MF to avoid conflicts; keep META-INF/services for JDBC driver discovery
Remove-Item -Force "$out\META-INF\MANIFEST.MF" -ErrorAction SilentlyContinue

$manifest = "$root\MANIFEST.MF"
"Manifest-Version: 1.0`r`nMain-Class: tetris.Main`r`n" | Out-File -FilePath $manifest -Encoding ascii
& $JAR_TOOL cfm $jar $manifest -C $out .
Remove-Item $manifest

Write-Host ""
Write-Host "Build successful! Run with:"
Write-Host "  java -jar tetris.jar"
