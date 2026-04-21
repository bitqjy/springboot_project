package com.service.impl;

import com.entity.CeshichengjiEntity;
import com.service.ScoreAnalyzeService;
import com.service.dto.ScoreAnalysisResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 体测成绩分析服务
 */
@Service("scoreAnalyzeService")
public class ScoreAnalyzeServiceImpl implements ScoreAnalyzeService {

    private static final double BMI_PASS_LOW = 18.5D;
    private static final double BMI_PASS_HIGH = 23.9D;

    @Override
    public ScoreAnalysisResult analyze(CeshichengjiEntity<?> record) {
        ScoreAnalysisResult result = new ScoreAnalysisResult();
        if (record == null) {
            result.setAbnormal(true);
            result.getAbnormalMessages().add("成绩记录为空");
            return result;
        }

        boolean male = isMale(record.getGender());
        List<Integer> scores = new ArrayList<Integer>();

        // 50米：5~15秒（越小越好）
        addLowerBetterItem(result, scores, "RUN_50M", "50米", "s",
                record.getRun50m(), 5D, 15D, male ? 9.2D : 10.4D);

        // 长跑：1000米/800米（越小越好）
        if (male || record.getRun1000m() != null) {
            addLowerBetterItem(result, scores, "RUN_1000M", "1000米", "s",
                    record.getRun1000m(), 120D, 600D, 280D);
        }
        if (!male || record.getRun800m() != null) {
            addLowerBetterItem(result, scores, "RUN_800M", "800米", "s",
                    record.getRun800m(), 120D, 600D, 260D);
        }

        // 立定跳远：100~350cm（越大越好）
        addHigherBetterItem(result, scores, "JUMP_LONG", "立定跳远", "cm",
                record.getLongJump(), 100D, 350D, male ? 190D : 170D);

        // 力量项：男生常用引体向上、女生常用仰卧起坐；如果都填，两个都分析
        addHigherBetterItem(result, scores, "PULL_UP", "引体向上", "次",
                toDouble(record.getPullUp()), 0D, 60D, 8D);
        addHigherBetterItem(result, scores, "SIT_UP", "仰卧起坐", "次",
                toDouble(record.getSitUp()), 0D, 120D, 26D);

        // BMI：10~40，合理区间 18.5~23.9
        addBmiItem(result, scores, record.getBmi(), 10D, 40D);

        if (scores.isEmpty()) {
            // 没有细分项目时，回退到已有综合评分
            if (record.getCeshipingfen() != null) {
                result.setCompositeScore(record.getCeshipingfen());
                result.setRating(scoreToRating(record.getCeshipingfen()));
            } else {
                result.setCompositeScore(null);
                result.setRating(StringUtils.defaultIfBlank(record.getCeshipingji(), null));
            }
        } else {
            int total = 0;
            for (Integer s : scores) {
                total += s;
            }
            int composite = Math.round((float) total / (float) scores.size());
            result.setCompositeScore(composite);
            result.setRating(scoreToRating(composite));
        }
        result.setAbnormal(!result.getAbnormalMessages().isEmpty());
        return result;
    }

    @Override
    public void fillComputedFields(CeshichengjiEntity<?> record, ScoreAnalysisResult result) {
        if (record == null || result == null) {
            return;
        }
        record.setAbnormalFlag(result.isAbnormal() ? 1 : 0);
        record.setAbnormalReason(result.abnormalText());
        record.setWeakItems(result.weakItemsText());
        record.setStrongItems(result.strongItemsText());

        if (result.getCompositeScore() != null) {
            record.setCeshipingfen(result.getCompositeScore());
            record.setCeshipingji(result.getRating());
        } else if (StringUtils.isNotBlank(result.getRating())) {
            record.setCeshipingji(result.getRating());
        }
    }

    private static boolean isMale(String gender) {
        if (StringUtils.isBlank(gender)) {
            return false;
        }
        String g = gender.trim().toLowerCase();
        return g.equals("m") || g.equals("male") || g.contains("男");
    }

    private static Double toDouble(Integer v) {
        return v == null ? null : Double.valueOf(v.doubleValue());
    }

    private void addLowerBetterItem(ScoreAnalysisResult result, List<Integer> scores,
                                    String itemCode, String itemName, String unit,
                                    Double value, double min, double max, double passLine) {
        if (value == null) {
            return;
        }
        Map<String, Object> item = baseItem(itemCode, itemName, unit, value, min, max);
        if (value < min || value > max) {
            String msg = itemName + "成绩超出合理范围(" + trim(min) + "~" + trim(max) + unit + ")：" + trim(value);
            item.put("abnormal", true);
            item.put("message", msg);
            result.getAbnormalMessages().add(msg);
            result.getItemResults().add(item);
            return;
        }
        int normalized = normalizeLowerBetter(value, min, max);
        scores.add(normalized);

        boolean pass = value <= passLine;
        item.put("pass", pass);
        item.put("passLine", trim(passLine));
        item.put("normalizedScore", normalized);
        if (!pass) {
            result.getWeakItems().add(itemName);
            item.put("level", "偏弱");
        } else if (normalized >= 85) {
            result.getStrongItems().add(itemName);
            item.put("level", "优势");
        } else {
            item.put("level", "中等");
        }
        result.getItemResults().add(item);
    }

    private void addHigherBetterItem(ScoreAnalysisResult result, List<Integer> scores,
                                     String itemCode, String itemName, String unit,
                                     Double value, double min, double max, double passLine) {
        if (value == null) {
            return;
        }
        Map<String, Object> item = baseItem(itemCode, itemName, unit, value, min, max);
        if (value < min || value > max) {
            String msg = itemName + "成绩超出合理范围(" + trim(min) + "~" + trim(max) + unit + ")：" + trim(value);
            item.put("abnormal", true);
            item.put("message", msg);
            result.getAbnormalMessages().add(msg);
            result.getItemResults().add(item);
            return;
        }
        int normalized = normalizeHigherBetter(value, min, max);
        scores.add(normalized);

        boolean pass = value >= passLine;
        item.put("pass", pass);
        item.put("passLine", trim(passLine));
        item.put("normalizedScore", normalized);
        if (!pass) {
            result.getWeakItems().add(itemName);
            item.put("level", "偏弱");
        } else if (normalized >= 85) {
            result.getStrongItems().add(itemName);
            item.put("level", "优势");
        } else {
            item.put("level", "中等");
        }
        result.getItemResults().add(item);
    }

    private void addBmiItem(ScoreAnalysisResult result, List<Integer> scores,
                            Double value, double min, double max) {
        if (value == null) {
            return;
        }
        Map<String, Object> item = baseItem("BMI", "BMI", "", value, min, max);
        if (value < min || value > max) {
            String msg = "BMI超出合理范围(" + trim(min) + "~" + trim(max) + ")：" + trim(value);
            item.put("abnormal", true);
            item.put("message", msg);
            result.getAbnormalMessages().add(msg);
            result.getItemResults().add(item);
            return;
        }

        int normalized = normalizeBmi(value, min, max);
        scores.add(normalized);
        boolean pass = value >= BMI_PASS_LOW && value <= BMI_PASS_HIGH;
        item.put("pass", pass);
        item.put("passLine", trim(BMI_PASS_LOW) + "~" + trim(BMI_PASS_HIGH));
        item.put("normalizedScore", normalized);
        if (!pass) {
            result.getWeakItems().add("BMI");
            item.put("level", "偏弱");
        } else if (normalized >= 85) {
            result.getStrongItems().add("BMI");
            item.put("level", "优势");
        } else {
            item.put("level", "中等");
        }
        result.getItemResults().add(item);
    }

    private static Map<String, Object> baseItem(String code, String name, String unit, Double value, double min, double max) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        item.put("itemCode", code);
        item.put("itemName", name);
        item.put("value", value);
        item.put("unit", unit);
        item.put("min", min);
        item.put("max", max);
        item.put("abnormal", false);
        return item;
    }

    private static int normalizeLowerBetter(double value, double min, double max) {
        double score = (max - value) / (max - min) * 100D;
        return clampScore(score);
    }

    private static int normalizeHigherBetter(double value, double min, double max) {
        double score = (value - min) / (max - min) * 100D;
        return clampScore(score);
    }

    private static int normalizeBmi(double value, double min, double max) {
        if (value >= BMI_PASS_LOW && value <= BMI_PASS_HIGH) {
            double center = (BMI_PASS_LOW + BMI_PASS_HIGH) / 2D;
            double distance = Math.abs(value - center);
            // 在合理区间内按“越接近中心越高分”计算，80~100分
            double half = (BMI_PASS_HIGH - BMI_PASS_LOW) / 2D;
            double factor = half == 0D ? 0D : Math.min(1D, distance / half);
            return clampScore(100D - factor * 20D);
        }
        // 超出合理区间，按全局区间降分
        double raw = 60D - (Math.min(Math.abs(value - BMI_PASS_LOW), Math.abs(value - BMI_PASS_HIGH)) / (max - min) * 100D);
        return clampScore(raw);
    }

    private static int clampScore(double score) {
        if (score < 0D) {
            score = 0D;
        }
        if (score > 100D) {
            score = 100D;
        }
        return (int) Math.round(score);
    }

    private static String scoreToRating(int score) {
        if (score >= 85) {
            return "优秀";
        }
        if (score >= 75) {
            return "良好";
        }
        if (score >= 50) {
            return "及格";
        }
        return "不及格";
    }

    private static String trim(double v) {
        long lv = Math.round(v);
        if (Math.abs(v - lv) < 0.0001D) {
            return String.valueOf(lv);
        }
        return String.valueOf(v);
    }
}
