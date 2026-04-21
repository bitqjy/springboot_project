-- 初始化大模型配置（重复执行安全）
INSERT INTO `config` (`name`, `value`)
SELECT 'llmBaseUrl', 'https://api.openai.com'
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `name` = 'llmBaseUrl');

INSERT INTO `config` (`name`, `value`)
SELECT 'llmApiKey', '请在后台配置真实密钥'
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `name` = 'llmApiKey');

INSERT INTO `config` (`name`, `value`)
SELECT 'llmModel', 'gpt-4o-mini'
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `name` = 'llmModel');
