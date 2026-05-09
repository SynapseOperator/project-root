$ErrorActionPreference = "Stop"

$paths = @(
    "android/src/main",
    "backend/src/main",
    "scripts",
    "README.md"
)

$selfPath = (Resolve-Path $PSCommandPath).Path
$matches = @()
foreach ($path in $paths) {
    if (Test-Path $path) {
        $item = Get-Item $path
        if ($item.PSIsContainer) {
            $files = Get-ChildItem -Path $path -Recurse -File | Where-Object {
                (Resolve-Path $_.FullName).Path -ne $selfPath
            }
            if ($files.Count -gt 0) {
                $matches += $files | Select-String -Pattern "TODO", "FIXME" -SimpleMatch -CaseSensitive -ErrorAction SilentlyContinue
            }
        } elseif ((Resolve-Path $item.FullName).Path -ne $selfPath) {
            $matches += Select-String -Path $path -Pattern "TODO", "FIXME" -SimpleMatch -CaseSensitive -ErrorAction SilentlyContinue
        }
    }
}

if ($matches.Count -gt 0) {
    $matches | ForEach-Object {
        Write-Error ("Open TODO/FIXME found in {0}:{1}: {2}" -f $_.Path, $_.LineNumber, $_.Line.Trim())
    }
    exit 1
}

Write-Output "TODO scan passed."
