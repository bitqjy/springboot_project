Abo开发说明

开发语言：Java
框架：springboot
JDK版本：JDK1.8
服务器：tomcat7
数据库：mysql 5.7（一定要5.7版本）
数据库工具：Navicat11
开发软件：eclipse/myeclipse/idea
Maven包：Maven3.3.9
浏览器：谷歌浏览器
 
Java配置环境链接：https://pan.baidu.com/s/1Dzpiqb46mrukQzXOEj3otw 
提取码：0000    

后台路径地址：localhost:8080/项目名称/admin/dist/index.html
前台路径地址：localhost:8080/项目名称/front/index.html （无前台不需要输入）

管理员账号：abo	
管理员密码：abo

如果您要学会调试运行，一定要去看运行教学
springboot程序运行教学地址：
链接：https://pan.baidu.com/s/1qVMYZiJKYsw5DLuA30YDnQ 
提取码：0000 

如果您想对系统多一些了解，一定要去看系统讲解
springboot系统逻辑讲解地址：
链接：https://pan.baidu.com/s/1rAcNdhCrKC_KdrgIjBxG9Q 
提取码：0000 

---

2026-03 功能增强（体育测试成绩智能化）

新增后端接口（`/ceshichengji`）：

1. `POST /validate`
   - 手动录入校验，不落库。
   - 返回异常项、薄弱项、优势项、综合分与评级。

2. `GET /importTemplate`
   - 返回 Excel 导入模板字段、示例行、填写说明。

3. `POST /importExcel`
   - 支持 xls/xlsx 批量导入。
   - 支持字段：学号/姓名/班级/性别/年级/50米/1000米/800米/立定跳远/引体向上/仰卧起坐/BMI。
   - 支持异常报告（学号不存在、格式错误、超范围等）。

4. `GET /compareRoster`
   - 按测试编号+班级对比 已测/缺测/免测。

5. `GET /statsOverview`
   - 成绩统计总览（平均分、评级分布、异常数量、薄弱项 Top）。

6. `GET /aiSuggest/{id}`
   - 生成并回写 AI 个性化训练建议到测试评价。

7. `POST /aiSuggestPreview`
   - 预览 AI 建议，不写库。

8. `GET /aiSuggestBatch`
   - 批量生成 AI 建议并回写。

数据库新增脚本：

- `db/alter_ceshichengji_20260317.sql`：体测明细与异常字段
- `db/alter_config_llm_20260317.sql`：LLM 配置初始化（`llmBaseUrl` / `llmApiKey` / `llmModel`）
