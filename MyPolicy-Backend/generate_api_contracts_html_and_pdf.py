from pathlib import Path

import markdown
from weasyprint import HTML, CSS


def main() -> None:
  root = Path(__file__).parent
  md_path = root / "API_CONTRACTS.md"
  html_path = root / "API_CONTRACTS.html"
  pdf_path = root / "API_CONTRACTS_styled.pdf"

  text = md_path.read_text(encoding="utf-8")

  # Convert markdown to HTML
  body_html = markdown.markdown(
    text,
    extensions=["fenced_code", "tables", "toc"],
  )

  # Wrap with a basic HTML template and some professional CSS.
  html_full = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <title>MyPolicy – API Contracts</title>
  <style>
    @page {{
      size: A4;
      margin: 20mm 18mm 20mm 18mm;
    }}
    body {{
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      font-size: 11pt;
      color: #1a202c;
      line-height: 1.4;
    }}
    h1, h2, h3, h4 {{
      font-weight: 700;
      color: #0f4c81;
      margin-top: 18px;
      margin-bottom: 8px;
    }}
    h1 {{
      font-size: 20pt;
      border-bottom: 2px solid #0f4c81;
      padding-bottom: 4px;
    }}
    h2 {{
      font-size: 16pt;
    }}
    h3 {{
      font-size: 13pt;
    }}
    p {{
      margin: 4px 0;
    }}
    ul, ol {{
      margin: 4px 0 4px 22px;
    }}
    code, pre {{
      font-family: "Fira Code", "Source Code Pro", Menlo, Consolas, monospace;
      font-size: 9pt;
    }}
    pre {{
      background: #f7fafc;
      border-radius: 4px;
      padding: 6px 8px;
      border: 1px solid #e2e8f0;
      overflow-x: auto;
      margin: 6px 0 8px 0;
    }}
    blockquote {{
      border-left: 3px solid #0f4c81;
      padding-left: 10px;
      color: #4a5568;
      margin: 6px 0;
    }}
    table {{
      border-collapse: collapse;
      margin: 8px 0;
      width: 100%;
      font-size: 10pt;
    }}
    th, td {{
      border: 1px solid #e2e8f0;
      padding: 4px 6px;
      text-align: left;
    }}
    th {{
      background: #edf2f7;
      font-weight: 600;
    }}
    header {{
      text-align: right;
      font-size: 9pt;
      color: #718096;
      border-bottom: 1px solid #e2e8f0;
      margin-bottom: 8px;
      padding-bottom: 2px;
    }}
    footer {{
      position: fixed;
      bottom: 6mm;
      left: 0;
      right: 0;
      text-align: center;
      font-size: 8pt;
      color: #a0aec0;
    }}
  </style>
</head>
<body>
  <header>MyPolicy – API Contracts</header>
  {body_html}
  <footer>Generated API Reference · MyPolicy</footer>
</body>
</html>
"""

  html_path.write_text(html_full, encoding="utf-8")

  HTML(string=html_full).write_pdf(str(pdf_path), stylesheets=[CSS(string="")])
  print(f"Generated HTML: {html_path}")
  print(f"Generated styled PDF: {pdf_path}")


if __name__ == "__main__":
  main()

