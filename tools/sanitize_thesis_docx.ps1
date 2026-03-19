param(
  [Parameter(Mandatory = $true)]
  [string]$PaperPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function S([string]$b64) {
  [Text.Encoding]::Unicode.GetString([Convert]::FromBase64String($b64))
}

# Base64(UTF-16LE) literals (ASCII-only file)
$T_CH1 = "LHsATuB6IAAgAOp+uos="
$T_CH2 = "LHuMTuB6IAAgAPt83374dnNRgGIvZw=="
$T_CH3 = "LHsJTuB6IAAgAPt8334Al0JsBlKQZw=="
$T_CH4 = "LHvbVuB6IAAgAPt8336+i6GL"
$T_CH6 = "LHttUeB6IAAgAPt8335LbdWL"

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

function InsertAfterPara($doc, $para, [string]$text, [int]$level) {
  $r = $para.Range.Duplicate
  $r.Collapse(0) | Out-Null
  $next = $doc.Paragraphs.Add($r)
  $next.Range.Text = $text
  if ($level -gt 0) { SetHeading $next $level } else { $next.Range.Style = -1; $next.OutlineLevel = 10 }
  $next
}

function SplitPrefixToNewPara($doc, $para, [string]$prefix, [int]$level, [string]$marker1, [string]$marker2) {
  $t = Clean $para.Range.Text
  # Tolerate variable whitespace in the prefix.
  $esc = [regex]::Escape($prefix)
  $pat = "^" + ($esc -replace "\\\\ ", "\\s+")
  $m = [regex]::Match($t, $pat)
  if (-not $m.Success) { return $false }
  if ($t.Length -le $m.Length) { return $false }

  $para.Range.Text = $prefix
  SetHeading $para $level

  $rest = (Clean $t.Substring($m.Length))
  if (-not $rest) { return $true }

  # If markers are present (e.g. 2.1 / 2.2), split into multiple heading paragraphs.
  if ($marker1 -and $marker2) {
    $i1 = $rest.IndexOf($marker1)
    $i2 = $rest.IndexOf($marker2)
    if ($i1 -ge 0 -and $i2 -gt $i1) {
      $seg1 = $rest.Substring($i1, $i2 - $i1).Trim()
      $seg2 = $rest.Substring($i2).Trim()
      $p1 = InsertAfterPara $doc $para $seg1 2
      [void](InsertAfterPara $doc $p1 $seg2 2)
      return $true
    }
  }

  [void](InsertAfterPara $doc $para $rest 0)
  return $true
}

$paperFull = (Resolve-Path -LiteralPath $PaperPath).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $word.Documents.Open($paperFull, $false, $false)

try {
  $ch1 = S $T_CH1
  $ch2 = S $T_CH2
  $ch3 = S $T_CH3
  $ch4 = S $T_CH4
  $ch6 = S $T_CH6

  $p1 = $ch1.Substring(0, 3)
  $p2 = $ch2.Substring(0, 3)
  $p3 = $ch3.Substring(0, 3)
  $p6 = $ch6.Substring(0, 3)

  for ($i = 1; $i -le $doc.Paragraphs.Count; $i++) {
    $p = $doc.Paragraphs.Item($i)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if (-not $t) { continue }

    # Demote chapter-arrangement paragraph (contains multiple chapter prefixes).
    if ($t.StartsWith($p1) -and $t.Contains($p2) -and $t.Contains($p3) -and $t.Contains($p6)) {
      $p.Range.Style = -1
      $p.OutlineLevel = 10
      continue
    }

    # Delete garbage paragraph that concatenates different chapters.
    if ($t.StartsWith($p3) -and $t.Contains($p2) -and ($t -notmatch "^\d+\.\d+")) {
      $p.Range.Delete()
      $i--
      continue
    }

    # Split merged chapter headings into standalone paragraphs.
    if (SplitPrefixToNewPara $doc $p $ch1 1 $null $null) { continue }
    if (SplitPrefixToNewPara $doc $p $ch2 1 "2.1" "2.2") { continue }
    if (SplitPrefixToNewPara $doc $p $ch3 1 "3.1" "3.2") { continue }

    # Remove accidental chapter4 prefix from normal paragraphs.
    if ($t.StartsWith($ch4) -and ($t -notmatch "^\d+\.\d+")) {
      $p.Range.Text = (Clean $t.Substring($ch4.Length))
      $p.Range.Style = -1
      $p.OutlineLevel = 10
      continue
    }

    # Remove accidental chapter6 prefix from section headings (e.g., "第六章...3.2 ...").
    if ($t.StartsWith($ch6) -and ($t -match "^[^0-9]*3\.\d+\b")) {
      $idx = $t.IndexOf("3.")
      if ($idx -ge 0) {
        $p.Range.Text = (Clean $t.Substring($idx))
        $p.Range.Style = -3
        $p.OutlineLevel = 2
      }
      continue
    }
  }

  $doc.Save()
} finally {
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}

