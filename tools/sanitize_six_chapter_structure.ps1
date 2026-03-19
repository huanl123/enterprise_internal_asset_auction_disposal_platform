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

function InsertAfterPara($doc, $para, [string]$text) {
  $r = $para.Range.Duplicate
  $r.Collapse(0) | Out-Null
  $next = $doc.Paragraphs.Add($r)
  $next.Range.Text = $text
  $next
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

function ApplyStyleByPrefix($p, $chDi, $chZhang, [string]$concTitle) {
  $t = Clean $p.Range.Text
  if (-not $t) { SetNormal $p; return }
  if ($t -eq $concTitle) { SetHeading $p 1; return }
  $head = $t
  if ($head.Length -gt 12) { $head = $head.Substring(0, 12) }
  if ($t.StartsWith([string]$chDi) -and $head.Contains([string]$chZhang)) { SetHeading $p 1; return }
  if ($t -match '^\d+\.\d+\.\d+\b') { SetHeading $p 3; return }
  if ($t -match '^\d+\.\d+\b') { SetHeading $p 2; return }
  SetNormal $p
}

# Base64 titles we may insert or match
$T_CH2 = "LHuMTuB6IAAgAPt83374dnNRgGIvZw==" # Chapter 2 title
$T_CH6 = "LHttUeB6IAAgAPt8335LbdWL"          # Chapter 6 title
$T_CH1 = "LHsATuB6IAAgAOp+uos="              # Chapter 1 title
$T_CONC = "034gACAAuos="                    # Conclusion title
$T_TOC = "7nYgACAAVV8="                     # TOC title

$T_61 = "NgAuADEAIAAgAEtt1Yuvc4NY"          # 6.1
$T_62 = "NgAuADIAIAAgAEtt1YuFUblbjFTTfpxn"  # 6.2
$T_621= "NgAuADIALgAxACAAIAAnYP2ADk6JW2hRS23Viw==" # 6.2.1
$T_622= "NgAuADIALgAyACAAIABLbdWL036cZwZSkGc="     # 6.2.2
$T_63 = "NgAuADMAIAAgACxn4HoPXNN+"          # 6.3

$paperFull = (Resolve-Path -LiteralPath $PaperPath).Path
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$doc = $word.Documents.Open($paperFull, $false, $false)

try {
  $chDi = [char]0x7B2C
  $chZhang = [char]0x7AE0
  $concTitle = (S $T_CONC)
  $stage = "init"

  # Chapter markers (only used for locating the body start and TOC anchors).
  $nums = @([char]0x4E00, [char]0x4E8C, [char]0x4E09, [char]0x56DB, [char]0x4E94, [char]0x516D)
  $chapterMarkers = @()
  foreach ($n in $nums) { $chapterMarkers += ([string]$chDi + [string]$n + [string]$chZhang) }

  # Avoid touching the TOC field by starting from the *body* Chapter 1 heading.
  # Use the last occurrence to avoid TOC entries.
  $ch1Full = (S $T_CH1)
  $iFirstChapter = 0
  for ($scan = $doc.Paragraphs.Count; $scan -ge 1; $scan--) {
    $pp = $doc.Paragraphs.Item($scan)
    if (IsToc $pp) { continue }
    $tt = Clean $pp.Range.Text
    if (-not $tt) { continue }
    if ($tt.StartsWith($ch1Full)) { $iFirstChapter = $scan; break }
  }
  if ($iFirstChapter -eq 0) { $iFirstChapter = FindFirstText $doc $chapterMarkers[0] 1 }
  if ($iFirstChapter -eq 0) { $iFirstChapter = 1 }

  # Numeric heading markers: require whitespace right after marker to avoid version numbers like "3.2xxx".
  $reNum = [regex]'(?<!\d)([1-6]\.\d+(?:\.\d+)?)(?=\s)'

  $stage = "split"
  $i = $iFirstChapter
  while ($i -le $doc.Paragraphs.Count) {
    try {
      $p = $doc.Paragraphs.Item($i)
      if (IsToc $p) { $i++; continue }
      try { if ($p.Range.Fields.Count -gt 0) { $i++; continue } } catch {}
      $t = Clean $p.Range.Text
      if (-not $t) { $i++; continue }

      $idxs = New-Object System.Collections.Generic.List[int]
      foreach ($m in $reNum.Matches($t)) { $idxs.Add($m.Index) }

      $uniq = @($idxs | Sort-Object -Unique)
      if ($uniq.Count -ge 2) {
        $pieces = @()
        for ($k = 0; $k -lt $uniq.Count; $k++) {
          $start = [int]$uniq[$k]
          $end = $t.Length
          if ($k -lt ($uniq.Count - 1)) { $end = [int]$uniq[$k + 1] }
          if ($end -gt $start) {
            $seg = $t.Substring($start, $end - $start).Trim()
            if ($seg) { $pieces += $seg }
          }
        }
        if ($pieces.Count -ge 2) {
          $p.Range.Text = $pieces[0]
          ApplyStyleByPrefix $p $chDi $chZhang $concTitle
          $cur = $p
          for ($k = 1; $k -lt $pieces.Count; $k++) {
            $np = InsertAfterPara $doc $cur $pieces[$k]
            ApplyStyleByPrefix $np $chDi $chZhang $concTitle
            $cur = $np
          }
          $i += $pieces.Count
          continue
        }
      }

      ApplyStyleByPrefix $p $chDi $chZhang $concTitle
      $i++
    } catch {
      throw "Stage=$stage Para=${i}: $($_.Exception.Message)"
    }
  }

  # Ensure Chapter 2 heading exists before the first "2.1".
  $stage = "ensure_ch2"
  $i21 = 0
  for ($j = 1; $j -le $doc.Paragraphs.Count; $j++) {
    $p = $doc.Paragraphs.Item($j)
    if (IsToc $p) { continue }
    $t = Clean $p.Range.Text
    if ($t -match '^2\.1\b') { $i21 = $j; break }
  }
  if ($i21 -ne 0) {
    $need = $true
    for ($j = $i21 - 1; $j -ge 1; $j--) {
      $p = $doc.Paragraphs.Item($j)
      if (IsToc $p) { continue }
      $t = Clean $p.Range.Text
      if (-not $t) { continue }
      if ($p.OutlineLevel -eq 1) {
        if ($t.StartsWith([string]$chDi + [string][char]0x4E8C + [string]$chZhang)) { $need = $false }
        break
      }
    }
    if ($need) {
      $p21 = $doc.Paragraphs.Item($i21)
      $r = $p21.Range.Duplicate
      $r.Collapse(1) | Out-Null
      $pCh2 = $doc.Paragraphs.Add($r)
      $pCh2.Range.Text = (S $T_CH2)
      SetHeading $pCh2 1
    }
  }

  # Remove stray 6.1/6.2 headings that appear before the actual Chapter 6 heading.
  $stage = "dedupe_test_before_ch6"
  $iCh6 = FindFirstText $doc (S $T_CH6) 1
  $stage = "renumber_tests"
  if ($iCh6 -ne 0) {
    for ($j = $iCh6 - 1; $j -ge 1; $j--) {
      $p = $doc.Paragraphs.Item($j)
      if (IsToc $p) { continue }
      $t = Clean $p.Range.Text
      if ($t -match '^6\.[12]\b') { $p.Range.Delete() | Out-Null }
    }
  }

  # Under Chapter 6, renumber test headings that still start with 5.x.
  if ($iCh6 -ne 0) {
    for ($j = $iCh6 + 1; $j -le $doc.Paragraphs.Count; $j++) {
      $p = $doc.Paragraphs.Item($j)
      if (IsToc $p) { continue }
      $t = Clean $p.Range.Text
      if (-not $t) { continue }
      if ($t -match '^5\.1\b') { $p.Range.Text = (S $T_61); SetHeading $p 2; continue }
      if ($t -match '^5\.2\b') { $p.Range.Text = (S $T_62); SetHeading $p 2; continue }
      if ($t -match '^5\.3\b') { $p.Range.Text = (S $T_621); SetHeading $p 3; continue }
      if ($t -match '^5\.4\b') { $p.Range.Text = (S $T_622); SetHeading $p 3; continue }
    }
  }

  # Convert any paragraph that starts with "6.3" and contains the "conclusion" keyword into a standalone conclusion heading.
  $kwConc = [string][char]0x7ED3 + [string][char]0x8BBA
  $stage = "conclusion"
  $i63 = FindFirstRegex $doc '^6\.3\b' 1
  if ($i63 -ne 0) {
    $p = $doc.Paragraphs.Item($i63)
    $t = Clean $p.Range.Text
    if ($t.Contains($kwConc) -and $t.Length -gt 20) {
      $idx = $t.IndexOf($kwConc)
      $body = ""
      if ($idx -ge 0) { $body = $t.Substring($idx).Trim() }
      $p.Range.Text = (S $T_CONC)
      SetHeading $p 1
      if ($body) { $np = InsertAfterPara $doc $p $body; SetNormal $np }
    }
  }

  # Rebuild TOC (levels 1-2): delete old TOC body between TOC title and first chapter heading, then insert.
  # TOC rebuilding is intentionally skipped here because some documents can have a locked TOC Range.
  # Once headings are clean, you can rebuild the TOC separately (or manually in Word).

  $doc.Save()
} catch {
  throw "Stage=${stage}: $($_.Exception.Message)"
} finally {
  try { $doc.Close($true) | Out-Null } catch {}
  try { $word.Quit() | Out-Null } catch {}
}
