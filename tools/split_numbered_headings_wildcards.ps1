param(
  [Parameter(Mandatory = $true)]
  [string]$PaperPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Clean([string]$t) {
  if ($null -eq $t) { return "" }
  ($t -replace "[\r\a]", "").Trim()
}

function IsToc($p) {
  try {
    $raw = $p.Range.Text
    if ($raw -match "\t\s*\d+\s*[\r\a]?$") { return $true }
    $name = $p.Range.Style.NameLocal
    if ($name -match "^(?i)toc(\s|$)") { return $true }
    $false
  } catch { $false }
}

function FindFirstChapterStart($doc) {
  $chDi = [char]0x7B2C
  $chZhang = [char]0x7AE0
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if (-not $t) { continue }
    $head = $t
    if ($head.Length -gt 12) { $head = $head.Substring(0, 12) }
    if ($t.StartsWith([string]$chDi) -and $head.Contains([string]$chZhang)) { return $p.Range.Start }
  }
  return 0
}

function ReplaceWildAll($range, [string]$findPattern, [string]$replaceText) {
  $find = $range.Find
  $find.ClearFormatting() | Out-Null
  $find.Replacement.ClearFormatting() | Out-Null
  [void]$find.Execute(
    $findPattern,
    $false,  # MatchCase
    $false,  # MatchWholeWord
    $true,   # MatchWildcards
    $false, $false,
    $true, 1, $false,
    $replaceText,
    2,
    $false, $false, $false, $false
  )
}

$paperFull = (Resolve-Path -LiteralPath $PaperPath).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $word.Documents.Open($paperFull, $false, $false)

try {
  $start = FindFirstChapterStart $doc
  if ($start -eq 0) { throw "Cannot locate first chapter start." }
  $r = $doc.Range($start, $doc.Content.End)

  # Insert paragraph breaks before numbered headings like 1.2 / 5.2.1 when they are glued to prior text.
  # Keep the whitespace after the heading marker.
  $pat = '([!^13])([1-6]\.[0-9]{1,2}(\.[0-9]{1,2}){0,1})([ ]{1,3})'
  $rep = '\1^p\2\4'
  ReplaceWildAll $r $pat $rep

  # Also split when a chapter marker "第X章" is glued to previous text.
  # We only handle common 1..6.
  $pat2 = '([!^13])(第[一二三四五六]章[ ]{0,3})'
  $rep2 = '\1^p\2'
  ReplaceWildAll $r $pat2 $rep2

  $doc.Save()
} finally {
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}
