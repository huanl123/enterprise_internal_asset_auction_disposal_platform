param(
  [Parameter(Mandatory = $true)]
  [string]$PaperPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function S([string]$b64) { [Text.Encoding]::Unicode.GetString([Convert]::FromBase64String($b64)) }
function Clean([string]$t) { if ($null -eq $t) { "" } else { ($t -replace "[\r\a]", "").Trim() } }

function IsToc($p) {
  try {
    $raw = $p.Range.Text
    if ($raw -match "\t\s*\d+\s*[\r\a]?$") { return $true }
    $name = $p.Range.Style.NameLocal
    if ($name -match "^(?i)toc(\s|$)") { return $true }
    $false
  } catch { $false }
}

function SetHeading($p, [int]$level) {
  if ($level -eq 1) { $p.Range.Style = -2; $p.OutlineLevel = 1; return }
  if ($level -eq 2) { $p.Range.Style = -3; $p.OutlineLevel = 2; return }
  if ($level -eq 3) { $p.Range.Style = -4; $p.OutlineLevel = 3; return }
}

function SetNormal($p) { $p.Range.Style = -1; $p.OutlineLevel = 10 }

function InsertBeforeIndex($doc, [int]$index, [string]$text, [int]$level) {
  $refP = $doc.Paragraphs.Item($index)
  $r = $refP.Range.Duplicate
  $r.Collapse(1) | Out-Null
  $p = $doc.Paragraphs.Add($r)
  $p.Range.Text = $text
  if ($level -gt 0) { SetHeading $p $level } else { SetNormal $p }
  $p
}

function InsertAfterPara($doc, $para, [string]$text, [int]$level) {
  $r = $para.Range.Duplicate
  $r.Collapse(0) | Out-Null
  $p = $doc.Paragraphs.Add($r)
  $p.Range.Text = $text
  if ($level -gt 0) { SetHeading $p $level } else { SetNormal $p }
  $p
}

function FindLastStartsWith($doc, [string]$prefix) {
  for ($i = $doc.Paragraphs.Count; $i -ge 1; $i--) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t.StartsWith($prefix)) { return $i }
  }
  return 0
}

function FindFirstStartsWithFrom($doc, [string]$prefix, [int]$start) {
  for ($i = $start; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t.StartsWith($prefix)) { return $i }
  }
  return 0
}

function FindFirstRegexFrom($doc, [string]$pattern, [int]$start) {
  $re = [regex]$pattern
  for ($i = $start; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if (-not $t) { continue }
    if ($re.IsMatch($t)) { return $i }
  }
  return 0
}

# Headings (Base64 UTF-16LE)
$CH1 = "LHsATuB6IAAgAOp+uos="
$CH2 = "LHuMTuB6IAAgAPt83374dnNRgGIvZw=="
$CH3 = "LHsJTuB6IAAgAPt8334Al0JsBlKQZw=="
$CH4 = "LHvbVuB6IAAgAPt8336+i6GL"
$CH5 = "LHuUTuB6IAAgAPt8336eW7Bz"
$CH6 = "LHttUeB6IAAgAPt8335LbdWL"

$H13 = "MQAuADMAIAAgAMeRKHWEdhR4dnq5ZdVs"
$H14 = "MQAuADQAIAAgACxnh2XgeoKCiVuSYw=="

$H21 = "MgAuADEAIAAgAPt83362Z4RnDk4AX9FTIWoPXw=="
$H22 = "MgAuADIAIAAgAA5U73pzUS6VgGIvZw=="
$H23 = "MgAuADMAIAAgAE1S73pzUS6VgGIvZw=="
$H24 = "MgAuADQAIAAgAHBlbmOTXg5OiVtoUTpnNlI="
$H25 = "MgAuADUAIAAgAABf0VMOTtCPTIivc4NY"
$H26 = "MgAuADYAIAAgAIdlLnMOTgdoxlGdT25j"

$H31 = "MwAuADEAIAAgAPt8336CafCP"
$H32 = "MwAuADIAIAAgAO9TTIgnYAZSkGc="
$H33 = "MwAuADMAIAAgAJ9S/YAAl0JsBlKQZw=="
$H34 = "MwAuADQAIAAgAF6Xn1L9gACXQmwGUpBn"
$H35 = "MwAuADUAIAAgACxn4HoPXNN+"

$H41 = "NAAuADEAIAAgAPt83347YFNPvouhiw=="
$H411 = "NAAuADEALgAxACAAIAD7fN9+b4/2TrZnhGc="
$H412 = "NAAuADEALgAyACAAIAA7YFNPIWpXVxJSBlI="
$H42 = "NAAuADIAIAAgAJ9S/YAhaldXvouhiw=="
$H43 = "NAAuADMAIAAgAHBlbmOTXr6LoYs="
$H44 = "NAAuADQAIAAgACxn4HoPXNN+"

$H51 = "NQAuADEAIAAgAPt8334AX9FTr3ODWA=="
$H52 = "NQAuADIAIAAgAPt8336fUv2Anluwcw=="
$H521 = "NQAuADIALgAxACAAIAAodTdipIvBiw5OQ2dQliFqV1efUv2Anluwcw=="
$H522 = "NQAuADIALgAyACAAIABEjadOoXsGdCFqV1efUv2Anluwcw=="
$H523 = "NQAuADIALgAzACAAIADNYlZTDk6kThNmIWpXV59S/YCeW7Bz"
$H524 = "NQAuADIALgA0ACAAIADYTj5roVs4aA5OBFluf1JfY2ghaldXn1L9gJ5bsHM="
$H525 = "NQAuADIALgA1ACAAIADffqGLBlKQZw5Omlv2ZftOoVKfUv2Anluwcw=="
$H526 = "NQAuADIALgA2ACAAIABzUS6Vnluwc/SLDmY="
$H53 = "NQAuADMAIAAgACxn4HoPXNN+"

$H61 = "NgAuADEAIAAgAEtt1Yuvc4NY"
$H62 = "NgAuADIAIAAgAEtt1YuFUblbjFTTfpxn"
$H63 = "NgAuADMAIAAgACxn4HoPXNN+"

$TODO = "CP+FX2WICf8=" # placeholder "TODO"

$ACK = "9IEgACAAIow="  # acknowledgements title
$REF = "wlMgAAOAIACHZSAALnM=" # references title

$paperFull = (Resolve-Path -LiteralPath $PaperPath).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $word.Documents.Open($paperFull, $false, $false)

try {
  $iBodyCh1 = FindLastStartsWith $doc (S $CH1)
  if ($iBodyCh1 -eq 0) { throw "Cannot locate body Chapter 1 heading." }

  # Ensure 1.3 and 1.4 headings are clean (do not try to preserve merged tail here).
  $i13 = FindFirstRegexFrom $doc '^1\.3\b' ($iBodyCh1 + 1)
  if ($i13 -ne 0) { $p = $doc.Paragraphs.Item($i13); $p.Range.Text = (S $H13); SetHeading $p 2 }
  $i14 = FindFirstRegexFrom $doc '^1\.4\b' ($iBodyCh1 + 1)
  if ($i14 -ne 0) { $p = $doc.Paragraphs.Item($i14); $p.Range.Text = (S $H14); SetHeading $p 2 }

  $iCh3 = FindFirstStartsWithFrom $doc (S $CH3) ($iBodyCh1 + 1)
  if ($iCh3 -eq 0) { $iCh3 = $doc.Paragraphs.Count + 1 }

  # Insert Chapter 2 skeleton before the first Chapter 3 heading if Chapter 2 is missing in between.
  $iCh2 = FindFirstStartsWithFrom $doc (S $CH2) ($iBodyCh1 + 1)
  if ($iCh2 -eq 0 -or $iCh2 -gt $iCh3) {
    $pCh2 = InsertBeforeIndex $doc $iCh3 (S $CH2) 1
    $p = InsertAfterPara $doc $pCh2 (S $H21) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H22) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H23) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H24) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H25) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H26) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $iCh3 = FindFirstStartsWithFrom $doc (S $CH3) ($p.Range.End)
  }

  # Back matter insertion point: first ACK/REF after body, else end.
  $iBack = FindFirstStartsWithFrom $doc (S $ACK) ($iBodyCh1 + 1)
  if ($iBack -eq 0) { $iBack = FindFirstStartsWithFrom $doc (S $REF) ($iBodyCh1 + 1) }
  if ($iBack -eq 0) { $iBack = $doc.Paragraphs.Count + 1 }

  # Insert Chapters 4-6 skeleton before back matter if missing.
  $iCh4 = FindFirstStartsWithFrom $doc (S $CH4) ($iBodyCh1 + 1)
  if ($iCh4 -eq 0 -or $iCh4 -gt $iBack) {
    $pCh4 = InsertBeforeIndex $doc $iBack (S $CH4) 1
    $p = InsertAfterPara $doc $pCh4 (S $H41) 2
    [void](InsertAfterPara $doc $p (S $H411) 3)
    [void](InsertAfterPara $doc $p (S $H412) 3)
    $p = InsertAfterPara $doc $p (S $H42) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H43) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H44) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
  }

  $iCh5 = FindFirstStartsWithFrom $doc (S $CH5) ($iBodyCh1 + 1)
  if ($iCh5 -eq 0 -or $iCh5 -gt $iBack) {
    $pCh5 = InsertBeforeIndex $doc $iBack (S $CH5) 1
    $p = InsertAfterPara $doc $pCh5 (S $H51) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H52) 2
    $p = InsertAfterPara $doc $p (S $H521) 3
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H522) 3
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H523) 3
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H524) 3
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H525) 3
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H526) 3
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H53) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
  }

  $iCh6 = FindFirstStartsWithFrom $doc (S $CH6) ($iBodyCh1 + 1)
  if ($iCh6 -eq 0 -or $iCh6 -gt $iBack) {
    $pCh6 = InsertBeforeIndex $doc $iBack (S $CH6) 1
    $p = InsertAfterPara $doc $pCh6 (S $H61) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H62) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
    $p = InsertAfterPara $doc $p (S $H63) 2
    [void](InsertAfterPara $doc $p (S $TODO) 0)
  }

  $doc.Save()
} finally {
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}

