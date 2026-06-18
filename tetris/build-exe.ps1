# =====================================================================
#  build-exe.ps1  -  Compile SANS Maven (javac) puis fabrique Tetris.exe
#  Lancer avec :  PowerShell -ExecutionPolicy Bypass -File build-exe.ps1
#  Resultat    :  dist\Tetris\Tetris.exe
# =====================================================================

$ErrorActionPreference = "Stop"

# ---- Localiser le VRAI dossier du JDK (ou se trouve jpackage) ----
function Find-JdkBin {
    # 1) Variable JAVA_HOME si definie
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\jpackage.exe"))) {
        return (Join-Path $env:JAVA_HOME "bin")
    }
    # 2) Via java.home (fiable meme avec les raccourcis Oracle "javapath")
    try {
        $line = & java -XshowSettings:properties -version 2>&1 | Where-Object { $_ -match 'java\.home' } | Select-Object -First 1
        if ($line) {
            $home = ($line -split '=', 2)[1].Trim()
            if (Test-Path (Join-Path $home "bin\jpackage.exe")) { return (Join-Path $home "bin") }
        }
    } catch {}
    # 3) Recherche dans les emplacements d'installation classiques
    $cands = Get-ChildItem "C:\Program Files\Java\*\bin\jpackage.exe" -ErrorAction SilentlyContinue
    if ($cands) { return (Split-Path $cands[0].FullName) }
    return $null
}

$jdkBin = Find-JdkBin
if (-not $jdkBin) {
    Write-Host "jpackage introuvable. Un JDK 17+ COMPLET est installe (javac 26 OK)," -ForegroundColor Red
    Write-Host "mais je ne trouve pas son dossier 'bin'. Definis JAVA_HOME vers le JDK," -ForegroundColor Red
    Write-Host "par ex.:  setx JAVA_HOME `"C:\Program Files\Java\jdk-26`"  puis rouvre le terminal." -ForegroundColor Red
    exit 1
}
$javac    = Join-Path $jdkBin "javac.exe"
$jarTool  = Join-Path $jdkBin "jar.exe"
$jpackage = Join-Path $jdkBin "jpackage.exe"
Write-Host ("JDK utilise : " + $jdkBin) -ForegroundColor Green

$root = $PSScriptRoot
$src  = Join-Path $root "src\main\java"
$res  = Join-Path $root "src\main\resources"
$out  = Join-Path $root "out"
$lib  = Join-Path $root "lib"
$sqJar   = Join-Path $lib "sqlite-jdbc.jar"
$mailJar = Join-Path $lib "javax.mail.jar"
$actJar  = Join-Path $lib "activation.jar"

# ---- 1/4  Bibliotheques ----
Write-Host "== 1/4  Bibliotheques ==" -ForegroundColor Cyan
if (-not (Test-Path $lib)) { New-Item -ItemType Directory -Path $lib | Out-Null }
if (-not (Test-Path $sqJar))   { Write-Host "Telechargement SQLite JDBC..."; Invoke-WebRequest "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.43.0.0/sqlite-jdbc-3.43.0.0.jar" -OutFile $sqJar }
if (-not (Test-Path $mailJar)) { Write-Host "Telechargement JavaMail...";   Invoke-WebRequest "https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar" -OutFile $mailJar }
if (-not (Test-Path $actJar))  { Write-Host "Telechargement Activation..."; Invoke-WebRequest "https://repo1.maven.org/maven2/javax/activation/activation/1.1/activation-1.1.jar" -OutFile $actJar }

# ---- 2/4  Compilation ----
Write-Host "== 2/4  Compilation (javac) ==" -ForegroundColor Cyan
if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Path $out | Out-Null
# IMPORTANT : on entoure chaque chemin de guillemets pour gerer les espaces du dossier
$sources = Get-ChildItem -Path $src -Recurse -Filter "*.java" | ForEach-Object { '"' + $_.FullName + '"' }
$tmp = Join-Path $root "sources.txt"
[System.IO.File]::WriteAllLines($tmp, $sources, [System.Text.Encoding]::UTF8)
& $javac -encoding UTF-8 -cp "$sqJar;$mailJar;$actJar" -d $out "@$tmp"
if ($LASTEXITCODE -ne 0) { Remove-Item $tmp -ErrorAction SilentlyContinue; Write-Host "Echec de la compilation." -ForegroundColor Red; exit 1 }
Remove-Item $tmp

# Copier les ressources (email.properties) dans les classes compilees
if (Test-Path $res) { Copy-Item "$res\*" $out -Recurse -Force }

# ---- 3/4  Assemblage du jar complet ----
Write-Host "== 3/4  Assemblage du jar ==" -ForegroundColor Cyan
Push-Location $out
& $jarTool xf $sqJar
& $jarTool xf $mailJar
& $jarTool xf $actJar
Pop-Location
Remove-Item -Force "$out\META-INF\MANIFEST.MF" -ErrorAction SilentlyContinue
Get-ChildItem "$out\META-INF" -Filter *.SF  -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem "$out\META-INF" -Filter *.RSA -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem "$out\META-INF" -Filter *.DSA -ErrorAction SilentlyContinue | Remove-Item -Force

$manifest = Join-Path $root "MANIFEST.MF"
"Manifest-Version: 1.0`r`nMain-Class: tetris.Main`r`n" | Out-File -FilePath $manifest -Encoding ascii
$jar = Join-Path $root "tetris.jar"
& $jarTool cfm $jar $manifest -C $out .
Remove-Item $manifest

# ---- 4/4  Generation de l'executable ----
Write-Host "== 4/4  Generation de l'executable (jpackage) ==" -ForegroundColor Cyan
$inputDir = Join-Path $root "jpackage-input"
if (Test-Path $inputDir) { Remove-Item $inputDir -Recurse -Force }
New-Item -ItemType Directory -Path $inputDir | Out-Null
Copy-Item $jar (Join-Path $inputDir "tetris.jar")

# Sortie dans un dossier COURT (profil utilisateur) pour eviter le
# "chemin d'acces trop long" de Windows avec le dossier runtime.
$destBase = Join-Path $env:USERPROFILE "TetrisApp"
if (Test-Path (Join-Path $destBase "Tetris")) { Remove-Item (Join-Path $destBase "Tetris") -Recurse -Force }
if (-not (Test-Path $destBase)) { New-Item -ItemType Directory -Path $destBase | Out-Null }

$jpArgs = @(
    "--type","app-image","--name","Tetris",
    "--input",$inputDir,"--main-jar","tetris.jar","--main-class","tetris.Main",
    "--dest",$destBase
)
$icon = Join-Path $root "build-resources\Tetris.ico"
if (Test-Path $icon) { $jpArgs += @("--icon",$icon) }
& $jpackage @jpArgs
if ($LASTEXITCODE -ne 0) { Write-Host "Echec de jpackage." -ForegroundColor Red; exit 1 }

Write-Host ""
Write-Host "Termine ! L'executable est ici :" -ForegroundColor Green
Write-Host ("   " + (Join-Path $destBase "Tetris\Tetris.exe")) -ForegroundColor Yellow
