import re
import shutil
from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt


PAPER = Path(r"E:\Study\code\Java\enterprise_internal_asset_auction_disposal_platform\images\2215304659 庄采润 论文.docx")
BACKUP = Path(r"E:\Study\code\Java\enterprise_internal_asset_auction_disposal_platform\images\2215304659 庄采润 论文.bak_before_batch_format_fix.docx")


CHAPTER_RE = re.compile(r"^第[一二三四五六七八九十]+章\s{1,}.*$")
SECOND_RE = re.compile(r"^\d+\.\d+\s{1,}[^.\d].*$")
THIRD_RE = re.compile(r"^\d+\.\d+\.\d+\s{0,}.*$")
TABLE_TITLE_RE = re.compile(r"^表\d+\.\d+\s{1,}.*$")
FIG_TITLE_RE = re.compile(r"^图\d+\.\d+\s{1,}.*$")
APPENDIX_RE = re.compile(r"^附录[A-ZＡ-Ｚ].*$")


def set_run_font(run, east_asia: str | None = None, ascii_font: str | None = None,
                 size: Pt | None = None, bold: bool | None = None) -> None:
    if ascii_font is not None:
        run.font.name = ascii_font
    if size is not None:
        run.font.size = size
    if bold is not None:
        run.bold = bold
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.rFonts
    if rfonts is None:
        rfonts = OxmlElement("w:rFonts")
        rpr.append(rfonts)
    if east_asia is not None:
        rfonts.set(qn("w:eastAsia"), east_asia)
    if ascii_font is not None:
        rfonts.set(qn("w:ascii"), ascii_font)
        rfonts.set(qn("w:hAnsi"), ascii_font)
        rfonts.set(qn("w:cs"), ascii_font)


def ensure_page_break_before(paragraph) -> None:
    ppr = paragraph._element.get_or_add_pPr()
    if ppr.find(qn("w:pageBreakBefore")) is None:
        pbr = OxmlElement("w:pageBreakBefore")
        pbr.set(qn("w:val"), "1")
        ppr.append(pbr)


def format_chapter_heading(paragraph) -> None:
    pf = paragraph.paragraph_format
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    pf.first_line_indent = None
    pf.left_indent = None
    pf.right_indent = None
    pf.space_before = Pt(7.5)
    pf.space_after = Pt(7.5)
    pf.line_spacing_rule = WD_LINE_SPACING.ONE_POINT_FIVE
    ensure_page_break_before(paragraph)
    for run in paragraph.runs:
        set_run_font(run, east_asia="黑体", ascii_font="Times New Roman", size=Pt(15), bold=True)


def format_special_heading(paragraph) -> None:
    pf = paragraph.paragraph_format
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    pf.first_line_indent = None
    pf.left_indent = None
    pf.right_indent = None
    pf.space_before = Pt(0)
    pf.space_after = Pt(0)
    pf.line_spacing_rule = WD_LINE_SPACING.ONE_POINT_FIVE
    ensure_page_break_before(paragraph)
    for run in paragraph.runs:
        set_run_font(run, east_asia="黑体", ascii_font="Times New Roman", size=Pt(15), bold=True)


def format_second_heading(paragraph) -> None:
    pf = paragraph.paragraph_format
    paragraph.alignment = WD_ALIGN_PARAGRAPH.LEFT
    pf.first_line_indent = None
    pf.left_indent = None
    pf.right_indent = None
    pf.space_before = Pt(0)
    pf.space_after = Pt(0)
    pf.line_spacing_rule = WD_LINE_SPACING.ONE_POINT_FIVE
    for run in paragraph.runs:
        set_run_font(run, east_asia="黑体", ascii_font="Times New Roman", size=Pt(14), bold=True)


def format_third_heading(paragraph) -> None:
    pf = paragraph.paragraph_format
    paragraph.alignment = WD_ALIGN_PARAGRAPH.LEFT
    pf.first_line_indent = None
    pf.left_indent = None
    pf.right_indent = None
    pf.space_before = Pt(0)
    pf.space_after = Pt(0)
    pf.line_spacing_rule = WD_LINE_SPACING.ONE_POINT_FIVE
    for run in paragraph.runs:
        set_run_font(run, east_asia="黑体", ascii_font="Times New Roman", size=Pt(12), bold=False)


def format_reference_entry(paragraph) -> None:
    pf = paragraph.paragraph_format
    paragraph.alignment = WD_ALIGN_PARAGRAPH.LEFT
    pf.left_indent = Cm(0.85)
    pf.first_line_indent = Cm(-0.85)
    pf.right_indent = None
    pf.space_before = Pt(0)
    pf.space_after = Pt(0)
    pf.line_spacing_rule = WD_LINE_SPACING.ONE_POINT_FIVE


def format_caption(paragraph) -> None:
    pf = paragraph.paragraph_format
    paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
    pf.left_indent = None
    pf.right_indent = None
    pf.first_line_indent = None
    pf.space_before = Pt(0)
    pf.space_after = Pt(0)
    for run in paragraph.runs:
        set_run_font(run, east_asia="黑体", ascii_font="Times New Roman", size=Pt(10.5), bold=False)


def main() -> None:
    if not BACKUP.exists():
        shutil.copy2(PAPER, BACKUP)

    doc = Document(str(PAPER))

    # Known special headings can be matched by text, with index fallback if needed.
    special_heading_texts = {"结  论", "致  谢", "参 考 文 献"}
    special_heading_fallback_indices = {408, 413, 418}

    for idx, paragraph in enumerate(doc.paragraphs):
        text = paragraph.text.strip()
        if not text:
            continue

        if CHAPTER_RE.match(text):
            format_chapter_heading(paragraph)
            continue

        if text in special_heading_texts or idx in special_heading_fallback_indices:
            format_special_heading(paragraph)
            continue

        if APPENDIX_RE.match(text):
            format_special_heading(paragraph)
            continue

        if THIRD_RE.match(text):
            format_third_heading(paragraph)
            continue

        if SECOND_RE.match(text):
            format_second_heading(paragraph)
            continue

        if re.match(r"^\[\d+\]", text):
            format_reference_entry(paragraph)
            continue

        if TABLE_TITLE_RE.match(text) or FIG_TITLE_RE.match(text):
            format_caption(paragraph)

    doc.save(str(PAPER))
    print(f"formatted: {PAPER}")
    print(f"backup: {BACKUP}")


if __name__ == "__main__":
    main()
