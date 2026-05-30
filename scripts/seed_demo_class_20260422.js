var Files = Java.type("java.nio.file.Files");
var Paths = Java.type("java.nio.file.Paths");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
var DriverManager = Java.type("java.sql.DriverManager");

java.lang.Class.forName("com.mysql.cj.jdbc.Driver");

var sqlPath = Paths.get("db/seed_dayi_demo1_class30_20260422.sql");
var sqlText = new java.lang.String(Files.readAllBytes(sqlPath), StandardCharsets.UTF_8);

var conn = DriverManager.getConnection(
  "jdbc:mysql://127.0.0.1:3306/springboot415ef?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
  "root",
  "123456"
);

function queryInt(statement, sql) {
  var rs = statement.executeQuery(sql);
  rs.next();
  var value = rs.getInt(1);
  rs.close();
  return value;
}

var stmt = conn.createStatement();
sqlText.split(/;/).forEach(function(part) {
  var trimmed = String(part).trim();
  if (trimmed.length > 0) {
    stmt.execute(trimmed);
  }
});

print("students=" + queryInt(stmt, "select count(*) from yonghu where banji='大一演示1班'"));
print("reports=" + queryInt(stmt, "select count(*) from ceshibaogao where ceshibianhao='TC20260422-DEMO01'"));
print("tests=" + queryInt(stmt, "select count(*) from tizhiceshi where ceshibianhao='TC20260422-DEMO01'"));

stmt.close();
conn.close();
