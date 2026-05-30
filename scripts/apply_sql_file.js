var Files = Java.type("java.nio.file.Files");
var Paths = Java.type("java.nio.file.Paths");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
var DriverManager = Java.type("java.sql.DriverManager");

if (arguments.length < 1) {
  throw new Error("用法: jrunscript -cp <jdbc-driver> scripts/apply_sql_file.js <sql-file>");
}

var sqlPath = Paths.get(String(arguments[0]));
var jdbcUrl = "jdbc:mysql://127.0.0.1:3306/springboot415ef?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false";
var jdbcUser = "root";
var jdbcPassword = "123456";

function sanitizeSql(text) {
  var lines = text.replace(/\r\n/g, "\n").replace(/\r/g, "\n").split("\n");
  var kept = [];
  for (var i = 0; i < lines.length; i++) {
    var line = String(lines[i]);
    if (/^\s*--/.test(line)) {
      continue;
    }
    kept.push(line);
  }
  return kept.join("\n");
}

var sqlText = new java.lang.String(Files.readAllBytes(sqlPath), StandardCharsets.UTF_8);
sqlText = sanitizeSql(sqlText);
var statements = sqlText.split(/;\s*\n/);

var conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
conn.setAutoCommit(false);
var stmt = conn.createStatement();
var executed = 0;

for (var i = 0; i < statements.length; i++) {
  var sql = String(statements[i]).trim();
  if (!sql) {
    continue;
  }
  stmt.execute(sql);
  executed++;
}

conn.commit();
stmt.close();
conn.close();

print("executed=" + executed);
print("sql=" + sqlPath.toString());
