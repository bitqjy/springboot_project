-- 初始化大模型配置（重复执行安全）
INSERT INTO `config` (`name`, `value`)
SELECT 'llmBaseUrl', 'https://api.deepseek.com'
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `name` = 'llmBaseUrl');

INSERT INTO `config` (`name`, `value`)
SELECT 'llmApiKey', 'sk-diipiumhoceaggiaprfpfogrcoaemmbtnmlvnvqinsakducv'
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `name` = 'llmApiKey');

INSERT INTO `config` (`name`, `value`)
SELECT 'llmModel', 'deepseek-v4-flash'
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `name` = 'llmModel');
