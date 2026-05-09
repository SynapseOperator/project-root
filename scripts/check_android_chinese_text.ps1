$ErrorActionPreference = "Stop"

function U([int[]]$Codes) {
    return -join ($Codes | ForEach-Object { [char]$_ })
}

$paths = @(
    "android/src/main/java/com/yuelutraffic/app",
    "android/src/main/res/values/strings.xml"
)

$legacyEnglishPhrases = @(
    "Campus traffic safety and road condition reporting",
    "Student number",
    "Enter app",
    "Signed in as",
    "Submit report",
    "Accident board",
    "Private contact",
    "Request contact",
    "Confirm match",
    "Admin review",
    "Public ranking uses app display codes",
    "Posting is temporarily restricted"
)

$requiredChinesePhrases = @(
    (U @(0x5730, 0x56FE)),
    (U @(0x4E0A, 0x62A5)),
    (U @(0x4E8B, 0x6545, 0x680F)),
    (U @(0x6211, 0x7684)),
    (U @(0x540E, 0x7AEF, 0x8FDE, 0x63A5, 0x5931, 0x8D25)),
    (U @(0x4F7F, 0x7528, 0x672C, 0x5730, 0x6F14, 0x793A, 0x6A21, 0x5F0F)),
    (U @(0x4EA4, 0x901A, 0x7BA1, 0x7406, 0x63D0, 0x793A)),
    (U @(0x9053, 0x8DEF, 0x62E5, 0x5835)),
    (U @(0x4E8B, 0x6545, 0x4E92, 0x52A9, 0x680F)),
    (U @(0x7BA1, 0x7406, 0x5458, 0x9762, 0x677F)),
    (U @(0x6392, 0x884C, 0x699C)),
    (U @(0x5B66, 0x53F7, 0x4EC5, 0x7528, 0x4E8E, 0x533A, 0x5206, 0x5E94, 0x7528, 0x5185, 0x7528, 0x6237))
)

$mojibakePatterns = @(
    (U @(0x9225)),
    (U @(0x00C3)),
    (U @(0x00C2)),
    (U @(0xFFFD))
)

$files = @()
foreach ($path in $paths) {
    if (Test-Path $path) {
        $item = Get-Item $path
        if ($item.PSIsContainer) {
            $files += Get-ChildItem -Path $path -Recurse -File
        } else {
            $files += $item
        }
    }
}

if ($files.Count -eq 0) {
    Write-Error "No Android source files found for Chinese text scan."
    exit 1
}

$fileTexts = @()
foreach ($file in $files) {
    $fileTexts += [pscustomobject]@{
        Path = $file.FullName
        Text = Get-Content -Raw -Encoding UTF8 -LiteralPath $file.FullName
    }
}

$legacyMatches = @()
$mojibakeMatches = @()
foreach ($fileText in $fileTexts) {
    foreach ($phrase in $legacyEnglishPhrases) {
        if ($fileText.Text.Contains($phrase)) {
            $legacyMatches += [pscustomobject]@{ Path = $fileText.Path; Phrase = $phrase }
        }
    }
    foreach ($phrase in $mojibakePatterns) {
        if ($fileText.Text.Contains($phrase)) {
            $mojibakeMatches += [pscustomobject]@{ Path = $fileText.Path; Phrase = $phrase }
        }
    }
}

if ($legacyMatches.Count -gt 0) {
    $legacyMatches | ForEach-Object {
        Write-Error ("Legacy English Android UI phrase found in {0}: {1}" -f $_.Path, $_.Phrase)
    }
    exit 1
}

if ($mojibakeMatches.Count -gt 0) {
    $mojibakeMatches | ForEach-Object {
        Write-Error ("Possible garbled Android text found in {0}: {1}" -f $_.Path, $_.Phrase)
    }
    exit 1
}

$allText = ($fileTexts | ForEach-Object { $_.Text }) -join "`n"
$missing = @()
foreach ($phrase in $requiredChinesePhrases) {
    if (-not $allText.Contains($phrase)) {
        $missing += $phrase
    }
}

if ($missing.Count -gt 0) {
    Write-Error ("Required Android Chinese phrase(s) missing: {0}" -f ($missing -join ", "))
    exit 1
}

Write-Output "Android Chinese text scan passed."
