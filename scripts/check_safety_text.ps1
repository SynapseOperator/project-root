$ErrorActionPreference = "Stop"

function U([int[]]$Codes) {
    return -join ($Codes | ForEach-Object { [char]$_ })
}

$patterns = @(
    "evade police",
    "avoid enforcement",
    "avoid penalties",
    "bypass law enforcement",
    "bypass checkpoints",
    (U @(0x89C4, 0x907F, 0x4EA4, 0x8B66)),
    (U @(0x8EB2, 0x907F, 0x6267, 0x6CD5)),
    (U @(0x9003, 0x907F, 0x5904, 0x7F5A)),
    (U @(0x907F, 0x5F00, 0x68C0, 0x67E5)),
    (U @(0x7ED5, 0x8FC7, 0x6267, 0x6CD5)),
    (U @(0x8EB2, 0x907F, 0x4EA4, 0x8B66)),
    (U @(0x89C4, 0x907F, 0x6267, 0x6CD5))
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
        $files = @()
        if ($item.PSIsContainer) {
            $files = Get-ChildItem -Path $path -Recurse -File
        } else {
            $files = @($item)
        }

        foreach ($file in $files) {
            $text = Get-Content -Raw -Encoding UTF8 -LiteralPath $file.FullName
            foreach ($pattern in $patterns) {
                if ($text.Contains($pattern)) {
                    $matches += [pscustomobject]@{
                        Path = $file.FullName
                        Pattern = $pattern
                    }
                }
            }
        }
    }
}

if ($matches.Count -gt 0) {
    $matches | ForEach-Object {
        Write-Error ("Prohibited user-facing safety phrase found in {0}: {1}" -f $_.Path, $_.Pattern)
    }
    exit 1
}

Write-Output "Safety text scan passed."
