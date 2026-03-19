param(
  [Parameter(Mandatory = $true)]
  [string]$PaperPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function S([string]$b64) {
  [Text.Encoding]::Unicode.GetString([Convert]::FromBase64String($b64))
}

# Base64(UTF-16LE) literals (ASCII-only)
$T_CH1 = "LHsATuB6IAAgAOp+uos="
$T_CH2 = "LHuMTuB6IAAgAPt83374dnNRgGIvZw=="
$T_CH3 = "LHsJTuB6IAAgAPt8334Al0JsBlKQZw=="
$T_CH4 = "LHvbVuB6IAAgAPt8336+i6GL"
$T_CH5 = "LHuUTuB6IAAgAPt8336eW7Bz"
$T_CH6 = "LHttUeB6IAAgAPt8335LbdWL"

$T_13  = "MQAuADMAIAAgAMeRKHWEdhR4dnq5ZdVs"
$T_31  = "MwAuADEAIAAgAAFPGk6fXudlRI2nToVR6JDNYlZTDk4EWW5/c17wU/t8336CafCP"
$T_32  = "MwAuADIAIAAgAO9TTIgnYAZSkGc="
$T_33  = "MwAuADMAIAAgAJ9S/YAAl0JsBlKQZw=="
$T_34  = "MwAuADQAIAAgAF6Xn1L9gACXQmwGUpBn"
$T_35  = "MwAuADUAIAAgACxn4HoPXNN+"

$T_41  = "NAAuADEAIAAgAPt83347YFNPvouhiw=="
$T_411 = "NAAuADEALgAxACAAIAD7fN9+b4/2TrZnhGc="
$T_412 = "NAAuADEALgAyACAAIAA7YFNPIWpXVxJSBlI="
$T_42  = "NAAuADIAIAAgAJ9S/YAhaldXvouhiw=="
$T_421 = "NAAuADIALgAxACAAIAAodTdipIvBiw5OQ2dQliFqV1e+i6GL"
$T_422 = "NAAuADIALgAyACAAIABEjadOoXsGdA5OoVs4aCFqV1e+i6GL"
$T_423 = "NAAuADIALgAzACAAIADNYlZTDk6kThNmIWpXV76LoYs="
$T_424 = "NAAuADIALgA0ACAAIADYTj5roVs4aA5OBFluf1JfY2ghaldXvouhiw=="
$T_425 = "NAAuADIALgA1ACAAIADffqGLBlKQZw5Omlv2ZftOoVIhaldXvouhiw=="
$T_43  = "NAAuADMAIAAgAHBlbmOTXr6LoYs="
$T_44  = "NAAuADQAIAAgACxn4HoPXNN+"

$T_51  = "NQAuADEAIAAgAPt8334AX9FTr3ODWA=="
$T_52  = "NQAuADIAIAAgAPt8336fUv2Anluwcw=="
$T_521 = "NQAuADIALgAxACAAIAAodTdipIvBiw5OQ2dQliFqV1efUv2Anluwcw=="
$T_522 = "NQAuADIALgAyACAAIABEjadOoXsGdA5OoVs4aCFqV1efUv2Anluwcw=="
$T_523 = "NQAuADIALgAzACAAIADNYlZTDk6kThNmIWpXV59S/YCeW7Bz"
$T_524 = "NQAuADIALgA0ACAAIADYTj5roVs4aA5OBFluf1JfY2ghaldXn1L9gJ5bsHM="
$T_525 = "NQAuADIALgA1ACAAIADffqGLBlKQZw5Omlv2ZftOoVKfUv2Anluwcw=="
$T_526 = "NQAuADIALgA2ACAAIABzUS6Vnluwc/SLDmY="
$T_53  = "NQAuADMAIAAgACxn4HoPXNN+"

$T_61  = "NgAuADEAIAAgAEtt1Yuvc4NY"
$T_62  = "NgAuADIAIAAgAEtt1YuFUblbjFTTfpxn"
$T_621 = "NgAuADIALgAxACAAIACfUv2AS23Viw=="
$T_622 = "NgAuADIALgAyACAAIAAnYP2ADk6JW2hRS23Viw=="

$T_63  = "NgAuADMAIAAgANN+uos="

function Clean([string]$t) {
  if ($null -eq $t) { return "" }
  ($t -replace "[\r\a]", "").Trim()
}

function IsToc($p) {
  try {
    $raw = $p.Range.Text
    if ($raw -match "\t\s*\d+\s*[\r\a]?$") { return $true }
    $name = $p.Range.Style.NameLocal
    if ($name -match "^(?i)toc\s") { return $true }
    $false
  } catch { $false }
}

function FindFirstSection($doc, [string]$prefix) {
  $re = [regex]("^" + [regex]::Escape($prefix) + "\b")
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($re.IsMatch($t)) { return $i }
  }
  throw "Section $prefix not found."
}

function FindFirstStartsWith($doc, [string]$prefix) {
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t.StartsWith($prefix)) { return $i }
  }
  0
}

function SetHeading($p, [int]$level) {
  if ($level -eq 1) { $p.Range.Style = -2; $p.OutlineLevel = 1; return }
  if ($level -eq 2) { $p.Range.Style = -3; $p.OutlineLevel = 2; return }
  if ($level -eq 3) { $p.Range.Style = -4; $p.OutlineLevel = 3; return }
}

function InsertPara($doc, [int]$pos, [string]$text, [int]$level) {
  $r = $doc.Range($pos, $pos)
  $p = $doc.Paragraphs.Add($r)
  $p.Range.Text = $text
  SetHeading $p $level
  $p
}

function RangeParaToParaStart($doc, [int]$startIdx, [int]$endIdx) {
  $start = $doc.Paragraphs.Item($startIdx).Range.Start
  $end = $doc.Paragraphs.Item($endIdx).Range.Start
  $doc.Range($start, $end)
}

function Stash($stashDoc, $srcRange) {
  $endPos = $stashDoc.Content.End
  $dest = $stashDoc.Range($endPos, $endPos)
  $start = $dest.Start
  $dest.FormattedText = $srcRange.FormattedText
  @($start, $dest.End)
}

function PasteStash($doc, [int]$pos, $stashDoc, [int]$s, [int]$e) {
  $r = $doc.Range($pos, $pos)
  $src = $stashDoc.Range($s, $e)
  $r.FormattedText = $src.FormattedText
  $r
}

function SetHeadingStyles($doc) {
  foreach ($sid in @(-2, -3, -4)) {
    try {
      $st = $doc.Styles.Item($sid)
      $st.Font.NameFarEast = "SimHei"
      $st.Font.Bold = $true
    } catch {}
  }
}

$paperFull = (Resolve-Path -LiteralPath $PaperPath).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $word.Documents.Open($paperFull, $false, $false)
$stashDoc = $null

try {
  SetHeadingStyles $doc
  $stashDoc = $word.Documents.Add()

  $prefixCh6 = (S $T_CH6).Substring(0, 3)
  $prefixCh3 = (S $T_CH3).Substring(0, 3)

  $idx11 = FindFirstSection $doc "1.1"
  $idx21 = FindFirstSection $doc "2.1"
  $idx31 = FindFirstSection $doc "3.1"
  $idx34 = FindFirstSection $doc "3.4"
  $idx35 = FindFirstSection $doc "3.5"
  $idx41 = FindFirstSection $doc "4.1"
  $idx51 = FindFirstSection $doc "5.1"

  $before11 = $idx11
  if ($idx11 -gt 1) {
    $pPrev = $doc.Paragraphs.Item($idx11 - 1)
    if (-not (IsToc $pPrev)) {
      $tPrev = Clean $pPrev.Range.Text
      if ($tPrev -and ($tPrev -notmatch "^\d+\.\d+\b")) {
        $before11 = $idx11 - 1
        $ch2 = S $T_CH2
        if ($tPrev.StartsWith($ch2)) {
          $pPrev.Range.Text = $tPrev.Substring($ch2.Length).Trim()
        }
      }
    }
  }
  [void](InsertPara $doc $doc.Paragraphs.Item($before11).Range.Start (S $T_CH1) 1)

  $idx21 = FindFirstSection $doc "2.1"
  [void](InsertPara $doc $doc.Paragraphs.Item($idx21).Range.Start (S $T_CH2) 1)

  $idx31 = FindFirstSection $doc "3.1"
  for ($i = $idx31 - 1; $i -ge 1 -and $i -ge ($idx31 - 3); $i--) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t.StartsWith($prefixCh3)) { $p.Range.Delete(); break }
  }
  $idx31 = FindFirstSection $doc "3.1"
  [void](InsertPara $doc $doc.Paragraphs.Item($idx31).Range.Start (S $T_CH3) 1)

  $idx34 = FindFirstSection $doc "3.4"
  $idx35 = FindFirstSection $doc "3.5"
  $idx41 = FindFirstSection $doc "4.1"

  $archBody = $doc.Range($doc.Paragraphs.Item($idx34).Range.End, $doc.Paragraphs.Item($idx35).Range.Start)
  $dbBody = $doc.Range($doc.Paragraphs.Item($idx35).Range.End, $doc.Paragraphs.Item($idx41).Range.Start)
  $archPos = Stash $stashDoc $archBody
  $stashDoc.Range($stashDoc.Content.End, $stashDoc.Content.End).Text = "`r"
  $dbPos = Stash $stashDoc $dbBody
  (RangeParaToParaStart $doc $idx34 $idx41).Delete()

  $idx31 = FindFirstSection $doc "3.1"
  $idx32old = FindFirstSection $doc "3.2"
  $idx33old = FindFirstSection $doc "3.3"
  $p31 = $doc.Paragraphs.Item($idx31); $p31.Range.Text = (S $T_31); SetHeading $p31 2
  $p32old = $doc.Paragraphs.Item($idx32old)
  $p33old = $doc.Paragraphs.Item($idx33old)
  [void](InsertPara $doc $p32old.Range.Start (S $T_32) 2)
  $p32old.Range.Text = (S $T_33); SetHeading $p32old 2
  $p33old.Range.Text = (S $T_34); SetHeading $p33old 2

  $idx41 = FindFirstSection $doc "4.1"
  [void](InsertPara $doc $doc.Paragraphs.Item($idx41).Range.Start (S $T_35) 2)

  $idx41 = FindFirstSection $doc "4.1"
  $idx51 = FindFirstSection $doc "5.1"
  $implRange = RangeParaToParaStart $doc $idx41 $idx51
  $implPos = Stash $stashDoc $implRange
  $implRange.Delete()

  $idx51 = FindFirstSection $doc "5.1"
  $pos = $doc.Paragraphs.Item($idx51).Range.Start
  $pCh4 = InsertPara $doc $pos (S $T_CH4) 1
  $p41 = InsertPara $doc $pCh4.Range.End (S $T_41) 2
  $p411 = InsertPara $doc $p41.Range.End (S $T_411) 3
  $archInserted = PasteStash $doc $p411.Range.End $stashDoc $archPos[0] $archPos[1]
  $p412 = InsertPara $doc $archInserted.End (S $T_412) 3
  $p42 = InsertPara $doc $p412.Range.End (S $T_42) 2
  [void](InsertPara $doc $p42.Range.End (S $T_421) 3)
  [void](InsertPara $doc $p42.Range.End (S $T_422) 3)
  [void](InsertPara $doc $p42.Range.End (S $T_423) 3)
  [void](InsertPara $doc $p42.Range.End (S $T_424) 3)
  $p425 = InsertPara $doc $p42.Range.End (S $T_425) 3
  $p43 = InsertPara $doc $p425.Range.End (S $T_43) 2
  $dbInserted = PasteStash $doc $p43.Range.End $stashDoc $dbPos[0] $dbPos[1]
  [void](InsertPara $doc $dbInserted.End (S $T_44) 2)

  $idx51 = FindFirstSection $doc "5.1"
  $pCh5 = InsertPara $doc $doc.Paragraphs.Item($idx51).Range.Start (S $T_CH5) 1
  $p51 = InsertPara $doc $pCh5.Range.End (S $T_51) 2
  $p52 = InsertPara $doc $p51.Range.End (S $T_52) 2
  $implInserted = PasteStash $doc $p52.Range.End $stashDoc $implPos[0] $implPos[1]
  $implInsertedRange = $doc.Range($implInserted.Start, $implInserted.End)

  $map = @{
    "4.1" = (S $T_521)
    "4.2" = (S $T_522)
    "4.3" = (S $T_523)
    "4.4" = (S $T_524)
    "4.5" = (S $T_525)
    "4.6" = (S $T_526)
  }
  for ($i = 1; $i -le $implInsertedRange.Paragraphs.Count; $i++) {
    $pp = $implInsertedRange.Paragraphs.Item($i)
    if (IsToc $pp) { continue }
    $tt = Clean $pp.Range.Text
    foreach ($k in $map.Keys) {
      if ($tt -match ("^" + [regex]::Escape($k) + "\b")) {
        $pp.Range.Text = $map[$k]
        SetHeading $pp 3
        break
      }
    }
  }
  [void](InsertPara $doc $implInsertedRange.End (S $T_53) 2)

  $idx51 = FindFirstSection $doc "5.1"
  [void](InsertPara $doc $doc.Paragraphs.Item($idx51).Range.Start (S $T_CH6) 1)

  $idx51 = FindFirstSection $doc "5.1"
  $idxConc = FindFirstStartsWith $doc $prefixCh6
  if ($idxConc -eq 0) { $idxConc = $doc.Paragraphs.Count + 1 }
  for ($i = $idx51; $i -lt $idxConc; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t -match "^5\.1\b") { $p.Range.Text = (S $T_61); SetHeading $p 2; continue }
    if ($t -match "^5\.2\b") { $p.Range.Text = (S $T_62); SetHeading $p 2; continue }
    if ($t -match "^5\.3\b") { $p.Range.Text = (S $T_621); SetHeading $p 3; continue }
    if ($t -match "^5\.4\b") { $p.Range.Text = (S $T_622); SetHeading $p 3; continue }
    if ($t -match "^5\.(1[1-9]|2[0-9])\b") {
      $p.Range.Text = ($t -replace "^5\.", "6.")
      $p.Range.Style = -1
      $p.OutlineLevel = 10
    }
  }

  $idxConc = FindFirstStartsWith $doc $prefixCh6
  if ($idxConc -ne 0) {
    $p = $doc.Paragraphs.Item($idxConc)
    $p.Range.Text = (S $T_63)
    SetHeading $p 2
  }

  $idx13 = FindFirstSection $doc "1.3"
  $p = $doc.Paragraphs.Item($idx13)
  $p.Range.Text = (S $T_13)
  SetHeading $p 2

  $doc.Save()
} finally {
  try { if ($null -ne $stashDoc) { $stashDoc.Close($false) | Out-Null } } catch {}
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}

