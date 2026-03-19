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

function IsTocPara($p) {
  try {
    $raw = $p.Range.Text
    if ($raw -match "\t\s*\d+\s*[\r\a]?$") { return $true }
    $name = $p.Range.Style.NameLocal
    if ($name -match "^(?i)toc(\s|$)") { return $true }
    $false
  } catch { $false }
}

function FindParaContains($doc, [string]$needle) {
  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    $t = Clean $p.Range.Text
    if ($t.Contains($needle)) { return $i }
  }
  return 0
}

function FindLastBodyCh1($doc, [string]$ch1Full) {
  for ($i = $doc.Paragraphs.Count; $i -ge 1; $i--) {
    $p = $doc.Paragraphs.Item($i)
    if (IsTocPara $p) { continue }
    $t = Clean $p.Range.Text
    if ($t.StartsWith($ch1Full)) { return $i }
  }
  return 0
}

function AppendFormatted($destDoc, $srcRange) {
  $pos = $destDoc.Content.End - 1
  if ($pos -lt 0) { $pos = 0 }
  $r = $destDoc.Range($pos, $pos)
  $r.FormattedText = $srcRange.FormattedText
}

function AppendPara($doc, [string]$text, [int]$styleId, [int]$outlineLevel) {
  $pos = $doc.Content.End - 1
  if ($pos -lt 0) { $pos = 0 }
  $p = $doc.Paragraphs.Add($doc.Range($pos, $pos))
  $p.Range.Text = $text
  $p.Range.Style = $styleId
  $p.OutlineLevel = $outlineLevel
  $p
}

function AddSkeleton($doc) {
  # Base64 headings (UTF-16LE)
  $CH1 = "LHsATuB6IAAgAOp+uos="
  $CH2 = "LHuMTuB6IAAgAPt83374dnNRgGIvZw=="
  $CH3 = "LHsJTuB6IAAgAPt8334Al0JsBlKQZw=="
  $CH4 = "LHvbVuB6IAAgAPt8336+i6GL"
  $CH5 = "LHuUTuB6IAAgAPt8336eW7Bz"
  $CH6 = "LHttUeB6IAAgAPt8335LbdWL"

  $H11 = "MQAuADEAIAAgAP6LmJjMgG9mylMPYUlO"
  $H12 = "MQAuADIAIAAgAA5U73pzUS6VgGIvZyhX"
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

  $H41  = "NAAuADEAIAAgAPt83347YFNPvouhiw=="
  $H411 = "NAAuADEALgAxACAAIAD7fN9+b4/2TrZnhGc="
  $H412 = "NAAuADEALgAyACAAIAA7YFNPIWpXVxJSBlI="
  $H42  = "NAAuADIAIAAgAJ9S/YAhaldXvouhiw=="
  $H43  = "NAAuADMAIAAgAHBlbmOTXr6LoYs="
  $H44  = "NAAuADQAIAAgACxn4HoPXNN+"

  $H51 = "NQAuADEAIAAgAPt8334AX9FTr3ODWA=="
  $H52 = "NQAuADIAIAAgAPt8336fUv2Anluwcw=="
  $H521 = "NQAuADIALgAxACAAIAAodTdipIvBiw5OQ2dQliFqV1efUv2Anluwcw=="
  $H522 = "NQAuADIALgAyACAAIABEjadOoXsGdCFqV1efUv2Anluwcw=="
  $H523 = "NQAuADIALgAzACAAIADNYlZTDk6kThNmIWpXV59S/YCeW7Bz"
  $H524 = "NQAuADIALgA0ACAAIADYTj5roVs4aA5OBFluf1JfY2ghaldXn1L9gJ5bsHM="
  $H525 = "NQAuADIALgA1ACAAIADffqGLBlKQZw5Omlv2ZftOoVKfUv2Anluwcw=="
  $H526 = "NQAuADIALgA2ACAAIABzUS6Vnluwc/SLDmY="
  $H53  = "NQAuADMAIAAgACxn4HoPXNN+"

  $H61 = "NgAuADEAIAAgAEtt1Yuvc4NY"
  $H62 = "NgAuADIAIAAgAEtt1YuFUblbjFTTfpxn"
  $H63 = "NgAuADMAIAAgACxn4HoPXNN+"

  $TODO = "CP+FX2WICf8=" # （待补）

  AppendPara $doc (S $CH1) -2 1 | Out-Null
  foreach ($h in @($H11,$H12,$H13,$H14)) { AppendPara $doc (S $h) -3 2 | Out-Null; AppendPara $doc (S $TODO) -1 10 | Out-Null }

  AppendPara $doc (S $CH2) -2 1 | Out-Null
  foreach ($h in @($H21,$H22,$H23,$H24,$H25,$H26)) { AppendPara $doc (S $h) -3 2 | Out-Null; AppendPara $doc (S $TODO) -1 10 | Out-Null }

  AppendPara $doc (S $CH3) -2 1 | Out-Null
  foreach ($h in @($H31,$H32,$H33,$H34,$H35)) { AppendPara $doc (S $h) -3 2 | Out-Null; AppendPara $doc (S $TODO) -1 10 | Out-Null }

  AppendPara $doc (S $CH4) -2 1 | Out-Null
  AppendPara $doc (S $H41) -3 2 | Out-Null
  AppendPara $doc (S $H411) -4 3 | Out-Null
  AppendPara $doc (S $H412) -4 3 | Out-Null
  AppendPara $doc (S $TODO) -1 10 | Out-Null
  foreach ($h in @($H42,$H43,$H44)) { AppendPara $doc (S $h) -3 2 | Out-Null; AppendPara $doc (S $TODO) -1 10 | Out-Null }

  AppendPara $doc (S $CH5) -2 1 | Out-Null
  AppendPara $doc (S $H51) -3 2 | Out-Null; AppendPara $doc (S $TODO) -1 10 | Out-Null
  AppendPara $doc (S $H52) -3 2 | Out-Null; AppendPara $doc (S $TODO) -1 10 | Out-Null
  foreach ($h in @($H521,$H522,$H523,$H524,$H525,$H526)) { AppendPara $doc (S $h) -4 3 | Out-Null; AppendPara $doc (S $TODO) -1 10 | Out-Null }
  AppendPara $doc (S $H53) -3 2 | Out-Null; AppendPara $doc (S $TODO) -1 10 | Out-Null

  AppendPara $doc (S $CH6) -2 1 | Out-Null
  foreach ($h in @($H61,$H62,$H63)) { AppendPara $doc (S $h) -3 2 | Out-Null; AppendPara $doc (S $TODO) -1 10 | Out-Null }
}

$T_TOC = "7nYgACAAVV8=" # TOC title
$T_CH1 = "LHsATuB6IAAgAOp+uos=" # Chapter 1 title
$T_APP_B = "RJZVX0IAIAAgAJ9TuouHZYVRuVsI/4VfdGUGdAn/" # Appendix B title

$srcFull = (Resolve-Path -LiteralPath $SourcePath).Path
$outDir = (Resolve-Path -LiteralPath (Split-Path -Parent $OutputPath)).Path
$outFull = Join-Path $outDir (Split-Path -Leaf $OutputPath)

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0

$src = $word.Documents.Open($srcFull, $false, $true) # read-only
$out = $word.Documents.Add()

try {
  $tocTitle = (S $T_TOC)
  $iToc = FindParaContains $src $tocTitle
  if ($iToc -gt 0) {
    $front = $src.Range(0, $src.Paragraphs.Item($iToc).Range.End)
    AppendFormatted $out $front
  } else {
    # Fallback: copy everything up to the first body chapter heading.
    $iCh1 = FindLastBodyCh1 $src (S $T_CH1)
    if ($iCh1 -gt 0) {
      $front = $src.Range(0, $src.Paragraphs.Item($iCh1).Range.Start)
      AppendFormatted $out $front
    }
  }

  # Insert a fresh TOC placeholder (levels 1-2). It can be updated in Word after edits.
  $pos = $out.Content.End - 1
  if ($pos -lt 0) { $pos = 0 }
  $tocRange = $out.Range($pos, $pos)
  $out.TablesOfContents.Add($tocRange, $true, 1, 2) | Out-Null
  # Leave it un-updated for now; it will show entries after headings are added.

  # Insert content after the TOC field range to avoid it being overwritten on update.
  $toc = $out.TablesOfContents.Item(1)
  $afterToc = $out.Range($toc.Range.End, $toc.Range.End)
  $afterToc.InsertParagraphAfter() | Out-Null
  $afterToc = $out.Range($out.Content.End - 1, $out.Content.End - 1)
  $afterToc.InsertBreak(7) | Out-Null

  AddSkeleton $out

  # Now update TOC to pick up skeleton headings.
  try { $out.TablesOfContents.Item(1).Update() | Out-Null } catch {}

  # Append the original body as an appendix block so no content is lost.
  $ch1Idx = FindLastBodyCh1 $src (S $T_CH1)
  if ($ch1Idx -gt 0) {
    AppendPara $out (S $T_APP_B) -2 1 | Out-Null
    $body = $src.Range($src.Paragraphs.Item($ch1Idx).Range.Start, $src.Content.End)
    AppendFormatted $out $body
  }

  try {
    $out.SaveAs2([string]$outFull) | Out-Null
  } catch {
    $out.SaveAs([string]$outFull) | Out-Null
  }
} finally {
  try { $src.Close($false) | Out-Null } catch {}
  try { $out.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}
