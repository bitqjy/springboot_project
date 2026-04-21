-- 为 ceshichengji 增加体测明细与异常检测字段（幂等执行请自行判断列是否已存在）
ALTER TABLE `ceshichengji`
    ADD COLUMN `run50m` decimal(10,2) NULL COMMENT '50米成绩(秒)' AFTER `ceshipingji`,
    ADD COLUMN `run1000m` decimal(10,2) NULL COMMENT '1000米成绩(秒)' AFTER `run50m`,
    ADD COLUMN `run800m` decimal(10,2) NULL COMMENT '800米成绩(秒)' AFTER `run1000m`,
    ADD COLUMN `long_jump` decimal(10,2) NULL COMMENT '立定跳远(cm)' AFTER `run800m`,
    ADD COLUMN `pull_up` int(11) NULL COMMENT '引体向上(次)' AFTER `long_jump`,
    ADD COLUMN `sit_up` int(11) NULL COMMENT '仰卧起坐(次)' AFTER `pull_up`,
    ADD COLUMN `bmi` decimal(10,2) NULL COMMENT 'BMI' AFTER `sit_up`,
    ADD COLUMN `gender` varchar(20) NULL COMMENT '性别' AFTER `bmi`,
    ADD COLUMN `grade` int(11) NULL COMMENT '年级' AFTER `gender`,
    ADD COLUMN `abnormal_flag` tinyint(4) NULL DEFAULT '0' COMMENT '异常标记：0否1是' AFTER `grade`,
    ADD COLUMN `abnormal_reason` varchar(500) NULL COMMENT '异常原因' AFTER `abnormal_flag`,
    ADD COLUMN `weak_items` varchar(500) NULL COMMENT '薄弱项' AFTER `abnormal_reason`,
    ADD COLUMN `strong_items` varchar(500) NULL COMMENT '优势项' AFTER `weak_items`;

