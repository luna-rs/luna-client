$ErrorActionPreference = 'Stop'
$clientRoot = Split-Path -Parent $PSScriptRoot
$testOutput = Join-Path $clientRoot 'build/music-tests'
New-Item -ItemType Directory -Path $testOutput -Force | Out-Null
$sources = @(Get-ChildItem -LiteralPath (Join-Path $clientRoot 'src'), $PSScriptRoot -Recurse -Filter '*.java' |
    ForEach-Object { $_.FullName })
& javac -d $testOutput $sources
if ($LASTEXITCODE -ne 0) { throw 'Client/test compilation failed.' }
foreach ($testClass in @('MusicControllerTest', 'OnDemandFetcherTest', 'sign.MidiPlayerTest', 'sign.WavePlayerTest')) {
    & java -cp $testOutput $testClass
    if ($LASTEXITCODE -ne 0) { throw "$testClass failed." }
}
