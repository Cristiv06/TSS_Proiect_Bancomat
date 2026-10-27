# Ruleaza testele, acoperirea (JaCoCo) si mutation testing (PIT) pentru fiecare set de teste separat.
# Rapoartele se salveaza in rapoarte\acoperire\<set> si rapoarte\pit\<set>.
#
# Utilizare:  powershell -ExecutionPolicy Bypass -File .\ruleaza.ps1 [-Doar acoperire|pit]

param(
    [ValidateSet("toate", "acoperire", "pit")]
    [string]$Doar = "toate"
)

$ErrorActionPreference = "Stop"
$radacina = $PSScriptRoot

# JDK si Maven: se folosesc cele din sistem daca exista, altfel cele incluse in IntelliJ IDEA
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $idea = Get-ChildItem "C:\Program Files\JetBrains" -Directory -Filter "IntelliJ IDEA*" -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending | Select-Object -First 1
    if (-not $idea) { throw "Nu am gasit nici JAVA_HOME, nici IntelliJ IDEA." }
    $env:JAVA_HOME = Join-Path $idea.FullName "jbr"
}
$mvn = (Get-Command mvn -ErrorAction SilentlyContinue).Source
if (-not $mvn) {
    $mvn = Join-Path $idea.FullName "plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
}
Write-Host "JAVA_HOME = $env:JAVA_HOME"
Write-Host "Maven     = $mvn"

$seturi = [ordered]@{
    "1a_echivalenta" = "EchivalentaTest"
    "1b_frontiera"   = "FrontieraTest"
    "1c_cauza_efect" = "CauzaEfectTest"
    "4_mcdc"         = "MCDCTest"
}

function Copiaza($sursa, $destinatie) {
    if (Test-Path $destinatie) { Remove-Item $destinatie -Recurse -Force }
    New-Item -ItemType Directory -Force (Split-Path $destinatie) | Out-Null
    Copy-Item $sursa $destinatie -Recurse
}

Push-Location $radacina
try {
    if ($Doar -in "toate", "acoperire") {
        foreach ($set in $seturi.GetEnumerator()) {
            Write-Host "`n=== Acoperire: $($set.Key) ($($set.Value)) ===" -ForegroundColor Cyan
            & $mvn -q -B clean test "-Dtest=$($set.Value)" "-Dsurefire.failIfNoSpecifiedTests=false"
            if ($LASTEXITCODE -ne 0) { throw "Testele $($set.Value) au esuat." }
            Copiaza "target\site\jacoco" "rapoarte\acoperire\$($set.Key)"
        }
    }

    if ($Doar -in "toate", "pit") {
        $pitSeturi = [ordered]@{
            "1a_echivalenta" = "ro.tss.bancomat.EchivalentaTest"
            "1b_frontiera"   = "ro.tss.bancomat.FrontieraTest"
            "1c_cauza_efect" = "ro.tss.bancomat.CauzaEfectTest"
            "1_functionale" ="ro.tss.bancomat.EchivalentaTest,ro.tss.bancomat.FrontieraTest,ro.tss.bancomat.CauzaEfectTest"
            "4_mcdc"        = "ro.tss.bancomat.MCDCTest"
        }
        foreach ($mutatori in "DEFAULTS", "STRONGER", "ALL") {
            foreach ($set in $pitSeturi.GetEnumerator()) {
                Write-Host "`n=== PIT ($mutatori): $($set.Key) ===" -ForegroundColor Cyan
                $dir = "rapoarte\pit\$($mutatori.ToLower())\$($set.Key)"
                & $mvn -q -B clean test-compile org.pitest:pitest-maven:mutationCoverage `
                    "-DtargetTests=$($set.Value)" "-Dpit.mutators=$mutatori" "-Dpit.reportsDirectory=$radacina\$dir"
                if ($LASTEXITCODE -ne 0) { throw "PIT a esuat pentru $($set.Key)." }
            }
        }
    }
}
finally {
    Pop-Location
}
Write-Host "`nGata. Rapoartele sunt in $radacina\rapoarte" -ForegroundColor Green
