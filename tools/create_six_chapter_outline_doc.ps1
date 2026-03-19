param(
  [Parameter(Mandatory = $true)]
  [string]$OutputPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function S([string]$b64) { [Text.Encoding]::Unicode.GetString([Convert]::FromBase64String($b64)) }

function AddP($doc, [string]$text, [int]$styleId, [int]$outline) {
  $r = $doc.Content.Duplicate
  $r.Collapse(0) | Out-Null
  $p = $doc.Paragraphs.Add($r)
  $p.Range.Text = $text
  $p.Range.Style = $styleId
  $p.OutlineLevel = $outline
}

# Base64 headings (UTF-16LE)
$T_TOC = "7nYgACAAVV8=" # TOC title
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

$outDir = (Resolve-Path -LiteralPath (Split-Path -Parent $OutputPath)).Path
$outFull = Join-Path $outDir (Split-Path -Leaf $OutputPath)

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $word.Documents.Add()

try {
  # TOC placeholder (build TOC in Word after headings exist)
  AddP $doc (S $T_TOC) -1 10
  $br = $doc.Range($doc.Content.End - 1, $doc.Content.End - 1)
  $br.InsertBreak(7) | Out-Null

  # 6 chapters skeleton (Heading 1/2/3)
  AddP $doc (S $CH1) -2 1
  foreach($h in @($H11,$H12,$H13,$H14)) { AddP $doc (S $h) -3 2; AddP $doc (S $TODO) -1 10 }

  AddP $doc (S $CH2) -2 1
  foreach($h in @($H21,$H22,$H23,$H24,$H25,$H26)) { AddP $doc (S $h) -3 2; AddP $doc (S $TODO) -1 10 }

  AddP $doc (S $CH3) -2 1
  foreach($h in @($H31,$H32,$H33,$H34,$H35)) { AddP $doc (S $h) -3 2; AddP $doc (S $TODO) -1 10 }

  AddP $doc (S $CH4) -2 1
  AddP $doc (S $H41) -3 2
  AddP $doc (S $H411) -4 3
  AddP $doc (S $H412) -4 3
  AddP $doc (S $TODO) -1 10
  foreach($h in @($H42,$H43,$H44)) { AddP $doc (S $h) -3 2; AddP $doc (S $TODO) -1 10 }

  AddP $doc (S $CH5) -2 1
  AddP $doc (S $H51) -3 2; AddP $doc (S $TODO) -1 10
  AddP $doc (S $H52) -3 2; AddP $doc (S $TODO) -1 10
  foreach($h in @($H521,$H522,$H523,$H524,$H525,$H526)) { AddP $doc (S $h) -4 3; AddP $doc (S $TODO) -1 10 }
  AddP $doc (S $H53) -3 2; AddP $doc (S $TODO) -1 10

  AddP $doc (S $CH6) -2 1
  foreach($h in @($H61,$H62,$H63)) { AddP $doc (S $h) -3 2; AddP $doc (S $TODO) -1 10 }

  try { $doc.SaveAs2([string]$outFull) | Out-Null } catch { $doc.SaveAs([string]$outFull) | Out-Null }
} finally {
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}
