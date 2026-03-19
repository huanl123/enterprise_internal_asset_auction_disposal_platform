param(
  [Parameter(Mandatory = $true)]
  [string]$SourcePath,
  [Parameter(Mandatory = $true)]
  [string]$OutputPath
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
    if ($name -match "^(?i)toc\s") { return $true }
    $false
  } catch { $false }
}

function FindParaStartsWith($doc, [string]$prefix) {
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t.StartsWith($prefix)) { return $i }
  }
  return 0
}

function FindParaRegex($doc, [string]$pattern) {
  $re = [regex]$pattern
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($re.IsMatch($t)) { return $i }
  }
  return 0
}

function AppendFormatted($destDoc, $srcRange) {
  $pos = $destDoc.Content.End - 1
  if ($pos -lt 0) { $pos = 0 }
  $r = $destDoc.Range($pos, $pos)
  $r.FormattedText = $srcRange.FormattedText
}

function AppendTextPara($destDoc, [string]$text, [int]$styleId, [int]$outlineLevel) {
  $pos = $destDoc.Content.End - 1
  if ($pos -lt 0) { $pos = 0 }
  $p = $destDoc.Paragraphs.Add($destDoc.Range($pos, $pos))
  $p.Range.Text = $text
  $p.Range.Style = $styleId
  $p.OutlineLevel = $outlineLevel
}

# Base64 literals
$CH1 = "LHsATuB6IAAgAOp+uos="                 # 第一章  绪论
$CH2 = "LHuMTuB6IAAgAPt83374dnNRgGIvZw=="     # 第二章  系统相关技术
$CH3 = "LHsJTuB6IAAgAPt8334Al0JsBlKQZw=="     # 第三章  系统需求分析
$CH4 = "LHvbVuB6IAAgAPt8336+i6GL"             # 第四章  系统设计
$CH5 = "LHuUTuB6IAAgAPt8336eW7Bz"             # 第五章  系统实现
$CH6 = "LHttUeB6IAAgAPt8335LbdWL"             # 第六章  系统测试
$CH5_TESTS = "LHuUTuB6IAAgAPt8335LbdWLylMnYP2ABlKQZw==" # 第五章  系统测试及性能分析
$CH6_CONC = "LHttUeB6IAAgANN+uos="            # 第六章  结论
$S63 = "NgAuADMAIAAgANN+uos="                 # 6.3  结论
$H41 = "NAAuADEAIAAgAPt83347YFNPvouhiw=="
$H42 = "NAAuADIAIAAgAJ9S/YAhaldXvouhiw=="
$H43 = "NAAuADMAIAAgAHBlbmOTXr6LoYs="
$H44 = "NAAuADQAIAAgACxn4HoPXNN+"
$H51 = "NQAuADEAIAAgAPt8334AX9FTr3ODWA=="
$H52 = "NQAuADIAIAAgAPt8336fUv2Anluwcw=="
$H53 = "NQAuADMAIAAgACxn4HoPXNN+"

$srcFull = (Resolve-Path -LiteralPath $SourcePath).Path
$outFull = (Resolve-Path -LiteralPath (Split-Path -Parent $OutputPath)).Path + "\\" + (Split-Path -Leaf $OutputPath)

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0

$src = $word.Documents.Open($srcFull, $false, $true)  # read-only
$out = $word.Documents.Add()

try {
  # Copy front matter up to Chapter 1 (if found).
  $ch1Prefix = (S $CH1)
  $iCh1 = FindParaStartsWith $src $ch1Prefix
  if ($iCh1 -gt 0) {
    $front = $src.Range(0, $src.Paragraphs.Item($iCh1).Range.Start)
    AppendFormatted $out $front
  }

  # Find chapter anchors in source (best effort).
  $p1 = (S $CH1).Substring(0,3)
  $p2 = (S $CH2).Substring(0,3)
  $p3 = (S $CH3).Substring(0,3)
  $p5 = (S $CH5).Substring(0,3)
  $p6 = (S $CH6).Substring(0,3)

  $i1 = FindParaStartsWith $src $p1
  $i2 = FindParaStartsWith $src $p2
  $i3 = FindParaStartsWith $src $p3
  $i5tests = FindParaStartsWith $src (S $CH5_TESTS)
  if ($i5tests -eq 0) { $i5tests = FindParaStartsWith $src $p5 }
  $i6conc = FindParaStartsWith $src (S $CH6_CONC)
  if ($i6conc -eq 0) { $i6conc = FindParaStartsWith $src $p6 }

  # Append clean main body skeleton + paste best-effort content blocks.
  if ($i1 -gt 0 -and $i2 -gt $i1) {
    AppendTextPara $out (S $CH1) -2 1
    AppendFormatted $out ($src.Range($src.Paragraphs.Item($i1).Range.End, $src.Paragraphs.Item($i2).Range.Start))
  }
  if ($i2 -gt 0 -and $i3 -gt $i2) {
    AppendTextPara $out (S $CH2) -2 1
    AppendFormatted $out ($src.Range($src.Paragraphs.Item($i2).Range.End, $src.Paragraphs.Item($i3).Range.Start))
  }
  if ($i3 -gt 0 -and $i5tests -gt $i3) {
    AppendTextPara $out (S $CH3) -2 1
    AppendFormatted $out ($src.Range($src.Paragraphs.Item($i3).Range.End, $src.Paragraphs.Item($i5tests).Range.Start))
  }

  # Chapter 4/5 placeholders (structure only).
  AppendTextPara $out (S $CH4) -2 1
  AppendTextPara $out (S $H41) -3 2
  AppendTextPara $out (S $H42) -3 2
  AppendTextPara $out (S $H43) -3 2
  AppendTextPara $out (S $H44) -3 2

  AppendTextPara $out (S $CH5) -2 1
  AppendTextPara $out (S $H51) -3 2
  AppendTextPara $out (S $H52) -3 2
  if ($i5tests -gt 0) {
    # Try to paste implementation-ish content: from first 5.2.1 or 4.1 up to tests heading.
    $iImpl = FindParaRegex $src '^5\\.2\\.1\\b'
    if ($iImpl -eq 0) { $iImpl = FindParaRegex $src '^4\\.1\\b' }
    if ($iImpl -gt 0) {
      AppendFormatted $out ($src.Range($src.Paragraphs.Item($iImpl).Range.Start, $src.Paragraphs.Item($i5tests).Range.Start))
    }
  }
  AppendTextPara $out (S $H53) -3 2

  # Chapter 6 tests + conclusion.
  AppendTextPara $out (S $CH6) -2 1
  if ($i5tests -gt 0 -and $i6conc -gt $i5tests) {
    AppendFormatted $out ($src.Range($src.Paragraphs.Item($i5tests).Range.End, $src.Paragraphs.Item($i6conc).Range.Start))
  }
  AppendTextPara $out (S $S63) -3 2
  if ($i6conc -gt 0) {
    AppendFormatted $out ($src.Range($src.Paragraphs.Item($i6conc).Range.End, $src.Content.End))
  }

  $out.SaveAs([ref]$outFull)
} finally {
  try { $src.Close($false) | Out-Null } catch {}
  try { $out.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}
