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
    if ($name -match "^(?i)toc(\s|$)") { return $true }
    $false
  } catch { $false }
}

function SetHeading($p, [int]$level) {
  if ($level -eq 1) { $p.Range.Style = -2; $p.OutlineLevel = 1; return }
  if ($level -eq 2) { $p.Range.Style = -3; $p.OutlineLevel = 2; return }
  if ($level -eq 3) { $p.Range.Style = -4; $p.OutlineLevel = 3; return }
}

function SetNormal($p) {
  $p.Range.Style = -1
  $p.OutlineLevel = 10
}

function InsertAfterPara($doc, $para, [string]$text, [int]$level) {
  $r = $para.Range.Duplicate
  $r.Collapse(0) | Out-Null
  $next = $doc.Paragraphs.Add($r)
  $next.Range.Text = $text
  if ($level -gt 0) { SetHeading $next $level } else { SetNormal $next }
  $next
}

function InsertBeforePara($doc, $para, [string]$text, [int]$level) {
  $r = $para.Range.Duplicate
  $r.Collapse(1) | Out-Null
  $p = $doc.Paragraphs.Add($r)
  $p.Range.Text = $text
  if ($level -gt 0) { SetHeading $p $level } else { SetNormal $p }
  $p
}

function FindFirstRegex($doc, [string]$pattern, [int]$startIndex = 1) {
  $re = [regex]$pattern
  for ($i = $startIndex; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if (-not $t) { continue }
    if ($re.IsMatch($t)) { return $i }
  }
  return 0
}

function FindFirstText($doc, [string]$needle, [int]$startIndex = 1) {
  for ($i = $startIndex; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if (-not $t) { continue }
    if ($t.Contains($needle)) { return $i }
  }
  return 0
}

# Base64(UTF-16LE) literals (ASCII-only file)
$T_TOC     = "7nYgACAAVV8="               # TOC title
$T_SYS_TEST= "+3zffktt1Ys="               # phrase "system test"
$T_SUM_TEST= "/H4IVEtt1YtoiA5m"           # phrase "summary of tests"

$T_11 = "MQAuADEAIAAgAP6LmJjMgG9mylMPYUlO" # 1.1
$T_13 = "MQAuADMAIAAgAMeRKHWEdhR4dnq5ZdVs" # 1.3
$T_14 = "MQAuADQAIAAgACxnh2XgeoKCiVuSYw==" # 1.4
$T_35 = "MwAuADUAIAAgACxn4HoPXNN+"         # 3.5

$T_CH4 = "LHvbVuB6IAAgAPt8336+i6GL"
$T_41  = "NAAuADEAIAAgAPt83347YFNPvouhiw=="
$T_411 = "NAAuADEALgAxACAAIAD7fN9+b4/2TrZnhGc="
$T_412 = "NAAuADEALgAyACAAIAA7YFNPIWpXVxJSBlI="
$T_42  = "NAAuADIAIAAgAJ9S/YAhaldXvouhiw=="
$T_43  = "NAAuADMAIAAgAHBlbmOTXr6LoYs="
$T_44  = "NAAuADQAIAAgACxn4HoPXNN+"

$T_CH5 = "LHuUTuB6IAAgAPt8336eW7Bz"
$T_51  = "NQAuADEAIAAgAPt8334AX9FTr3ODWA=="
$T_52  = "NQAuADIAIAAgAPt8336fUv2Anluwcw=="
$T_521 = "NQAuADIALgAxACAAIAAodTdipIvBiw5OQ2dQliFqV1efUv2Anluwcw=="
$T_522 = "NQAuADIALgAyACAAIABEjadOoXsGdCFqV1efUv2Anluwcw=="
$T_523 = "NQAuADIALgAzACAAIADNYlZTDk6kThNmIWpXV59S/YCeW7Bz"
$T_524 = "NQAuADIALgA0ACAAIADYTj5roVs4aA5OBFluf1JfY2ghaldXn1L9gJ5bsHM="
$T_525 = "NQAuADIALgA1ACAAIADffqGLBlKQZw5Omlv2ZftOoVKfUv2Anluwcw=="
$T_526 = "NQAuADIALgA2ACAAIABzUS6Vnluwc/SLDmY="

$T_CH6 = "LHttUeB6IAAgAPt8335LbdWL"
$T_61  = "NgAuADEAIAAgAEtt1Yuvc4NY"
$T_62  = "NgAuADIAIAAgAEtt1YuFUblbjFTTfpxn"
$T_621 = "NgAuADIALgAxACAAIAAnYP2ADk6JW2hRS23Viw=="
$T_622 = "NgAuADIALgAyACAAIABLbdWL036cZwZSkGc="
$T_63  = "NgAuADMAIAAgACxn4HoPXNN+"

$T_CONC = "034gACAAuos="

$paperFull = (Resolve-Path -LiteralPath $PaperPath).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $word.Documents.Open($paperFull, $false, $false)

try {
  $chDi = [char]0x7B2C
  $chZhang = [char]0x7AE0

  # Demote accidental "Heading 1" body paragraphs.
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if (-not $t) { continue }
    if ($p.OutlineLevel -eq 1) {
      $head = $t
      if ($head.Length -gt 12) { $head = $head.Substring(0, 12) }
      $looksChapter = $t.StartsWith([string]$chDi) -and $head.Contains([string]$chZhang)
      $looksBack = ($t -eq (S $T_CONC))
      if (-not $looksChapter -and -not $looksBack) { SetNormal $p }
    }
  }

  # Chapter 1: insert missing 1.1 before 1.2.
  $iCh1 = FindFirstRegex $doc ("^" + [regex]::Escape([string]$chDi) + ".*" + [regex]::Escape([string]$chZhang)) 1
  if ($iCh1 -eq 0) { throw "Cannot find first chapter heading." }
  $i12 = FindFirstRegex $doc '^1\.2\b' ($iCh1 + 1)
  $i11 = FindFirstRegex $doc '^1\.1\b' ($iCh1 + 1)
  if ($i12 -ne 0 -and $i11 -eq 0) {
    $p12 = $doc.Paragraphs.Item($i12)
    [void](InsertBeforePara $doc $p12 (S $T_11) 2)
  }

  # Fix merged 1.3 + chapter arrangement text.
  $i13 = FindFirstRegex $doc '^1\.3\b' ($iCh1 + 1)
  if ($i13 -ne 0) {
    $p13 = $doc.Paragraphs.Item($i13)
    $orig = Clean $p13.Range.Text
    $markerFirstChapter = [string]$chDi + [string][char]0x4E00 + [string]$chZhang
    $idx = $orig.IndexOf($markerFirstChapter)
    if ($idx -gt 0) {
      $rest = $orig.Substring($idx).Trim()
      $p13.Range.Text = (S $T_13)
      SetHeading $p13 2
      $p14 = InsertAfterPara $doc $p13 (S $T_14) 2
      if ($rest) { [void](InsertAfterPara $doc $p14 $rest 0) }
    } else {
      $p13.Range.Text = (S $T_13)
      SetHeading $p13 2
      $i14 = FindFirstRegex $doc '^1\.4\b' ($i13 + 1)
      if ($i14 -eq 0) { [void](InsertAfterPara $doc $p13 (S $T_14) 2) }
    }
  }

  # Promote 2.4/2.5/2.6 to Heading 2.
  for ($i = $iCh1 + 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t -match '^2\.[4-6]\b') { SetHeading $p 2 }
    if ($t -match '^3\.[1-5]\b') { break }
  }

  # Chapter 3: demote first body paragraph after chapter heading if needed.
  $markerThirdChapter = [string]$chDi + [string][char]0x4E09 + [string]$chZhang
  $iCh3 = FindFirstText $doc $markerThirdChapter 1
  if ($iCh3 -ne 0) {
    $idxNext = [Math]::Min($iCh3 + 1, $doc.Paragraphs.Count)
    $pNext = $doc.Paragraphs.Item($idxNext)
    if (-not (IsToc $pNext)) {
      $tNext = Clean $pNext.Range.Text
      if ($pNext.OutlineLevel -eq 1 -and -not $tNext.StartsWith([string]$chDi)) { SetNormal $pNext }
    }
  }

  # Remove duplicate "3.3 nonfunctional" line when followed by proper 3.4.
  $iDup = FindFirstRegex $doc '^3\.3\b' 1
  if ($iDup -ne 0) {
    $pDup = $doc.Paragraphs.Item($iDup)
    $tDup = Clean $pDup.Range.Text
    $nonFunc = [string][char]0x975E + [string][char]0x529F + [string][char]0x80FD
    if ($tDup.Contains($nonFunc)) {
      $i34 = FindFirstRegex $doc '^3\.4\b' ($iDup + 1)
      if ($i34 -eq ($iDup + 1)) { $pDup.Range.Delete() | Out-Null }
    }
  }

  # Fix merged 3.5 paragraph and insert Chapter 4 + Chapter 5 skeleton.
  $markerFourthChapter = [string]$chDi + [string][char]0x56DB + [string]$chZhang
  $i35 = FindFirstRegex $doc '^3\.5\b' 1
  if ($i35 -ne 0) {
    $p35 = $doc.Paragraphs.Item($i35)
    $t35 = Clean $p35.Range.Text
    if ($t35.Contains($markerFourthChapter)) {
      $p35.Range.Text = (S $T_35)
      SetHeading $p35 2

      $pCh4 = InsertAfterPara $doc $p35 (S $T_CH4) 1
      $p41 = InsertAfterPara $doc $pCh4 (S $T_41) 2
      [void](InsertAfterPara $doc $p41 (S $T_411) 3)
      [void](InsertAfterPara $doc $p41 (S $T_412) 3)
      $p42 = InsertAfterPara $doc $p41 (S $T_42) 2
      $p43 = InsertAfterPara $doc $p42 (S $T_43) 2
      $p44 = InsertAfterPara $doc $p43 (S $T_44) 2

      $pCh5 = InsertAfterPara $doc $p44 (S $T_CH5) 1
      $p51 = InsertAfterPara $doc $pCh5 (S $T_51) 2
      [void](InsertAfterPara $doc $p51 (S $T_52) 2)
    }
  }

  # Clean module headings.
  $markQuanXian = [string][char]0x6743 + [string][char]0x9650
  $markZiChan = [string][char]0x8D44 + [string][char]0x4EA7
  $markTongJi = [string][char]0x7EDF + [string][char]0x8BA1

  $i521 = FindFirstRegex $doc '^5\.2\.1\b' 1
  if ($i521 -ne 0) {
    $p = $doc.Paragraphs.Item($i521)
    $orig = Clean $p.Range.Text
    $idx = $orig.IndexOf($markQuanXian)
    $body = ""
    if ($idx -ge 0) { $body = $orig.Substring($idx).Trim() }
    $p.Range.Text = (S $T_521)
    SetHeading $p 3
    if ($body) { [void](InsertAfterPara $doc $p $body 0) }

    $idxNext = [Math]::Min($i521 + 1, $doc.Paragraphs.Count)
    $pNext = $doc.Paragraphs.Item($idxNext)
    $tNext = Clean $pNext.Range.Text
    if ($tNext -match '^5\.2\.1\b' -and $tNext -match '^5\.2\.1.*4\.2\b') { $pNext.Range.Delete() | Out-Null }
  }

  $i523maybe = FindFirstRegex $doc '^5\.2\.3\b' 1
  if ($i523maybe -ne 0) {
    $p = $doc.Paragraphs.Item($i523maybe)
    $orig = Clean $p.Range.Text
    $idx = $orig.IndexOf($markZiChan)
    if ($idx -ge 0 -and $orig -match '^5\.2\.3.*4\.4\b') {
      $body = $orig.Substring($idx).Trim()
      $p.Range.Text = (S $T_522)
      SetHeading $p 3
      if ($body) { [void](InsertAfterPara $doc $p $body 0) }
    }
  }

  $i523 = FindFirstRegex $doc '^5\.2\.3\b' 1
  if ($i523 -ne 0) {
    $p = $doc.Paragraphs.Item($i523)
    $p.Range.Text = (S $T_523)
    SetHeading $p 3
  }

  $i525 = FindFirstRegex $doc '^5\.2\.5\b' 1
  if ($i525 -ne 0) {
    $p = $doc.Paragraphs.Item($i525)
    $orig = Clean $p.Range.Text
    $idx = $orig.IndexOf($markTongJi)
    $body = ""
    if ($idx -ge 0) { $body = $orig.Substring($idx).Trim() }
    $p.Range.Text = (S $T_525)
    SetHeading $p 3
    if ($body) { [void](InsertAfterPara $doc $p $body 0) }
  }

  $i526 = FindFirstRegex $doc '^5\.2\.6\b' 1
  if ($i526 -ne 0) { $p = $doc.Paragraphs.Item($i526); $p.Range.Text = (S $T_526); SetHeading $p 3 }

  # Convert "Chapter 5 tests" to Chapter 6 and renumber test headings.
  $iSysTest = FindFirstText $doc (S $T_SYS_TEST) 1
  if ($iSysTest -ne 0) {
    $p = $doc.Paragraphs.Item($iSysTest)
    $t = Clean $p.Range.Text
    $markerFifthChapter = [string]$chDi + [string][char]0x4E94 + [string]$chZhang
    if ($t.StartsWith($markerFifthChapter)) { $p.Range.Text = (S $T_CH6); SetHeading $p 1 }
  }

  $iCh6 = FindFirstText $doc (S $T_CH6).Substring(0, 3) 1
  if ($iCh6 -ne 0) {
    $chZhi = [char]0x81F4
    $chCan = [char]0x53C2
    for ($i = $iCh6 + 1; $i -le $doc.Paragraphs.Count; $i++) {
      $p = $doc.Paragraphs.Item($i)
      if (IsToc $p) { continue }
      $t = Clean $p.Range.Text
      if (-not $t) { continue }
      if ($t -match '^5\.1\b') { $p.Range.Text = (S $T_61); SetHeading $p 2; continue }
      if ($t -match '^5\.2\b') { $p.Range.Text = (S $T_62); SetHeading $p 2; continue }
      if ($t -match '^5\.3\b') { $p.Range.Text = (S $T_621); SetHeading $p 3; continue }
      if ($t -match '^5\.4\b') { $p.Range.Text = (S $T_622); SetHeading $p 3; continue }
      if ($t.StartsWith([string]$chZhi) -or $t.StartsWith([string]$chCan)) { break }
    }

    $iSum = FindFirstText $doc (S $T_SUM_TEST) ($iCh6 + 1)
    if ($iSum -ne 0) {
      $pSum = $doc.Paragraphs.Item($iSum)
      $i63 = FindFirstRegex $doc '^6\.3\b' ($iCh6 + 1)
      if ($i63 -eq 0 -or $i63 -gt $iSum) { [void](InsertBeforePara $doc $pSum (S $T_63) 2) }
    }
  }

  # Convert corrupted "6.3 conclusion..." into a standalone conclusion heading.
  $iConc = FindFirstRegex $doc '^6\.3\b' 1
  if ($iConc -ne 0) {
    $p = $doc.Paragraphs.Item($iConc)
    $orig = Clean $p.Range.Text
    $mark = [string][char]0x7ED3 + [string][char]0x8BBA
    if ($orig.Contains($mark) -and $orig.Length -gt 20) {
      $idx = $orig.IndexOf($mark)
      $body = ""
      if ($idx -ge 0) { $body = $orig.Substring($idx).Trim() }
      $p.Range.Text = (S $T_CONC)
      SetHeading $p 1
      if ($body) { [void](InsertAfterPara $doc $p $body 0) }
    }
  }

  # Rebuild TOC (levels 1-2).
  $iTocTitle = FindFirstText $doc (S $T_TOC) 1
  $markerFirstChapter = [string]$chDi + [string][char]0x4E00 + [string]$chZhang
  $iFirstChapter = FindFirstText $doc $markerFirstChapter 1
  if ($iTocTitle -ne 0 -and $iFirstChapter -ne 0 -and $iFirstChapter -gt $iTocTitle) {
    $start = $doc.Paragraphs.Item($iTocTitle).Range.End
    $end = $doc.Paragraphs.Item($iFirstChapter).Range.Start
    if ($end -gt $start) { $doc.Range($start, $end).Delete() | Out-Null }
    $tocRange = $doc.Range($start, $start)
    if ($doc.TablesOfContents.Count -ge 1) {
      for ($k = $doc.TablesOfContents.Count; $k -ge 1; $k--) { $doc.TablesOfContents.Item($k).Delete() | Out-Null }
    }
    $doc.TablesOfContents.Add($tocRange, $true, 1, 2) | Out-Null
    $doc.TablesOfContents.Item(1).Update() | Out-Null
  }

  $doc.Save()
} finally {
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}
