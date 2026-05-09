$ErrorActionPreference = "Stop"

$patterns = @(
    "evade police",
    "avoid enforcement",
    "avoid penalties",
    "bypass law enforcement",
    "bypass checkpoints"
)

$paths = @(
    "android/src/main",
    "backend/src/main/java",
    "README.md"
)

$matches = @()
foreach ($path in $paths) {
    if (Test-Path $path) {
        $item = Get-Item $path
        if ($item.PSIsContainer) {
            $files = Get-ChildItem -Path $path -Recurse -File
            if ($files.Count -gt 0) {
                $matches += $files | Select-String -Pattern $patterns -SimpleMatch -ErrorAction SilentlyContinue
            }
        } else {
            $matches += Select-String -Path $path -Pattern $patterns -SimpleMatch -ErrorAction SilentlyContinue
        }
    }
}

if ($matches.Count -gt 0) {
    $matches | ForEach-Object {
        Write-Error ("Prohibited user-facing safety phrase found in {0}:{1}: {2}" -f $_.Path, $_.LineNumber, $_.Line.Trim())
    }
    exit 1
}

Write-Output "Safety text scan passed."
