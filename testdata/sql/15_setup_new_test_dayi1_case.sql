-- 新测试：大一1班缺测/免测案例
-- 测试编号（导入成绩时请使用）：T20260320-DYI1-CASE

-- 1) 创建/更新体质测试任务
INSERT INTO tizhiceshi (
  id, ceshibianhao, ceshimingcheng, ceshiwenjian, tupian, faburiqi,
  ceshishuoming, ceshineirong, jiaoshigonghao, jiaoshixingming
)
VALUES (
  202603200001,
  'T20260320-DYI1-CASE',
  '2026春季体测-大一1班案例',
  '',
  '',
  '2026-03-20 09:00:00',
  '用于演示缺测/免测识别：共10人，6人成绩导入，2人免测，2人缺测',
  '步骤：先执行本脚本，再导入CSV成绩，最后在缺测/免测识别中查询',
  '123',
  'qjyy'
)
ON DUPLICATE KEY UPDATE
  ceshimingcheng = VALUES(ceshimingcheng),
  faburiqi = VALUES(faburiqi),
  ceshishuoming = VALUES(ceshishuoming),
  ceshineirong = VALUES(ceshineirong),
  jiaoshigonghao = VALUES(jiaoshigonghao),
  jiaoshixingming = VALUES(jiaoshixingming);

-- 2) 清理该测试下旧数据（防止重复演示）
DELETE FROM ceshichengji WHERE ceshibianhao = 'T20260320-DYI1-CASE';
DELETE FROM ceshimiance WHERE ceshibianhao = 'T20260320-DYI1-CASE';

-- 3) 写入两条免测记录（stu002, stu005）
INSERT INTO ceshimiance (
  id, ceshibianhao, ceshimingcheng, jiaoshigonghao, jiaoshixingming,
  yonghuzhanghao, yonghuxingming, banji, mianceyuanyin, mianceriqi
)
SELECT
  202603200101,
  'T20260320-DYI1-CASE',
  '2026春季体测-大一1班案例',
  '123',
  'qjyy',
  y.yonghuzhanghao,
  y.yonghuxingming,
  y.banji,
  '膝关节旧伤，校医院证明',
  '2026-03-20'
FROM yonghu y
WHERE y.yonghuzhanghao = 'stu002'
UNION ALL
SELECT
  202603200102,
  'T20260320-DYI1-CASE',
  '2026春季体测-大一1班案例',
  '123',
  'qjyy',
  y.yonghuzhanghao,
  y.yonghuxingming,
  y.banji,
  '急性踝关节扭伤，医生建议免测',
  '2026-03-20'
FROM yonghu y
WHERE y.yonghuzhanghao = 'stu005';
