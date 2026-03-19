param(
  [Parameter(Mandatory = $true)]
  [string]$PaperPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function S([string]$b64) {
  [Text.Encoding]::Unicode.GetString([Convert]::FromBase64String($b64))
}

function Replace-All($doc, [string]$findText, [string]$replaceText) {
  $range = $doc.Content
  $find = $range.Find
  $find.ClearFormatting() | Out-Null
  $find.Replacement.ClearFormatting() | Out-Null
  # Use positional args to avoid COM parameter-binding quirks.
  [void]$find.Execute(
    $findText,  # FindText
    $false,     # MatchCase
    $false,     # MatchWholeWord
    $false,     # MatchWildcards
    $false,     # MatchSoundsLike
    $false,     # MatchAllWordForms
    $true,      # Forward
    1,          # Wrap (wdFindContinue)
    $false,     # Format
    $replaceText, # ReplaceWith
    2,          # Replace (wdReplaceAll)
    $false, $false, $false, $false
  )
}

# Base64(UTF-16LE)
$F1 = "LHuMTuB6IAAgAPt83374dnNRgGIvZyhX"
$R1 = "LHuMTuB6IAAgAPt83374dnNRgGIvZ14AcAAoVw=="
$F2 = "MgAuADEAIAAgAPt83362Z4RnDk4AX9FTIWoPXyhX"
$R2 = "MgAuADEAIAAgAPt83362Z4RnDk4AX9FTIWoPX14AcAAoVw=="
$F3 = "MgAuADIAIAAgAA5U73pzUS6VgGIvZzIALgAzAA=="
$R3 = "MgAuADIAIAAgAA5U73pzUS6VgGIvZ14AcAAyAC4AMwA="
$F4 = "LHsJTuB6IAAgAPt8334Al0JsBlKQZ9N+CFQ="
$R4 = "LHsJTuB6IAAgAPt8334Al0JsBlKQZ14AcADTfghU"
$F5 = "MwAuADEAIAAgABpOoVJBbQt6BlKQZyx7bVHgeiAAIAD7fN9+S23Viw=="
$R5 = "MwAuADEAIAAgABpOoVJBbQt6BlKQZw=="
$F6 = "MwAuADEAIAAgABpOoVJBbQt6BlKQZ/5W"
$R6 = "MwAuADEAIAAgABpOoVJBbQt6BlKQZ14AcAD+Vg=="

$paperFull = (Resolve-Path -LiteralPath $PaperPath).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $word.Documents.Open($paperFull, $false, $false)

try {
  Replace-All $doc (S $F1) (S $R1)
  Replace-All $doc (S $F2) (S $R2)
  Replace-All $doc (S $F3) (S $R3)
  Replace-All $doc (S $F4) (S $R4)
  Replace-All $doc (S $F5) (S $R5)
  Replace-All $doc (S $F6) (S $R6)
  $doc.Save()
} finally {
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}
