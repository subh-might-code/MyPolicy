from pathlib import Path

from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer
from reportlab.lib.enums import TA_LEFT
from reportlab.lib import colors


def main() -> None:
  root = Path(__file__).parent
  md_path = root / "API_CONTRACTS.md"
  pdf_path = root / "API_CONTRACTS.pdf"

  text = md_path.read_text(encoding="utf-8")
  lines = text.splitlines()

  styles = getSampleStyleSheet()
  title_style = styles["Title"]
  h1_style = styles["Heading1"]
  h2_style = styles["Heading2"]
  body_style = styles["BodyText"]
  code_style = styles["Code"]
  for s in (title_style, h1_style, h2_style, body_style, code_style):
    s.alignment = TA_LEFT

  story = []
  in_code_block = False
  code_buffer: list[str] = []

  def flush_code():
    nonlocal code_buffer
    if not code_buffer:
      return
    code_text = "<br/>".join(
      line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
      for line in code_buffer
    )
    story.append(Paragraph(f"<font face='Courier'>{code_text}</font>", code_style))
    story.append(Spacer(1, 4 * mm))
    code_buffer = []

  for line in lines:
    stripped = line.strip("\n")

    if stripped.startswith("```"):
      if in_code_block:
        flush_code()
        in_code_block = False
      else:
        in_code_block = True
      continue

    if in_code_block:
      code_buffer.append(stripped)
      continue

    if not stripped:
      story.append(Spacer(1, 3 * mm))
      continue

    if stripped.startswith("# "):
      text_html = stripped[2:].strip()
      story.append(Paragraph(text_html, title_style))
      story.append(Spacer(1, 4 * mm))
    elif stripped.startswith("## "):
      text_html = stripped[3:].strip()
      story.append(Paragraph(text_html, h1_style))
      story.append(Spacer(1, 3 * mm))
    elif stripped.startswith("### "):
      text_html = stripped[4:].strip()
      story.append(Paragraph(text_html, h2_style))
      story.append(Spacer(1, 2 * mm))
    elif stripped.startswith("**") and stripped.endswith("**") and len(stripped) > 4:
      # Standalone bold line
      text_html = f"<b>{stripped[2:-2]}</b>"
      story.append(Paragraph(text_html, body_style))
      story.append(Spacer(1, 1.5 * mm))
    elif stripped.startswith("- "):
      text_html = stripped[2:]
      story.append(Paragraph(f"• {text_html}", body_style))
    elif stripped.startswith("> "):
      # Blockquote
      text_html = stripped[2:]
      para = Paragraph(text_html, body_style)
      para.backColor = colors.whitesmoke
      story.append(para)
      story.append(Spacer(1, 1.5 * mm))
    else:
      story.append(Paragraph(stripped, body_style))

  flush_code()

  doc = SimpleDocTemplate(
    str(pdf_path),
    pagesize=A4,
    leftMargin=18 * mm,
    rightMargin=18 * mm,
    topMargin=20 * mm,
    bottomMargin=20 * mm,
  )

  doc.build(story)
  print(f"Generated {pdf_path}")


if __name__ == "__main__":
  main()

