import copy
import shutil
import zipfile
from pathlib import Path
from xml.etree import ElementTree as ET


DOCX_PATH = Path(r"E:\Study\code\Java\enterprise_internal_asset_auction_disposal_platform\images\2215304659 庄采润 论文.docx")
BACKUP_PATH = DOCX_PATH.with_name(DOCX_PATH.stem + ".bak_before_rewrite_1_2.docx")

W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
NS = {"w": W_NS}
ET.register_namespace("w", W_NS)


def qn(tag: str) -> str:
    prefix, name = tag.split(":")
    if prefix != "w":
        raise ValueError(tag)
    return f"{{{W_NS}}}{name}"


def para_text(p: ET.Element) -> str:
    return "".join((t.text or "") for t in p.findall(".//w:t", NS)).strip()


def first_run_props(p: ET.Element) -> ET.Element | None:
    for r in p.findall("w:r", NS):
        rpr = r.find("w:rPr", NS)
        if rpr is not None:
            return copy.deepcopy(rpr)
    return None


def build_run(text: str, base_rpr: ET.Element | None, superscript: bool = False) -> ET.Element:
    r = ET.Element(qn("w:r"))
    if base_rpr is not None:
        rpr = copy.deepcopy(base_rpr)
    else:
        rpr = ET.Element(qn("w:rPr"))
    if superscript:
        rfonts = rpr.find("w:rFonts", NS)
        if rfonts is None:
            rfonts = ET.SubElement(rpr, qn("w:rFonts"))
        rfonts.set(qn("w:ascii"), "Times New Roman")
        rfonts.set(qn("w:hAnsi"), "Times New Roman")
        rfonts.set(qn("w:cs"), "Times New Roman")

        vert = rpr.find("w:vertAlign", NS)
        if vert is None:
            vert = ET.SubElement(rpr, qn("w:vertAlign"))
        vert.set(qn("w:val"), "superscript")
    if len(rpr):
        r.append(rpr)

    t = ET.SubElement(r, qn("w:t"))
    if text.startswith(" ") or text.endswith(" "):
        t.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
    t.text = text
    return r


def build_paragraph(template_p: ET.Element, segments: list[tuple[str, bool]]) -> ET.Element:
    new_p = copy.deepcopy(template_p)
    for child in list(new_p):
        if child.tag != qn("w:pPr"):
            new_p.remove(child)

    base_rpr = first_run_props(template_p)
    for text, superscript in segments:
        if text:
            new_p.append(build_run(text, base_rpr, superscript=superscript))
    return new_p


def main() -> None:
    if not BACKUP_PATH.exists():
        shutil.copy2(DOCX_PATH, BACKUP_PATH)

    with zipfile.ZipFile(DOCX_PATH, "r") as zin:
        files = {name: zin.read(name) for name in zin.namelist()}

    root = ET.fromstring(files["word/document.xml"])
    body = root.find("w:body", NS)
    if body is None:
        raise RuntimeError("document.xml missing body")

    children = list(body)
    start_idx = end_idx = None
    for i, child in enumerate(children):
        if child.tag != qn("w:p"):
            continue
        text = para_text(child)
        if text == "1.2  国内外研究现状":
            start_idx = i
        elif text == "1.3  研究目标与主要内容" and start_idx is not None:
            end_idx = i
            break

    if start_idx is None or end_idx is None or end_idx <= start_idx + 1:
        raise RuntimeError("Could not locate section 1.2 boundaries")

    template_p = children[start_idx + 1]

    new_paragraphs = [
        [
            ("国内关于企业资产管理与相关信息系统的研究，已从单纯台账管理逐步转向流程化、信息化和协同化建设。前后端分离和 B/S 架构方面的研究表明，模块解耦、接口统一与页面交互优化能够有效提升系统的扩展性和用户体验，为中后台业务系统建设提供了成熟思路", False),
            ("[2,4,8]", True),
            ("。在此基础上，张文、李明等围绕固定资产信息管理系统展开实践，已实现资产信息录入、折旧计算、审批提醒与基础报表等功能", False),
            ("[3,14]", True),
            ("。不过，这类研究大多聚焦资产登记和日常管理，对内部拍卖、成交确认、付款审核与处置归档等后续环节覆盖不足，尚未形成面向处置业务的完整闭环。", False),
        ],
        [
            ("从管理机制和业务需求层面看，罗洁、周悦、王晗、沈菲、杨静及张伟等研究分别从固定资产管理优化、全生命周期成本采集、内控信息化、内部控制改进以及数字化技术应用等角度进行了分析", False),
            ("[6,9,11-13,15]", True),
            ("。相关成果普遍认为，资产管理系统应加强数据贯通、过程留痕和制度约束，提升审批透明度和管理效率。葛辉关于校园闲置物品交易平台的研究，则从交易流程与用户交互角度提供了参考", False),
            ("[1]", True),
            ("。但综合来看，国内现有研究虽已在资产录入、价值计算、基础交易和信息统计方面积累了一定成果，却仍存在拍卖流程自动化不足、保留价与异常处理机制不完善、角色权限粒度不够细以及多部门协同支持不足等问题。", False),
        ],
        [
            ("国外在相关领域起步较早，研究更强调资产全生命周期治理、数据驱动决策以及流程自动化。CHEN 和 LUO 基于 Spring Boot 的物业管理系统研究表明，模块化架构和实时数据更新机制能够有效提升业务处理效率与用户体验", False),
            ("[5]", True),
            ("；Pakkala 等从工业生产资产信息管理出发，强调自动化采集与协同机制对提升资产信息质量和使用效率的重要作用", False),
            ("[7]", True),
            ("；Crespo Marquez 等则从更宏观的角度总结了资产管理技术、评估方法与行业应用的发展趋势，指出信息系统集成、资产评估和流程协同是资产治理的重要方向", False),
            ("[10]", True),
            ("。这些研究为本课题提供了方法借鉴，但已有成果多服务于物业、工业设备或广义资产管理场景，直接面向企业内部废旧资产拍卖与处置全过程的一体化平台研究仍相对有限。", False),
        ],
        [
            ("从具体实现技术来看，MVC 与三层架构、Java Web 技术体系、Vue 前端框架以及 Spring Boot 开发模式已经较为成熟，能够为多角色协同业务系统提供稳定的工程基础", False),
            ("[16-20]", True),
            ("。结合开题报告和任务书中的研究目标可以看出，企业废旧资产内部拍卖与处置平台不仅需要具备一般资产管理系统的数据维护能力，还需要进一步解决自动定价与保留价控制、拍卖结果判定、超时违约处理、付款凭证审核、处置归档锁定及统计分析等专门问题。因此，面向企业内部场景构建一套兼顾流程闭环、权限控制、业务留痕和数据分析能力的平台，仍具有较强的研究价值与实践意义。", False),
        ],
    ]

    for idx in range(end_idx - 1, start_idx, -1):
        body.remove(children[idx])

    insert_at = start_idx + 1
    for paragraph_segments in new_paragraphs:
        body.insert(insert_at, build_paragraph(template_p, paragraph_segments))
        insert_at += 1

    files["word/document.xml"] = ET.tostring(root, encoding="utf-8", xml_declaration=True)

    tmp_path = DOCX_PATH.with_suffix(".tmp.docx")
    with zipfile.ZipFile(tmp_path, "w", compression=zipfile.ZIP_DEFLATED) as zout:
        for name, data in files.items():
            zout.writestr(name, data)

    shutil.move(tmp_path, DOCX_PATH)
    print(f"updated: {DOCX_PATH}")
    print(f"backup: {BACKUP_PATH}")


if __name__ == "__main__":
    main()
