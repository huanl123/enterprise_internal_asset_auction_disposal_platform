param(
  [Parameter(Mandatory = $true)]
  [string]$PaperPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function S([string]$b64) {
  [Text.Encoding]::Unicode.GetString([Convert]::FromBase64String($b64))
}

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

function SetHeading($p, [int]$level) {
  if ($level -eq 1) { $p.Range.Style = -2; $p.OutlineLevel = 1; return }
  if ($level -eq 2) { $p.Range.Style = -3; $p.OutlineLevel = 2; return }
  if ($level -eq 3) { $p.Range.Style = -4; $p.OutlineLevel = 3; return }
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

function FindFirstRegex($doc, [string]$pattern) {
  $re = [regex]$pattern
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($re.IsMatch($t)) { return $i }
  }
  return 0
}

function FindFirstStartsWith($doc, [string]$prefix) {
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t.StartsWith($prefix)) { return $i }
  }
  return 0
}

function FindNthStartsWith($doc, [string]$prefix, [int]$n) {
  $count = 0
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t.StartsWith($prefix)) {
      $count++
      if ($count -eq $n) { return $i }
    }
  }
  return 0
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

function InsertPara($doc, [int]$pos, [string]$text, [int]$level) {
  $r = $doc.Range($pos, $pos)
  $p = $doc.Paragraphs.Add($r)
  $p.Range.Text = $text
  SetHeading $p $level
  $p
}

function Stash($stashDoc, $srcRange) {
  # New documents always contain a final paragraph mark; insert before it.
  $endPos = $stashDoc.Content.End - 1
  if ($endPos -lt 0) { $endPos = 0 }
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

# Base64(UTF-16LE) literals we set explicitly
$T_13  = "MQAuADMAIAAgAMeRKHWEdhR4dnq5ZdVs"
$T_31  = "MwAuADEAIAAgAAFPGk6fXudlRI2nToVR6JDNYlZTDk4EWW5/c17wU/t8336CafCP"
$T_32  = "MwAuADIAIAAgAO9TTIgnYAZSkGc="
$T_33  = "MwAuADMAIAAgAJ9S/YAAl0JsBlKQZw=="
$T_34  = "MwAuADQAIAAgAF6Xn1L9gACXQmwGUpBn"
$T_35  = "MwAuADUAIAAgACxn4HoPXNN+"

$T_CH4 = "LHvbVuB6IAAgAPt8336+i6GL"
$T_CH5 = "LHuUTuB6IAAgAPt8336eW7Bz"
$T_CH6 = "LHttUeB6IAAgAPt8335LbdWL"
$T_63  = "NgAuADMAIAAgANN+uos="

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

$T_521 = "NQAuADIALgAxACAAIAAodTdipIvBiw5OQ2dQliFqV1efUv2Anluwcw=="
$T_522 = "NQAuADIALgAyACAAIABEjadOoXsGdA5OoVs4aCFqV1efUv2Anluwcw=="
$T_523 = "NQAuADIALgAzACAAIADNYlZTDk6kThNmIWpXV59S/YCeW7Bz"
$T_524 = "NQAuADIALgA0ACAAIADYTj5roVs4aA5OBFluf1JfY2ghaldXn1L9gJ5bsHM="
$T_525 = "NQAuADIALgA1ACAAIADffqGLBlKQZw5Omlv2ZftOoVKfUv2Anluwcw=="
$T_526 = "NQAuADIALgA2ACAAIABzUS6Vnluwc/SLDmY="

$T_61  = "NgAuADEAIAAgAEtt1Yuvc4NY"
$T_62  = "NgAuADIAIAAgAEtt1YuFUblbjFTTfpxn"
$T_621 = "NgAuADIALgAxACAAIACfUv2AS23Viw=="
$T_622 = "NgAuADIALgAyACAAIAAnYP2ADk6JW2hRS23Viw=="

$paperFull = (Resolve-Path -LiteralPath $PaperPath).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $word.Documents.Open($paperFull, $false, $false)
$stashDoc = $null

try {
  SetHeadingStyles $doc
  $stashDoc = $word.Documents.Add()

  $p4prefix = (S $T_CH4).Substring(0,3)
  $p5prefix = (S $T_CH5).Substring(0,3)
  $p6prefix = (S $T_CH6).Substring(0,3)

  # Rename 1.3 heading title.
  $i13 = FindFirstRegex $doc '^1\.3\b'
  if ($i13 -ne 0) { $p = $doc.Paragraphs.Item($i13); $p.Range.Text = (S $T_13); SetHeading $p 2 }

  # Remove 3.4/3.5 from chapter 3 and stash bodies for chapter 4 (inserted before the first old Chapter 4).
  $i34 = FindFirstRegex $doc '^3\.4\b'
  $i35 = FindFirstRegex $doc '^3\.5\b'
  $iOldCh4 = FindFirstStartsWith $doc $p4prefix
  if ($i34 -eq 0 -or $i35 -eq 0 -or $iOldCh4 -eq 0) { throw 'Cannot locate 3.4/3.5 or old Chapter 4.' }

  $archBody = $doc.Range($doc.Paragraphs.Item($i34).Range.End, $doc.Paragraphs.Item($i35).Range.Start)
  $dbBody = $doc.Range($doc.Paragraphs.Item($i35).Range.End, $doc.Paragraphs.Item($iOldCh4).Range.Start)
  $archPos = Stash $stashDoc $archBody
  $sepPos = $stashDoc.Content.End - 1
  if ($sepPos -lt 0) { $sepPos = 0 }
  $stashDoc.Range($sepPos, $sepPos).Text = "`r"
  $dbPos = Stash $stashDoc $dbBody
  $doc.Range($doc.Paragraphs.Item($i34).Range.Start, $doc.Paragraphs.Item($iOldCh4).Range.Start).Delete()

  # Rebuild Chapter 3 headings: 3.1/3.2/3.3/3.4/3.5
  $i31 = FindFirstRegex $doc '^3\.1\b'
  $i32old = FindFirstRegex $doc '^3\.2\b'
  $i33old = FindFirstRegex $doc '^3\.3\b'
  if ($i31 -ne 0) { $p = $doc.Paragraphs.Item($i31); $p.Range.Text = (S $T_31); SetHeading $p 2 }
  if ($i32old -ne 0 -and $i33old -ne 0) {
    $p32 = $doc.Paragraphs.Item($i32old)
    $p33 = $doc.Paragraphs.Item($i33old)
    [void](InsertPara $doc $p32.Range.Start (S $T_32) 2)
    $p32.Range.Text = (S $T_33); SetHeading $p32 2
    $p33.Range.Text = (S $T_34); SetHeading $p33 2
  }
  $iOldCh4 = FindFirstStartsWith $doc $p4prefix
  if ($iOldCh4 -ne 0) { [void](InsertPara $doc $doc.Paragraphs.Item($iOldCh4).Range.Start (S $T_35) 2) }

  # Insert Chapter 4 framework and paste stashed content before the (remaining) old Chapter 4.
  $iOldCh4 = FindFirstStartsWith $doc $p4prefix
  $pos = $doc.Paragraphs.Item($iOldCh4).Range.Start
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

  # Rename the second "第四章 ..." (original detailed design/implementation) to Chapter 5.
  $iOldCh4Impl = FindNthStartsWith $doc $p4prefix 2
  if ($iOldCh4Impl -ne 0) { $p = $doc.Paragraphs.Item($iOldCh4Impl); $p.Range.Text = (S $T_CH5); SetHeading $p 1 }

  # Update module headings within Chapter 5 until the next "第五章 ..." (tests).
  $iCh5Impl = FindFirstStartsWith $doc $p5prefix
  $iCh5Tests = FindNthStartsWith $doc $p5prefix 2
  if ($iCh5Impl -ne 0 -and $iCh5Tests -ne 0 -and $iCh5Tests -gt $iCh5Impl) {
    $map = @{
      '4.1' = (S $T_521)
      '4.2' = (S $T_522)
      '4.3' = (S $T_523)
      '4.4' = (S $T_524)
      '4.5' = (S $T_525)
      '4.6' = (S $T_526)
    }
    for ($i = $iCh5Impl; $i -lt $iCh5Tests; $i++) {
      $pp = $doc.Paragraphs.Item($i)
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
  }

  # Rename the second "第五章 ..." (original tests chapter) to Chapter 6.
  if ($iCh5Tests -ne 0) { $p = $doc.Paragraphs.Item($iCh5Tests); $p.Range.Text = (S $T_CH6); SetHeading $p 1 }

  # Renumber within Chapter 6 and demote extra 5.11+ headings.
  $iCh6Tests = FindFirstStartsWith $doc $p6prefix
  $iCh6Conc = FindLastStartsWith $doc $p6prefix
  if ($iCh6Tests -ne 0 -and $iCh6Conc -ne 0 -and $iCh6Conc -gt $iCh6Tests) {
    for ($i = $iCh6Tests + 1; $i -lt $iCh6Conc; $i++) {
      $pp = $doc.Paragraphs.Item($i)
      if (IsToc $pp) { continue }
      $tt = Clean $pp.Range.Text
      if ($tt -match '^5\.1\b') { $pp.Range.Text = (S $T_61); SetHeading $pp 2; continue }
      if ($tt -match '^5\.2\b') { $pp.Range.Text = (S $T_62); SetHeading $pp 2; continue }
      if ($tt -match '^5\.3\b') { $pp.Range.Text = (S $T_621); SetHeading $pp 3; continue }
      if ($tt -match '^5\.4\b') { $pp.Range.Text = (S $T_622); SetHeading $pp 3; continue }
      if ($tt -match '^5\.(1[1-9]|2[0-9])\b') {
        $pp.Range.Text = ($tt -replace '^5\.', '6.')
        $pp.Range.Style = -1
        $pp.OutlineLevel = 10
      }
    }
  }

  # Convert the last "第六章 ..." (conclusion) to "6.3  结论" section heading.
  if ($iCh6Conc -ne 0) { $pp = $doc.Paragraphs.Item($iCh6Conc); $pp.Range.Text = (S $T_63); SetHeading $pp 2 }

  $doc.Save()
} finally {
  try { if ($null -ne $stashDoc) { $stashDoc.Close($false) | Out-Null } } catch {}
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}
