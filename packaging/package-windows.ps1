<#
.SYNOPSIS
    Builds the Windows downloads for a release: a portable zip and, optionally, an MSI installer.
    Both bundle a trimmed Java runtime, so players don't need Java installed.

.DESCRIPTION
    Run `mvn package` first. The portable zip needs only the JDK (17+). The MSI also needs
    WiX Toolset 3 (https://wixtoolset.org), which CI installs automatically.

.EXAMPLE
    ./packaging/package-windows.ps1 -Version 1.0.0
    ./packaging/package-windows.ps1 -Version 1.0.0 -Msi
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Version,

    [switch]$Msi
)

$ErrorActionPreference = 'Stop'

# Constant across versions so a newer MSI upgrades an older install in place. Never change it.
$UpgradeUuid = '00e6a213-c30a-4926-80a5-94509ba09ccd'
$MainClass = 'com.kswkev.minesweeper.Main'
# From `jdeps --print-module-deps`; keeps the bundled runtime small.
$Modules = 'java.base,java.desktop,java.prefs'

$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$jarName = "minesweeper-$Version.jar"
$jar = Join-Path $root "target\$jarName"
$work = Join-Path $root 'target\jpackage'
$inputDir = Join-Path $work 'input'

function Find-JPackage {
    if ($env:JAVA_HOME) {
        $candidate = Join-Path $env:JAVA_HOME 'bin\jpackage.exe'
        if (Test-Path $candidate) { return $candidate }
    }
    $onPath = Get-Command jpackage -ErrorAction SilentlyContinue
    if ($onPath) { return $onPath.Source }
    throw 'jpackage not found. Set JAVA_HOME to a JDK 17+ install or put its bin folder on PATH.'
}

function Invoke-JPackage([string[]]$Arguments) {
    & $script:jpackage @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "jpackage failed with exit code $LASTEXITCODE"
    }
}

function Assert-WiX {
    if (Get-Command candle.exe -ErrorAction SilentlyContinue) { return }
    $installed = Get-ChildItem "${env:ProgramFiles(x86)}\WiX Toolset v3*\bin\candle.exe" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($installed) {
        $env:PATH = "$($installed.DirectoryName);$env:PATH"
        return
    }
    throw 'Building the MSI needs WiX Toolset 3 (candle.exe). Install it from https://wixtoolset.org or run without -Msi.'
}

if (-not (Test-Path $jar)) {
    throw "$jar not found. Run 'mvn package' first (and check the version matches pom.xml)."
}
$jpackage = Find-JPackage

if (Test-Path $work) { Remove-Item -Recurse -Force $work }
New-Item -ItemType Directory -Force $inputDir | Out-Null
Copy-Item $jar $inputDir

$common = @(
    '--name', 'Minesweeper',
    '--app-version', $Version,
    '--vendor', 'kswkev',
    '--description', 'Classic Minesweeper',
    '--input', $inputDir,
    '--main-jar', $jarName,
    '--main-class', $MainClass,
    '--add-modules', $Modules,
    '--dest', $work
)

Write-Host "Building portable app image..."
Invoke-JPackage (@('--type', 'app-image') + $common)
$zip = Join-Path $work "Minesweeper-$Version-windows.zip"
Compress-Archive -Path (Join-Path $work 'Minesweeper') -DestinationPath $zip
Write-Host "Created $zip"

if ($Msi) {
    Assert-WiX
    Write-Host "Building MSI installer..."
    Invoke-JPackage (@(
        '--type', 'msi',
        '--win-menu',
        '--win-menu-group', 'Minesweeper',
        '--win-shortcut',
        '--win-dir-chooser',
        '--win-upgrade-uuid', $UpgradeUuid
    ) + $common)
    $msiFile = Join-Path $work "Minesweeper-$Version.msi"
    Write-Host "Created $msiFile"
}
