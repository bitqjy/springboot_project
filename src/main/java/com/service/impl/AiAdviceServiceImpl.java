package com.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.entity.CeshichengjiEntity;
import com.entity.ConfigEntity;
import com.service.AiAdviceService;
import com.service.ConfigService;
import com.service.ScoreAnalyzeService;
import com.service.dto.ScoreAnalysisResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("aiAdviceService")
public class AiAdviceServiceImpl implements AiAdviceService {

    @Autowired
    private ConfigService configService;

    @Autowired
    private ScoreAnalyzeService scoreAnalyzeService;

    private final RestTemplate restTemplate = buildUtf8RestTemplate();

    @Override
    public String generateAdvice(CeshichengjiEntity<?> scoreRecord) {
        if (scoreRecord == null) {
            throw new IllegalArgumentException("成绩记录为空");
        }

        String baseUrl = getConfig("llmBaseUrl");
        String apiKey = getConfig("llmApiKey");
        String model = getConfig("llmModel");

        if (StringUtils.isBlank(baseUrl) || StringUtils.isBlank(apiKey) || StringUtils.isBlank(model)) {
            throw new IllegalStateException("请在配置管理中设置 llmBaseUrl / llmApiKey / llmModel");
        }
        String url = resolveChatCompletionsUrl(baseUrl);

        String prompt = buildPrompt(scoreRecord);

        JSONObject req = new JSONObject(true);
        req.put("model", model);
        req.put("temperature", 0.4);
        req.put("max_tokens", 500);
        JSONArray messages = new JSONArray();
        JSONObject sys = new JSONObject(true);
        sys.put("role", "system");
        sys.put("content", "你是学校体育老师的智能助手。输出要简洁、可执行、避免医疗诊断与夸大承诺。");
        messages.add(sys);
        JSONObject user = new JSONObject(true);
        user.put("role", "user");
        user.put("content", prompt);
        messages.add(user);
        req.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> entity = new HttpEntity<String>(req.toJSONString(), headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
        if (resp.getBody() == null) {
            throw new IllegalStateException("大模型返回为空");
        }
        JSONObject json = JSONObject.parseObject(resp.getBody());
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("大模型返回缺少choices");
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        if (message == null) {
            throw new IllegalStateException("大模型返回缺少message");
        }
        String content = message.getString("content");
        return normalizeModelContent(content);
    }

    /**
     * 兼容不同配置风格：
     * - https://xxx
     * - https://xxx/
     * - https://xxx/v1
     * - https://xxx/v1/
     * - https://xxx/chat/completions
     * - https://api.deepseek.com
     */
    private String resolveChatCompletionsUrl(String baseUrl) {
        String normalized = StringUtils.trimToEmpty(baseUrl);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/v1")) {
            return normalized + "/chat/completions";
        }
        if ("https://api.deepseek.com".equalsIgnoreCase(normalized)
                || "http://api.deepseek.com".equalsIgnoreCase(normalized)) {
            return normalized + "/chat/completions";
        }
        return normalized + "/v1/chat/completions";
    }

    private String getConfig(String name) {
        ConfigEntity e = configService.selectOne(new EntityWrapper<ConfigEntity>().eq("name", name));
        return e == null ? null : e.getValue();
    }

    private String buildPrompt(CeshichengjiEntity<?> r) {
        ScoreAnalysisResult analysis = scoreAnalyzeService.analyze(r);
        String name = StringUtils.defaultIfBlank(r.getYonghuxingming(), "该学生");
        String banji = StringUtils.defaultIfBlank(r.getBanji(), "未知班级");
        String testName = StringUtils.defaultIfBlank(r.getCeshimingcheng(), "体质测试");
        String score = r.getCeshipingfen() == null ? "未知" : String.valueOf(r.getCeshipingfen());
        String level = StringUtils.defaultIfBlank(r.getCeshipingji(), "未知");
        String gender = StringUtils.defaultIfBlank(r.getGender(), "未知");
        String grade = r.getGrade() == null ? "未知" : String.valueOf(r.getGrade());

        List<String> metricLines = new ArrayList<String>();
        if (r.getRun50m() != null) {
            metricLines.add("- 50米: " + r.getRun50m() + "秒");
        }
        if (r.getRun1000m() != null) {
            metricLines.add("- 1000米: " + r.getRun1000m() + "秒");
        }
        if (r.getRun800m() != null) {
            metricLines.add("- 800米: " + r.getRun800m() + "秒");
        }
        if (r.getLongJump() != null) {
            metricLines.add("- 立定跳远: " + r.getLongJump() + "cm");
        }
        if (r.getPullUp() != null) {
            metricLines.add("- 引体向上: " + r.getPullUp() + "次");
        }
        if (r.getSitUp() != null) {
            metricLines.add("- 仰卧起坐: " + r.getSitUp() + "次");
        }
        if (r.getBmi() != null) {
            metricLines.add("- BMI: " + r.getBmi());
        }
        if (metricLines.isEmpty()) {
            metricLines.add("- 暂无细分项目成绩（仅有综合分）");
        }
        String metrics = StringUtils.join(metricLines, "\n");

        return ""
                + "请基于以下体测数据，生成该学生的个性化训练建议（中文），并严格遵守：\n"
                + "1) 先给总体评估（优势/短板）\n"
                + "2) 给每周训练建议（频次/时长/强度）\n"
                + "3) 对薄弱项分别给改进动作\n"
                + "4) 避免医疗诊断；涉及伤病仅建议咨询校医/医生\n"
                + "5) 末尾给一句简短鼓励\n"
                + "6) 输出为自然语言段落，150~300字\n\n"
                + "学生：" + name + "\n"
                + "班级：" + banji + "\n"
                + "性别：" + gender + "\n"
                + "年级：" + grade + "\n"
                + "项目：" + testName + "\n"
                + "评分：" + score + "\n"
                + "评级：" + level + "\n"
                + "异常：" + (analysis.isAbnormal() ? "是" : "否") + "\n"
                + "异常说明：" + StringUtils.defaultIfBlank(analysis.abnormalText(), "无") + "\n"
                + "薄弱项：" + StringUtils.defaultIfBlank(analysis.weakItemsText(), "无") + "\n"
                + "优势项：" + StringUtils.defaultIfBlank(analysis.strongItemsText(), "无") + "\n"
                + "细分成绩：\n" + metrics + "\n";
    }

    private static RestTemplate buildUtf8RestTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.getMessageConverters().removeIf(c -> c instanceof StringHttpMessageConverter);
        rt.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return rt;
    }

    private static String normalizeModelContent(String raw) {
        String text = StringUtils.trimToEmpty(raw);
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String recovered = new String(text.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        if (looksLikeChinese(recovered) && !looksLikeChinese(text)) {
            return recovered;
        }
        return text;
    }

    private static boolean looksLikeChinese(String s) {
        if (StringUtils.isBlank(s)) {
            return false;
        }
        int cnt = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) {
                cnt++;
                if (cnt >= 3) {
                    return true;
                }
            }
        }
        return false;
    }
}
