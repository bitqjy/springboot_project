var Files = Java.type("java.nio.file.Files");
var Paths = Java.type("java.nio.file.Paths");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
var ZipEntry = Java.type("java.util.zip.ZipEntry");
var ZipOutputStream = Java.type("java.util.zip.ZipOutputStream");
var FileOutputStream = Java.type("java.io.FileOutputStream");
var OffsetDateTime = Java.type("java.time.OffsetDateTime");

var inputPath = Paths.get(arguments.length > 0 ? String(arguments[0]) : "testdata/import/20260422_dayi_demo1_scores.csv");
var outputPath = Paths.get(arguments.length > 1 ? String(arguments[1]) : "testdata/import/20260422_dayi_demo1_scores.xlsx");

function xmlEscape(text) {
  return String(text)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\"/g, "&quot;")
    .replace(/'/g, "&apos;");
}

function parseCsvLine(line) {
  var result = [];
  if (line == null) {
    return result;
  }
  var sb = "";
  var inQuotes = false;
  for (var i = 0; i < line.length; i++) {
    var ch = line.charAt(i);
    if (ch === "\"") {
      if (inQuotes && i + 1 < line.length && line.charAt(i + 1) === "\"") {
        sb += "\"";
        i++;
      } else {
        inQuotes = !inQuotes;
      }
    } else if (ch === "," && !inQuotes) {
      result.push(sb);
      sb = "";
    } else {
      sb += ch;
    }
  }
  result.push(sb);
  return result;
}

function columnName(index) {
  var n = index + 1;
  var name = "";
  while (n > 0) {
    var rem = (n - 1) % 26;
    name = String.fromCharCode(65 + rem) + name;
    n = Math.floor((n - 1) / 26);
  }
  return name;
}

function isNumeric(value) {
  return /^-?\d+(\.\d+)?$/.test(String(value));
}

function buildCell(ref, value) {
  if (value == null || String(value).length === 0) {
    return "";
  }
  if (isNumeric(value)) {
    return '<c r="' + ref + '"><v>' + xmlEscape(value) + '</v></c>';
  }
  return '<c r="' + ref + '" t="inlineStr"><is><t>' + xmlEscape(value) + '</t></is></c>';
}

function buildSheetXml(rows) {
  var sheetRows = [];
  for (var r = 0; r < rows.length; r++) {
    var cells = [];
    for (var c = 0; c < rows[r].length; c++) {
      var ref = columnName(c) + (r + 1);
      var cellXml = buildCell(ref, rows[r][c]);
      if (cellXml.length > 0) {
        cells.push(cellXml);
      }
    }
    sheetRows.push('<row r="' + (r + 1) + '">' + cells.join("") + "</row>");
  }
  var maxCol = columnName(rows[0].length - 1);
  var dimension = "A1:" + maxCol + rows.length;
  return [
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>',
    '<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">',
    '<dimension ref="' + dimension + '"/>',
    '<sheetViews><sheetView workbookViewId="0"/></sheetViews>',
    '<sheetFormatPr defaultRowHeight="15"/>',
    "<sheetData>",
    sheetRows.join(""),
    "</sheetData>",
    "</worksheet>"
  ].join("");
}

function addEntry(zip, name, content) {
  zip.putNextEntry(new ZipEntry(name));
  zip.write(new java.lang.String(content).getBytes(StandardCharsets.UTF_8));
  zip.closeEntry();
}

var csvText = new java.lang.String(Files.readAllBytes(inputPath), StandardCharsets.UTF_8);
var lines = csvText.replace(/\r\n/g, "\n").replace(/\r/g, "\n").split("\n");
var rows = [];
for (var i = 0; i < lines.length; i++) {
  if (lines[i].length === 0) {
    continue;
  }
  rows.push(parseCsvLine(lines[i]));
}

if (rows.length === 0) {
  throw new Error("CSV内容为空，无法生成Excel");
}

var now = OffsetDateTime.now().toString();
var sheetXml = buildSheetXml(rows);

var contentTypesXml =
  '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">' +
  '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>' +
  '<Default Extension="xml" ContentType="application/xml"/>' +
  '<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>' +
  '<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>' +
  '<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>' +
  '<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>' +
  '<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>' +
  "</Types>";

var relsXml =
  '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">' +
  '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>' +
  '<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>' +
  '<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>' +
  "</Relationships>";

var appXml =
  '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">' +
  "<Application>Codex</Application>" +
  "<HeadingPairs><vt:vector size=\"2\" baseType=\"variant\"><vt:variant><vt:lpstr>Worksheets</vt:lpstr></vt:variant><vt:variant><vt:i4>1</vt:i4></vt:variant></vt:vector></HeadingPairs>" +
  "<TitlesOfParts><vt:vector size=\"1\" baseType=\"lpstr\"><vt:lpstr>成绩导入</vt:lpstr></vt:vector></TitlesOfParts>" +
  "</Properties>";

var coreXml =
  '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">' +
  "<dc:creator>Codex</dc:creator>" +
  "<cp:lastModifiedBy>Codex</cp:lastModifiedBy>" +
  '<dcterms:created xsi:type="dcterms:W3CDTF">' + xmlEscape(now) + "</dcterms:created>" +
  '<dcterms:modified xsi:type="dcterms:W3CDTF">' + xmlEscape(now) + "</dcterms:modified>" +
  "</cp:coreProperties>";

var workbookXml =
  '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">' +
  '<sheets><sheet name="成绩导入" sheetId="1" r:id="rId1"/></sheets>' +
  "</workbook>";

var workbookRelsXml =
  '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">' +
  '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>' +
  '<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>' +
  "</Relationships>";

var stylesXml =
  '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
  '<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">' +
  '<fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>' +
  '<fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills>' +
  '<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>' +
  '<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>' +
  '<cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>' +
  '<cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>' +
  "</styleSheet>";

var zip = new ZipOutputStream(new FileOutputStream(outputPath.toFile()));
addEntry(zip, "[Content_Types].xml", contentTypesXml);
addEntry(zip, "_rels/.rels", relsXml);
addEntry(zip, "docProps/app.xml", appXml);
addEntry(zip, "docProps/core.xml", coreXml);
addEntry(zip, "xl/workbook.xml", workbookXml);
addEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelsXml);
addEntry(zip, "xl/styles.xml", stylesXml);
addEntry(zip, "xl/worksheets/sheet1.xml", sheetXml);
zip.close();

print("created=" + outputPath.toString());
