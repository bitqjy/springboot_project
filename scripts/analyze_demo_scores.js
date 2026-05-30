var Files = Java.type("java.nio.file.Files");
var Paths = Java.type("java.nio.file.Paths");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
var ArrayList = Java.type("java.util.ArrayList");
var LinkedHashMap = Java.type("java.util.LinkedHashMap");
var ScoreAnalyzeServiceImpl = Java.type("com.service.impl.ScoreAnalyzeServiceImpl");
var CeshichengjiEntity = Java.type("com.entity.CeshichengjiEntity");

var inputPath = Paths.get(arguments.length > 0 ? String(arguments[0]) : "testdata/import/20260422_dayi_demo1_scores.csv");
var service = new ScoreAnalyzeServiceImpl();

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

function blank(value) {
  return value == null || String(value).trim().length === 0;
}

function toDouble(value) {
  if (blank(value)) {
    return null;
  }
  return java.lang.Double.valueOf(String(value).trim());
}

function toInteger(value) {
  if (blank(value)) {
    return null;
  }
  return java.lang.Integer.valueOf(String(value).trim());
}

function increment(map, key) {
  map.put(key, map.containsKey(key) ? map.get(key) + 1 : 1);
}

function text(value) {
  return value == null ? "" : String(value);
}

function joinList(list) {
  if (list == null || list.isEmpty()) {
    return "";
  }
  var values = [];
  for (var i = 0; i < list.size(); i++) {
    values.push(String(list.get(i)));
  }
  return values.join("|");
}

function mapText(map) {
  var keys = [];
  var iterator = map.keySet().iterator();
  while (iterator.hasNext()) {
    keys.push(String(iterator.next()));
  }
  keys.sort();
  var parts = [];
  for (var i = 0; i < keys.length; i++) {
    parts.push(keys[i] + "=" + map.get(keys[i]));
  }
  return parts.join(", ");
}

var csvText = new java.lang.String(Files.readAllBytes(inputPath), StandardCharsets.UTF_8);
var lines = csvText.replace(/\r\n/g, "\n").replace(/\r/g, "\n").split("\n");
if (lines.length < 2) {
  throw new Error("CSV内容不足");
}

var ratingCounts = new LinkedHashMap();
var weakCounts = new LinkedHashMap();
var strongCounts = new LinkedHashMap();
var rows = new ArrayList();

for (var i = 1; i < lines.length; i++) {
  if (blank(lines[i])) {
    continue;
  }
  var columns = parseCsvLine(lines[i]);
  var record = new CeshichengjiEntity();
  record.setYonghuzhanghao(text(columns[0]));
  record.setYonghuxingming(text(columns[1]));
  record.setBanji(text(columns[2]));
  record.setGender(text(columns[3]));
  record.setGrade(toInteger(columns[4]));
  record.setRun50m(toDouble(columns[5]));
  record.setRun1000m(toDouble(columns[6]));
  record.setRun800m(toDouble(columns[7]));
  record.setLongJump(toDouble(columns[8]));
  record.setPullUp(toInteger(columns[9]));
  record.setSitUp(toInteger(columns[10]));
  record.setBmi(toDouble(columns[11]));

  var analysis = service.analyze(record);
  increment(ratingCounts, text(analysis.getRating()));

  var weakItems = analysis.getWeakItems();
  for (var w = 0; w < weakItems.size(); w++) {
    increment(weakCounts, String(weakItems.get(w)));
  }

  var strongItems = analysis.getStrongItems();
  for (var s = 0; s < strongItems.size(); s++) {
    increment(strongCounts, String(strongItems.get(s)));
  }

  rows.add({
    studentNo: text(columns[0]),
    name: text(columns[1]),
    rating: text(analysis.getRating()),
    score: analysis.getCompositeScore(),
    weak: joinList(weakItems),
    strong: joinList(strongItems)
  });
}

print("ratings=" + mapText(ratingCounts));
print("weakCounts=" + mapText(weakCounts));
print("strongCounts=" + mapText(strongCounts));
for (var r = 0; r < rows.size(); r++) {
  var row = rows.get(r);
  print([
    row.studentNo,
    row.name,
    row.score,
    row.rating,
    row.weak,
    row.strong
  ].join(","));
}
