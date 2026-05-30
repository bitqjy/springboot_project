var Files = Java.type("java.nio.file.Files");
var Paths = Java.type("java.nio.file.Paths");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
var DriverManager = Java.type("java.sql.DriverManager");
var ScoreAnalyzeServiceImpl = Java.type("com.service.impl.ScoreAnalyzeServiceImpl");
var CeshichengjiEntity = Java.type("com.entity.CeshichengjiEntity");

var inputPath = Paths.get("testdata/import/20260422_dayi_demo1_scores.csv");
var jdbcUrl = "jdbc:mysql://127.0.0.1:3306/springboot415ef?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false";
var jdbcUser = "root";
var jdbcPassword = "123456";
var testNo = "TC20260422-DEMO01";

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

function setNullableDouble(ps, index, value) {
  if (value == null) {
    ps.setNull(index, java.sql.Types.DECIMAL);
  } else {
    ps.setDouble(index, value.doubleValue());
  }
}

function setNullableInteger(ps, index, value) {
  if (value == null) {
    ps.setNull(index, java.sql.Types.INTEGER);
  } else {
    ps.setInt(index, value.intValue());
  }
}

function setNullableString(ps, index, value) {
  if (blank(value)) {
    ps.setNull(index, java.sql.Types.VARCHAR);
  } else {
    ps.setString(index, String(value));
  }
}

var csvText = new java.lang.String(Files.readAllBytes(inputPath), StandardCharsets.UTF_8);
var lines = csvText.replace(/\r\n/g, "\n").replace(/\r/g, "\n").split("\n");
if (lines.length < 2) {
  throw new Error("CSV内容不足");
}

var service = new ScoreAnalyzeServiceImpl();
var conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
conn.setAutoCommit(false);

var updateSql = ""
  + "update ceshichengji set "
  + "yonghuxingming=?, banji=?, gender=?, grade=?, "
  + "run50m=?, run1000m=?, run800m=?, long_jump=?, pull_up=?, sit_up=?, bmi=?, "
  + "ceshipingfen=?, ceshipingji=?, abnormal_flag=?, abnormal_reason=?, weak_items=?, strong_items=? "
  + "where ceshibianhao=? and yonghuzhanghao=?";
var updatePs = conn.prepareStatement(updateSql);
var countPs = conn.prepareStatement("select count(*) as cnt from ceshichengji where ceshibianhao=? and yonghuzhanghao=?");

var updated = 0;
var missing = [];

for (var i = 1; i < lines.length; i++) {
  if (blank(lines[i])) {
    continue;
  }
  var columns = parseCsvLine(lines[i]);

  var record = new CeshichengjiEntity();
  record.setCeshibianhao(testNo);
  record.setYonghuzhanghao(String(columns[0]));
  record.setYonghuxingming(String(columns[1]));
  record.setBanji(String(columns[2]));
  record.setGender(String(columns[3]));
  record.setGrade(toInteger(columns[4]));
  record.setRun50m(toDouble(columns[5]));
  record.setRun1000m(toDouble(columns[6]));
  record.setRun800m(toDouble(columns[7]));
  record.setLongJump(toDouble(columns[8]));
  record.setPullUp(toInteger(columns[9]));
  record.setSitUp(toInteger(columns[10]));
  record.setBmi(toDouble(columns[11]));

  var analysis = service.analyze(record);
  service.fillComputedFields(record, analysis);

  countPs.setString(1, testNo);
  countPs.setString(2, record.getYonghuzhanghao());
  var rs = countPs.executeQuery();
  var exists = false;
  if (rs.next()) {
    exists = rs.getInt("cnt") > 0;
  }
  rs.close();
  if (!exists) {
    missing.push(record.getYonghuzhanghao() + " " + record.getYonghuxingming());
    continue;
  }

  var idx = 1;
  setNullableString(updatePs, idx++, record.getYonghuxingming());
  setNullableString(updatePs, idx++, record.getBanji());
  setNullableString(updatePs, idx++, record.getGender());
  setNullableInteger(updatePs, idx++, record.getGrade());
  setNullableDouble(updatePs, idx++, record.getRun50m());
  setNullableDouble(updatePs, idx++, record.getRun1000m());
  setNullableDouble(updatePs, idx++, record.getRun800m());
  setNullableDouble(updatePs, idx++, record.getLongJump());
  setNullableInteger(updatePs, idx++, record.getPullUp());
  setNullableInteger(updatePs, idx++, record.getSitUp());
  setNullableDouble(updatePs, idx++, record.getBmi());
  setNullableInteger(updatePs, idx++, record.getCeshipingfen());
  setNullableString(updatePs, idx++, record.getCeshipingji());
  setNullableInteger(updatePs, idx++, record.getAbnormalFlag());
  setNullableString(updatePs, idx++, record.getAbnormalReason());
  setNullableString(updatePs, idx++, record.getWeakItems());
  setNullableString(updatePs, idx++, record.getStrongItems());
  updatePs.setString(idx++, testNo);
  updatePs.setString(idx++, record.getYonghuzhanghao());
  updated += updatePs.executeUpdate();
}

conn.commit();
updatePs.close();
countPs.close();
conn.close();

print("updated=" + updated);
if (missing.length > 0) {
  print("missing=" + missing.join(", "));
}
