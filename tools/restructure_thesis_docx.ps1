param(
  [Parameter(Mandatory = $true)]
  [string]$PaperPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# PowerShell 5.1 may read script files as ANSI by default.
# Keep this file ASCII-only: all Chinese strings are Base64(UTF-16LE) decoded at runtime.
function S([string]$b64) {
  [Text.Encoding]::Unicode.GetString([Convert]::FromBase64String($b64))
}

# Base64(UTF-16LE) literals
$B64_DI = "LHs="
$B64_ZHANG = "4Ho="

$T_CH2 = "LHuMTuB6IAAgAPt83374dnNRgGIvZw=="
$T_CH3 = "LHsJTuB6IAAgAPt8334Al0JsBlKQZw=="
$T_CH4 = "LHvbVuB6IAAgAPt8336+i6GL"
$T_CH5 = "LHuUTuB6IAAgAPt8336eW7Bz"
$T_CH6 = "LHttUeB6IAAgAPt8335LbdWL"

$T_63  = "NgAuADMAIAAgANN+uos="
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

$T_ACK = "9IEgACAAIow="
$T_REF = "wlMgAAOAIACHZSAALnM="
$T_APP = "RJZVXw=="

function Clean-ParaText([string]$t) {
  if ($null -eq $t) { return "" }
  ($t -replace "[\r\a]", "").Trim()
}

function Is-TocStyle($para) {
  try {
    $style = $para.Range.Style
    $name = $style.NameLocal
    if ($name -match "^(?i)toc\\s") { return $true }
    # Some TOC result lines may carry non-TOC styles (e.g., Heading/Title) but still contain the tab+page-number pattern.
    $raw = $para.Range.Text
    if ($raw -match "\t\s*\d+\s*[\r\a]?$") { return $true }
    $false
  } catch {
    $false
  }
}

function ChapterRegex() {
  $di = [regex]::Escape((S $B64_DI))
  $zhang = [regex]::Escape((S $B64_ZHANG))
  [regex]("^$di.{1,6}$zhang")
}

function Find-FirstBodyChapterIndex($doc) {
  $re = ChapterRegex
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (Is-TocStyle $p) { continue }
    $text = Clean-ParaText $p.Range.Text
    if ($re.IsMatch($text)) { return $i }
  }
  throw "Cannot find first body chapter heading paragraph."
}

function Find-BodyChapterParagraphIndices($doc, [int]$startIndex) {
  $re = ChapterRegex
  $indices = New-Object System.Collections.Generic.List[int]
  for ($i = $startIndex; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (Is-TocStyle $p) { continue }
    $text = Clean-ParaText $p.Range.Text
    if ($re.IsMatch($text)) { [void]$indices.Add($i) }
  }
  $indices
}

function Find-BackMatterStartIndex($doc, [int]$fromIndex) {
  $ack = S $T_ACK
  $ref = S $T_REF
  $app = S $T_APP
  for ($i = $fromIndex; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (Is-TocStyle $p) { continue }
    $t = Clean-ParaText $p.Range.Text
    if ($t -eq $ack -or $t -eq $ref -or $t.StartsWith($app)) { return $i }
  }
  ($doc.Paragraphs.Count + 1)
}

function Range-FromParaStartToParaStart($doc, [int]$startParaIndex, [int]$endParaIndex) {
  $start = $doc.Paragraphs.Item($startParaIndex).Range.Start
  $end = $doc.Paragraphs.Item($endParaIndex).Range.Start
  $doc.Range($start, $end)
}

function Range-BodyBetweenParagraphs($doc, [int]$startAfterParaIndex, [int]$endParaIndex) {
  $start = $doc.Paragraphs.Item($startAfterParaIndex).Range.End
  $end = $doc.Paragraphs.Item($endParaIndex).Range.Start
  $doc.Range($start, $end)
}

function Stash-FormattedRange($stashDoc, $srcRange) {
  $endPos = $stashDoc.Content.End
  $dest = $stashDoc.Range($endPos, $endPos)
  $start = $dest.Start
  $dest.FormattedText = $srcRange.FormattedText
  @($start, $dest.End)
}

function Insert-Paragraph($doc, $atPos, [string]$text, [int]$styleId, [int]$outlineLevel) {
  $r = $doc.Range($atPos, $atPos)
  $p = $doc.Paragraphs.Add($r)
  $p.Range.Text = $text
  $p.Range.Style = $styleId
  $p.OutlineLevel = $outlineLevel
  $p
}

function Insert-FormattedText($doc, $atPos, $stashDoc, [int]$stashStart, [int]$stashEnd) {
  $r = $doc.Range($atPos, $atPos)
  $src = $stashDoc.Range($stashStart, $stashEnd)
  $r.FormattedText = $src.FormattedText
  $r
}

function Set-HeadingStyles($doc) {
  foreach ($sid in @(-2, -3, -4)) {
    try {
      $st = $doc.Styles.Item($sid)
      $st.Font.NameFarEast = "SimHei"
      $st.Font.Bold = $true
    } catch {}
  }
  try {
    $h1 = $doc.Styles.Item(-2)
    $h1.Font.Size = 15
    $h1.ParagraphFormat.Alignment = 1
    $h1.ParagraphFormat.SpaceBefore = 12
    $h1.ParagraphFormat.SpaceAfter = 12
  } catch {}
  try {
    $h2 = $doc.Styles.Item(-3)
    $h2.Font.Size = 14
    $h2.ParagraphFormat.Alignment = 0
    $h2.ParagraphFormat.SpaceBefore = 6
    $h2.ParagraphFormat.SpaceAfter = 6
  } catch {}
  try {
    $h3 = $doc.Styles.Item(-4)
    $h3.Font.Size = 12
    $h3.ParagraphFormat.Alignment = 0
    $h3.ParagraphFormat.SpaceBefore = 3
    $h3.ParagraphFormat.SpaceAfter = 3
  } catch {}
}

function Replace-InRange($range, [string]$findText, [string]$replaceText) {
  $find = $range.Find
  $find.ClearFormatting() | Out-Null
  $find.Replacement.ClearFormatting() | Out-Null
  $find.Text = $findText
  $find.Replacement.Text = $replaceText
  $find.Forward = $true
  $find.Wrap = 0
  $find.Format = $false
  $find.MatchCase = $false
  $find.MatchWholeWord = $false
  $find.MatchWildcards = $false
  [void]$find.Execute($null,$null,$null,$null,$null,$null,$null,$null,$null,$null,2)
}

function Find-SectionIndex($doc, [int]$from, [int]$to, [string]$prefix) {
  for ($i = $from; $i -lt $to; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (Is-TocStyle $p) { continue }
    $t = Clean-ParaText $p.Range.Text
    if ($t -match ("^" + [regex]::Escape($prefix) + "\\b")) { return $i }
  }
  0
}

$paperFull = (Resolve-Path -LiteralPath $PaperPath).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0

$doc = $word.Documents.Open($paperFull, $false, $false)
$stash = $null

try {
  Set-HeadingStyles $doc

  $bodyStart = Find-FirstBodyChapterIndex $doc
  $chap = Find-BodyChapterParagraphIndices $doc $bodyStart
  if ($chap.Count -lt 6) { throw "Expected >=6 body chapters, found $($chap.Count)." }

  $ch1 = $chap[0]; $ch2 = $chap[1]; $ch3 = $chap[2]; $ch4 = $chap[3]; $ch5Tests = $chap[4]; $ch6Conc = $chap[5]
  $backStart = Find-BackMatterStartIndex $doc ($ch6Conc + 1)

  foreach ($idx in @($ch1,$ch2,$ch3,$ch4,$ch5Tests)) {
    $p = $doc.Paragraphs.Item($idx)
    if (-not (Is-TocStyle $p)) { $p.Range.Style = -2; $p.OutlineLevel = 1 }
  }

  $doc.Paragraphs.Item($ch2).Range.Text = (S $T_CH2)
  $doc.Paragraphs.Item($ch3).Range.Text = (S $T_CH3)
  $doc.Paragraphs.Item($ch4).Range.Text = (S $T_CH4)
  $doc.Paragraphs.Item($ch5Tests).Range.Text = (S $T_CH6)

  $sec13 = Find-SectionIndex $doc ($ch1 + 1) $ch2 "1.3"
  if ($sec13 -ne 0) {
    $p = $doc.Paragraphs.Item($sec13)
    $p.Range.Text = (S $T_13)
    $p.Range.Style = -3
    $p.OutlineLevel = 2
  }

  $stash = $word.Documents.Add()

  $i34 = Find-SectionIndex $doc ($ch3 + 1) $ch4 "3.4"
  $i35 = Find-SectionIndex $doc ($ch3 + 1) $ch4 "3.5"
  if ($i34 -eq 0 -or $i35 -eq 0) { throw "Cannot locate 3.4/3.5 in chapter 3 body." }

  $archBody = Range-BodyBetweenParagraphs $doc $i34 $i35
  $dbBody = Range-BodyBetweenParagraphs $doc $i35 $ch4
  $archPos = Stash-FormattedRange $stash $archBody
  $stash.Range($stash.Content.End, $stash.Content.End).Text = "`r"
  $dbPos = Stash-FormattedRange $stash $dbBody

  (Range-FromParaStartToParaStart $doc $i34 $ch4).Delete()

  $bodyStart = Find-FirstBodyChapterIndex $doc
  $chap = Find-BodyChapterParagraphIndices $doc $bodyStart
  $ch1 = $chap[0]; $ch2 = $chap[1]; $ch3 = $chap[2]; $ch4 = $chap[3]; $ch5Tests = $chap[4]; $ch6Conc = $chap[5]

  $sec31 = Find-SectionIndex $doc ($ch3 + 1) $ch4 "3.1"
  $sec32old = Find-SectionIndex $doc ($ch3 + 1) $ch4 "3.2"
  $sec33old = Find-SectionIndex $doc ($ch3 + 1) $ch4 "3.3"
  if ($sec31 -eq 0 -or $sec32old -eq 0 -or $sec33old -eq 0) { throw "Cannot locate 3.1/3.2/3.3 after removing 3.4/3.5." }

  $p31 = $doc.Paragraphs.Item($sec31)
  $p31.Range.Text = (S $T_31)
  $p31.Range.Style = -3
  $p31.OutlineLevel = 2

  $p32old = $doc.Paragraphs.Item($sec32old)
  $p33old = $doc.Paragraphs.Item($sec33old)

  [void](Insert-Paragraph $doc $p32old.Range.Start (S $T_32) -3 2)
  $p32old.Range.Text = (S $T_33)
  $p32old.Range.Style = -3
  $p32old.OutlineLevel = 2
  $p33old.Range.Text = (S $T_34)
  $p33old.Range.Style = -3
  $p33old.OutlineLevel = 2

  [void](Insert-Paragraph $doc $doc.Paragraphs.Item($ch4).Range.Start (S $T_35) -3 2)

  $bodyStart = Find-FirstBodyChapterIndex $doc
  $chap = Find-BodyChapterParagraphIndices $doc $bodyStart
  $ch4 = $chap[3]; $ch5Tests = $chap[4]; $ch6Conc = $chap[5]

  $ch4Body = Range-BodyBetweenParagraphs $doc $ch4 $ch5Tests
  $implPos = Stash-FormattedRange $stash $ch4Body
  $ch4Body.Delete()

  $pCh4 = $doc.Paragraphs.Item($ch4)
  $pCh4.Range.Text = (S $T_CH4)
  $pCh4.Range.Style = -2
  $pCh4.OutlineLevel = 1

  $p41 = Insert-Paragraph $doc $pCh4.Range.End (S $T_41) -3 2
  $p411 = Insert-Paragraph $doc $p41.Range.End (S $T_411) -4 3
  $archInserted = Insert-FormattedText $doc $p411.Range.End $stash $archPos[0] $archPos[1]
  $p412 = Insert-Paragraph $doc $archInserted.End (S $T_412) -4 3
  $p42 = Insert-Paragraph $doc $p412.Range.End (S $T_42) -3 2
  $p421 = Insert-Paragraph $doc $p42.Range.End (S $T_421) -4 3
  $p422 = Insert-Paragraph $doc $p421.Range.End (S $T_422) -4 3
  $p423 = Insert-Paragraph $doc $p422.Range.End (S $T_423) -4 3
  $p424 = Insert-Paragraph $doc $p423.Range.End (S $T_424) -4 3
  $p425 = Insert-Paragraph $doc $p424.Range.End (S $T_425) -4 3
  $p43 = Insert-Paragraph $doc $p425.Range.End (S $T_43) -3 2
  $dbInserted = Insert-FormattedText $doc $p43.Range.End $stash $dbPos[0] $dbPos[1]
  [void](Insert-Paragraph $doc $dbInserted.End (S $T_44) -3 2)

  $bodyStart = Find-FirstBodyChapterIndex $doc
  $chap = Find-BodyChapterParagraphIndices $doc $bodyStart
  $ch5Tests = $chap[4]

  $pCh5 = Insert-Paragraph $doc $doc.Paragraphs.Item($ch5Tests).Range.Start (S $T_CH5) -2 1
  $p51 = Insert-Paragraph $doc $pCh5.Range.End (S $T_51) -3 2
  $p52 = Insert-Paragraph $doc $p51.Range.End (S $T_52) -3 2
  $implInserted = Insert-FormattedText $doc $p52.Range.End $stash $implPos[0] $implPos[1]
  $implRange = $doc.Range($implInserted.Start, $implInserted.End)

  $map = New-Object 'System.Collections.Generic.Dictionary[string,string]'
  $map.Add("4.1", (S $T_521))
  $map.Add("4.2", (S $T_522))
  $map.Add("4.3", (S $T_523))
  $map.Add("4.4", (S $T_524))
  $map.Add("4.5", (S $T_525))
  $map.Add("4.6", (S $T_526))

  for ($i = 1; $i -le $implRange.Paragraphs.Count; $i++) {
    $pp = $implRange.Paragraphs.Item($i)
    if (Is-TocStyle $pp) { continue }
    $tt = Clean-ParaText $pp.Range.Text
    foreach ($k in $map.Keys) {
      if ($tt -match ("^" + [regex]::Escape($k) + "\\b")) {
        $pp.Range.Text = $map[$k]
        $pp.Range.Style = -4
        $pp.OutlineLevel = 3
        break
      }
    }
  }

  Replace-InRange $implRange "图4-" "图5-"
  Replace-InRange $implRange "表4-" "表5-"
  Replace-InRange $implRange "代码清单4-" "代码清单5-"

  [void](Insert-Paragraph $doc $implRange.End (S $T_53) -3 2)

  $bodyStart = Find-FirstBodyChapterIndex $doc
  $chap = Find-BodyChapterParagraphIndices $doc $bodyStart
  if ($chap.Count -lt 7) { throw "After inserting chapter 5, expected >=7 chapters, found $($chap.Count)." }
  $ch6Tests = $chap[5]
  $ch6Conc = $chap[6]
  $backStart = Find-BackMatterStartIndex $doc ($ch6Conc + 1)

  $pTest = $doc.Paragraphs.Item($ch6Tests)
  $pTest.Range.Text = (S $T_CH6)
  $pTest.Range.Style = -2
  $pTest.OutlineLevel = 1

  for ($i = $ch6Tests + 1; $i -lt $ch6Conc; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (Is-TocStyle $p) { continue }
    $t = Clean-ParaText $p.Range.Text
    if ($t -match "^5\\.1\\b") { $p.Range.Text = (S $T_61); $p.Range.Style = -3; $p.OutlineLevel = 2; continue }
    if ($t -match "^5\\.2\\b") { $p.Range.Text = (S $T_62); $p.Range.Style = -3; $p.OutlineLevel = 2; continue }
    if ($t -match "^5\\.3\\b") { $p.Range.Text = (S $T_621); $p.Range.Style = -4; $p.OutlineLevel = 3; continue }
    if ($t -match "^5\\.4\\b") { $p.Range.Text = (S $T_622); $p.Range.Style = -4; $p.OutlineLevel = 3; continue }
    if ($t -match "^5\\.(1[1-9]|2[0-9])\\b") {
      $p.Range.Text = ($t -replace "^5\\.", "6.")
      $p.Range.Style = -1
      $p.OutlineLevel = 10
    }
  }

  $pConc = $doc.Paragraphs.Item($ch6Conc)
  $pConc.Range.Text = (S $T_63)
  $pConc.Range.Style = -3
  $pConc.OutlineLevel = 2

  $doc.Save()
} finally {
  try { if ($null -ne $stash) { $stash.Close($false) | Out-Null } } catch {}
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}
