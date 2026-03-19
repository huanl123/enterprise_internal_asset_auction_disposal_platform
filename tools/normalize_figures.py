# -*- coding: utf-8 -*-
from __future__ import annotations

import argparse
import datetime as _dt
import os
import re
import shutil
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List, Optional, Tuple

from lxml import etree
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.graphics import renderPDF, renderPM
from svglib.svglib import svg2rlg
from svglib.fonts import FontMap as SvglibFontMap


SVG_NS = "http://www.w3.org/2000/svg"
NSMAP = {"svg": SVG_NS}

TARGET_FONT_SIZE = "10.5pt"  # 五号
FONT_CJK = 'SimSun, "宋体"'
FONT_LATIN = '"Times New Roman"'

STROKE_MAIN = 1.6
STROKE_CONN = 1.2
STROKE_AUX = 0.8
STROKE_BUCKETS = (STROKE_AUX, STROKE_CONN, STROKE_MAIN)

DASH_PATTERN = "6 4"


@dataclass(frozen=True)
class ExportResult:
    src_svg: Path
    normalized_svg: Path
    out_pdf: Path
    out_png: Path
    warnings: Tuple[str, ...] = ()


def _quantize_stroke(value: float) -> float:
    return min(STROKE_BUCKETS, key=lambda v: abs(v - value))


_FONT_SHORTHAND_RE = re.compile(r"font\s*:\s*([^;]+);", re.IGNORECASE)
_FONT_WEIGHT_NUM_RE = re.compile(r"font-weight\s*:\s*([0-9]{3})\s*;", re.IGNORECASE)
_STROKE_WIDTH_RE = re.compile(r"stroke-width\s*:\s*([0-9.]+)\s*;?", re.IGNORECASE)
_DASH_RE = re.compile(r"stroke-dasharray\s*:\s*([^;]+);", re.IGNORECASE)
_CLASS_STROKE_RE_TEMPLATE = r"(\.{cls}\s*\{{[^}}]*?stroke-width\s*:\s*)([0-9.]+)"


def _extract_weight_from_font_shorthand(value: str) -> str:
    # Expected like: "700 28px 'Microsoft YaHei', ...", or "bold 14px ..."
    first = (value.strip().split() or ["400"])[0]
    if re.fullmatch(r"[0-9]{3}", first):
        w = int(first)
        return "bold" if w >= 600 else "normal"
    if first.lower() in {"bold", "normal", "bolder", "lighter"}:
        return first.lower()
    return "normal"


def _rewrite_css(style_text: str) -> str:
    def repl_font(m: re.Match[str]) -> str:
        weight = _extract_weight_from_font_shorthand(m.group(1))
        return f"font-weight: {weight};"

    style_text = _FONT_SHORTHAND_RE.sub(repl_font, style_text)

    def repl_weight_num(m: re.Match[str]) -> str:
        w = int(m.group(1))
        return "font-weight: bold;" if w >= 600 else "font-weight: normal;"

    # Normalize previously-written numeric font-weight values to bold/normal so
    # svglib's FontMap can resolve weight variants.
    style_text = _FONT_WEIGHT_NUM_RE.sub(repl_weight_num, style_text)

    def repl_dash(m: re.Match[str]) -> str:
        return f"stroke-dasharray: {DASH_PATTERN};"

    # Only normalize explicit dash definitions (e.g., .dash)
    style_text = _DASH_RE.sub(repl_dash, style_text)

    # Global stroke-width quantization (keeps hierarchy, enforces unified buckets)
    def repl_stroke(m: re.Match[str]) -> str:
        try:
            v = float(m.group(1))
        except ValueError:
            return m.group(0)
        q = _quantize_stroke(v)
        return f"stroke-width: {q};"

    style_text = _STROKE_WIDTH_RE.sub(repl_stroke, style_text)

    # Enforce explicit hierarchy by common class names (overrides quantization).
    def set_class_stroke(class_name: str, width: float) -> None:
        nonlocal style_text
        pat = re.compile(_CLASS_STROKE_RE_TEMPLATE.format(cls=re.escape(class_name)), re.IGNORECASE | re.DOTALL)
        style_text = pat.sub(rf"\g<1>{width}", style_text)

    # Connector lines
    for cls in ("line",):
        set_class_stroke(cls, STROKE_CONN)
    # Auxiliary strokes (panels, background lanes, headers)
    for cls in ("panel", "chip", "hdr", "head", "layer", "accent", "lane", "laneHeader"):
        set_class_stroke(cls, STROKE_AUX)
    # Main outlines (boxes, tables, actions, diamonds, use cases)
    for cls in ("box", "tbl", "module", "action", "diamond", "usecase"):
        set_class_stroke(cls, STROKE_MAIN)

    return style_text


def _is_cjk_char(ch: str) -> bool:
    cp = ord(ch)
    # CJK Unified Ideographs + extensions + common punctuation / fullwidth
    return (
        0x4E00 <= cp <= 0x9FFF
        or 0x3400 <= cp <= 0x4DBF
        or 0x20000 <= cp <= 0x2A6DF
        or 0x2A700 <= cp <= 0x2B73F
        or 0x2B740 <= cp <= 0x2B81F
        or 0x2B820 <= cp <= 0x2CEAF
        or 0xF900 <= cp <= 0xFAFF
        or 0x3000 <= cp <= 0x303F
        or 0xFF00 <= cp <= 0xFFEF
    )


def _split_runs(text: str) -> List[Tuple[str, str]]:
    # Returns list of (run_type, run_text) where run_type in {"cjk","latin"}
    # Heuristic: treat non-CJK as latin (digits/ASCII punctuation/Latin letters/most symbols).
    runs: List[Tuple[str, str]] = []
    if not text:
        return runs
    cur_type = "cjk" if _is_cjk_char(text[0]) else "latin"
    cur = [text[0]]
    for ch in text[1:]:
        t = "cjk" if _is_cjk_char(ch) else "latin"
        if t == cur_type:
            cur.append(ch)
        else:
            runs.append((cur_type, "".join(cur)))
            cur_type = t
            cur = [ch]
    runs.append((cur_type, "".join(cur)))
    return runs


def _merge_style(existing: Optional[str], additions: Iterable[str]) -> str:
    add = "; ".join(a.strip().rstrip(";") for a in additions if a.strip())
    if not existing:
        return add + ";"
    existing_clean = existing.strip()
    if not existing_clean.endswith(";"):
        existing_clean += ";"
    return existing_clean + " " + add + ";"


def normalize_svg(svg_path: Path, *, backup_dir: Optional[Path] = None) -> Tuple[Path, Tuple[str, ...]]:
    warnings: List[str] = []
    if backup_dir is not None:
        backup_dir.mkdir(parents=True, exist_ok=True)
        backup_target = backup_dir / svg_path.name
        if not backup_target.exists():
            shutil.copy2(svg_path, backup_target)

    parser = etree.XMLParser(remove_blank_text=False, recover=True)
    tree = etree.parse(str(svg_path), parser)
    root = tree.getroot()

    # Rewrite <style> blocks
    for style_el in root.xpath(".//svg:style", namespaces=NSMAP):
        if style_el.text:
            style_el.text = _rewrite_css(style_el.text)

    # Normalize stroke-width attributes (markers, etc.)
    for el in root.xpath(".//*[@stroke-width]", namespaces=NSMAP):
        raw = el.get("stroke-width")
        try:
            v = float(raw)
        except (TypeError, ValueError):
            continue
        el.set("stroke-width", f"{_quantize_stroke(v)}")

    # Normalize dash pattern attributes if present
    for el in root.xpath(".//*[@stroke-dasharray]", namespaces=NSMAP):
        el.set("stroke-dasharray", DASH_PATTERN)

    # Ensure every <text> uses SimSun + 五号; split Latin runs into <tspan> with Times New Roman.
    for text_el in root.xpath(".//svg:text", namespaces=NSMAP):
        text_el.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
        # Set as presentation attributes (not inline CSS) so svglib reliably sees them.
        text_el.attrib.pop("style", None)
        text_el.set("font-size", TARGET_FONT_SIZE)
        text_el.set("font-family", "SimSun")

        # If the <text> already contains tspans/children, try to rebuild into
        # our standardized run-based tspans when safe (no per-span positioning).
        if len(text_el) != 0:
            unsafe = False
            for el in text_el.xpath(".//svg:tspan", namespaces=NSMAP):
                if any(el.get(a) is not None for a in ("x", "y", "dx", "dy", "rotate")):
                    unsafe = True
                    break
            if not unsafe and all(
                (child.tag == f"{{{SVG_NS}}}tspan") for child in list(text_el)
            ):
                raw_text = "".join(text_el.itertext())
                for child in list(text_el):
                    text_el.remove(child)
                text_el.text = raw_text
            else:
                # Fallback: just make sure spans have size/family, don't restructure.
                for child in text_el.xpath(".//svg:tspan", namespaces=NSMAP):
                    child.attrib.pop("style", None)
                    if not child.get("font-size"):
                        child.set("font-size", TARGET_FONT_SIZE)
                    ff = child.get("font-family")
                    if not ff:
                        child.set("font-family", "SimSun")
                    elif ff == "Times New Roman":
                        child.set("font-family", "\"Times New Roman\"")
                continue

        raw_text = text_el.text or ""
        if not raw_text.strip():
            continue

        runs = _split_runs(raw_text)
        if all(t == "cjk" for t, _ in runs) or all(t == "latin" for t, _ in runs):
            # Still enforce latin-only font if entire string is latin.
            if all(t == "latin" for t, _ in runs):
                # Quote multi-word family so svglib doesn't split it.
                text_el.set("font-family", "\"Times New Roman\"")
            continue

        # Replace text with tspans
        text_el.text = None
        for run_type, run_text in runs:
            tspan = etree.SubElement(text_el, f"{{{SVG_NS}}}tspan")
            if run_type == "latin":
                tspan.set("font-size", TARGET_FONT_SIZE)
                tspan.set("font-family", "\"Times New Roman\"")
            else:
                tspan.set("font-size", TARGET_FONT_SIZE)
                tspan.set("font-family", "SimSun")
            tspan.text = run_text

    tree.write(str(svg_path), encoding="UTF-8", xml_declaration=True, pretty_print=False)
    return svg_path, tuple(warnings)


def _register_fonts_and_build_fontmap() -> SvglibFontMap:
    font_dir = Path(os.environ.get("WINDIR", r"C:\WINDOWS")) / "Fonts"
    times = {
        ("normal", "normal"): font_dir / "times.ttf",
        ("bold", "normal"): font_dir / "timesbd.ttf",
        ("normal", "italic"): font_dir / "timesi.ttf",
        ("bold", "italic"): font_dir / "timesbi.ttf",
    }
    simsun_regular = font_dir / "simsun.ttc"
    simsun_bold = font_dir / "simsunb.ttf"

    for _, p in times.items():
        if not p.exists():
            raise FileNotFoundError(f"Missing font file: {p}")
    if not simsun_regular.exists():
        raise FileNotFoundError(f"Missing font file: {simsun_regular}")
    if not simsun_bold.exists():
        # Bold isn't strictly required; keep going if missing.
        simsun_bold = None

    fm = SvglibFontMap()
    # Register fonts through svglib's FontMap so it can resolve them as exact matches.
    fm.register_font("Times New Roman", str(times[("normal", "normal")]), weight="normal", style="normal", rlgFontName="Times New Roman")
    fm.register_font("Times New Roman", str(times[("bold", "normal")]), weight="bold", style="normal", rlgFontName="Times New Roman Bold")
    fm.register_font("Times New Roman", str(times[("normal", "italic")]), weight="normal", style="italic", rlgFontName="Times New Roman Italic")
    fm.register_font("Times New Roman", str(times[("bold", "italic")]), weight="bold", style="italic", rlgFontName="Times New Roman Bold Italic")

    fm.register_font("SimSun", str(simsun_regular), weight="normal", style="normal", rlgFontName="SimSun")
    if simsun_bold is not None:
        fm.register_font("SimSun", str(simsun_bold), weight="bold", style="normal", rlgFontName="SimSun Bold")
    # Common alias used in our SVG font stacks.
    fm.register_font("宋体", str(simsun_regular), weight="normal", style="normal", rlgFontName="SimSun")
    if simsun_bold is not None:
        fm.register_font("宋体", str(simsun_bold), weight="bold", style="normal", rlgFontName="SimSun Bold")

    return fm


def export_svg(
    svg_path: Path, *, out_pdf: Path, out_png: Path, dpi: int, font_map: SvglibFontMap
) -> Tuple[Path, Path]:
    drawing = svg2rlg(str(svg_path), font_map=font_map)
    if drawing is None:
        raise RuntimeError(f"svglib failed to parse: {svg_path}")
    out_pdf.parent.mkdir(parents=True, exist_ok=True)
    out_png.parent.mkdir(parents=True, exist_ok=True)
    renderPDF.drawToFile(drawing, str(out_pdf))
    renderPM.drawToFile(drawing, str(out_png), fmt="PNG", dpi=dpi)
    return out_pdf, out_png


def write_report(results: List[ExportResult], report_path: Path) -> None:
    report_path.parent.mkdir(parents=True, exist_ok=True)
    lines: List[str] = []
    lines.append(f"# 插图批处理报告\n")
    lines.append(f"- 生成时间：{_dt.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
    lines.append(f"- 字体：中文 `{FONT_CJK}`（五号 {TARGET_FONT_SIZE}），外文/数字 `{FONT_LATIN}`\n")
    lines.append(f"- 线宽：主 {STROKE_MAIN} / 连线 {STROKE_CONN} / 辅助 {STROKE_AUX}（统一量化）\n")
    lines.append(f"- 虚线：`{DASH_PATTERN}`\n")
    lines.append(f"\n## 文件\n")
    for r in results:
        lines.append(f"- {r.src_svg.name}\n")
        lines.append(f"  - SVG：{r.normalized_svg.as_posix()}\n")
        lines.append(f"  - PDF：{r.out_pdf.as_posix()}\n")
        lines.append(f"  - PNG：{r.out_png.as_posix()}\n")
        if r.warnings:
            for w in r.warnings:
                lines.append(f"  - 警告：{w}\n")
    report_path.write_text("".join(lines), encoding="utf-8")


def main() -> int:
    ap = argparse.ArgumentParser(description="Normalize SVG figures to thesis style and export PDF/PNG.")
    ap.add_argument(
        "--dir",
        dest="dir_path",
        default=str(Path("images") / "毕设插图"),
        help="Input directory containing SVG files (default: images/毕设插图)",
    )
    ap.add_argument("--dpi", type=int, default=300, help="PNG export DPI (default: 300)")
    args = ap.parse_args()

    in_dir = Path(args.dir_path)
    if not in_dir.exists():
        raise SystemExit(f"Input dir not found: {in_dir}")

    export_root = in_dir / "_export"
    out_pdf_dir = export_root / "pdf"
    out_png_dir = export_root / "png"
    backup_dir = export_root / "backup_svg_original"

    svglib_font_map = _register_fonts_and_build_fontmap()

    results: List[ExportResult] = []
    for svg in sorted(in_dir.glob("*.svg")):
        normalized, warnings = normalize_svg(svg, backup_dir=backup_dir)
        pdf_path = out_pdf_dir / (svg.stem + ".pdf")
        png_path = out_png_dir / (svg.stem + ".png")
        export_svg(
            normalized,
            out_pdf=pdf_path,
            out_png=png_path,
            dpi=args.dpi,
            font_map=svglib_font_map,
        )
        results.append(
            ExportResult(
                src_svg=svg,
                normalized_svg=normalized,
                out_pdf=pdf_path,
                out_png=png_path,
                warnings=warnings,
            )
        )

    write_report(results, export_root / "report.md")
    print(f"Done. Exported {len(results)} figures to {export_root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
