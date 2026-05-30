package com.service.impl;

import com.entity.CeshichengjiEntity;
import com.service.ScoreAnalyzeService;
import com.service.dto.ScoreAnalysisResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
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
    private static final double BMI_SCORE_LOW = 16D;
    private static final double BMI_SCORE_HIGH = 30D;

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

        // 50米：5~15秒为合理区间；按更贴近体测的计分窗口映射到综合分
        addLowerBetterItem(result, scores, "RUN_50M", "50米", "s",
                record.getRun50m(),
                5D, 15D,
                male ? 6.0D : 7.0D,
                male ? 12.0D : 13.0D,
                male ? 9.2D : 10.4D);

        // 长跑：合理范围与计分窗口拆开，避免“大范围线性映射”导致所有人都挤在及格档
        if (male || record.getRun1000m() != null) {
            addLowerBetterItem(result, scores, "RUN_1000M", "1000米", "s",
                    record.getRun1000m(),
                    120D, 600D,
                    180D, 420D,
                    280D);
        }
        if (!male || record.getRun800m() != null) {
            addLowerBetterItem(result, scores, "RUN_800M", "800米", "s",
                    record.getRun800m(),
                    120D, 600D,
                    150D, 360D,
                    260D);
        }

        // 立定跳远：合理范围保留宽口径，计分窗口按男女分层
        addHigherBetterItem(result, scores, "JUMP_LONG", "立定跳远", "cm",
                record.getLongJump(),
                100D, 350D,
                male ? 140D : 120D,
                male ? 280D : 250D,
                male ? 190D : 170D);

        // 力量项：男生常用引体向上、女生常用仰卧起坐；如果都填，两个都分析
        addHigherBetterItem(result, scores, "PULL_UP", "引体向上", "次",
                toDouble(record.getPullUp()),
                0D, 60D,
                0D, 20D,
                8D);
        addHigherBetterItem(result, scores, "SIT_UP", "仰卧起坐", "次",
                toDouble(record.getSitUp()),
                0D, 120D,
                10D, 70D,
                26D);

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
        finalizeItemLevels(result);
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
                                    Double value, double validMin, double validMax,
                                    double scoreBest, double scoreWorst, double passLine) {
        if (value == null) {
            return;
        }
        Map<String, Object> item = baseItem(itemCode, itemName, unit, value, validMin, validMax);
        if (value < validMin || value > validMax) {
            String msg = itemName + "成绩超出合理范围(" + trim(validMin) + "~" + trim(validMax) + unit + ")：" + trim(value);
            item.put("abnormal", true);
            item.put("message", msg);
            result.getAbnormalMessages().add(msg);
            result.getItemResults().add(item);
            return;
        }
        int normalized = normalizeLowerBetter(value, scoreBest, scoreWorst, passLine);
        scores.add(normalized);

        boolean pass = value <= passLine;
        item.put("pass", pass);
        item.put("passLine", trim(passLine));
        item.put("normalizedScore", normalized);
        item.put("level", pass ? "中等" : "偏弱");
        result.getItemResults().add(item);
    }

    private void addHigherBetterItem(ScoreAnalysisResult result, List<Integer> scores,
                                     String itemCode, String itemName, String unit,
                                     Double value, double validMin, double validMax,
                                     double scoreWorst, double scoreBest, double passLine) {
        if (value == null) {
            return;
        }
        Map<String, Object> item = baseItem(itemCode, itemName, unit, value, validMin, validMax);
        if (value < validMin || value > validMax) {
            String msg = itemName + "成绩超出合理范围(" + trim(validMin) + "~" + trim(validMax) + unit + ")：" + trim(value);
            item.put("abnormal", true);
            item.put("message", msg);
            result.getAbnormalMessages().add(msg);
            result.getItemResults().add(item);
            return;
        }
        int normalized = normalizeHigherBetter(value, scoreWorst, scoreBest, passLine);
        scores.add(normalized);

        boolean pass = value >= passLine;
        item.put("pass", pass);
        item.put("passLine", trim(passLine));
        item.put("normalizedScore", normalized);
        item.put("level", pass ? "中等" : "偏弱");
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

        int normalized = normalizeBmi(value, BMI_SCORE_LOW, BMI_SCORE_HIGH);
        scores.add(normalized);
        boolean pass = value >= BMI_PASS_LOW && value <= BMI_PASS_HIGH;
        item.put("pass", pass);
        item.put("passLine", trim(BMI_PASS_LOW) + "~" + trim(BMI_PASS_HIGH));
        item.put("normalizedScore", normalized);
        item.put("level", pass ? "中等" : "偏弱");
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

    private static int normalizeLowerBetter(double value, double scoreBest, double scoreWorst, double passLine) {
        double score;
        if (value <= passLine) {
            double ratio = (passLine - value) / Math.max(passLine - scoreBest, 0.0001D);
            score = 60D + ratio * 40D;
        } else {
            double ratio = (value - passLine) / Math.max(scoreWorst - passLine, 0.0001D);
            score = 60D - ratio * 60D;
        }
        return clampScore(score);
    }

    private static int normalizeHigherBetter(double value, double scoreWorst, double scoreBest, double passLine) {
        double score;
        if (value >= passLine) {
            double ratio = (value - passLine) / Math.max(scoreBest - passLine, 0.0001D);
            score = 60D + ratio * 40D;
        } else {
            double ratio = (passLine - value) / Math.max(passLine - scoreWorst, 0.0001D);
            score = 60D - ratio * 60D;
        }
        return clampScore(score);
    }

    private static int normalizeBmi(double value, double scoreLow, double scoreHigh) {
        if (value >= BMI_PASS_LOW && value <= BMI_PASS_HIGH) {
            double center = (BMI_PASS_LOW + BMI_PASS_HIGH) / 2D;
            double distance = Math.abs(value - center);
            // BMI 更像健康基线，避免它过于轻易成为所有人的唯一优势项
            double half = (BMI_PASS_HIGH - BMI_PASS_LOW) / 2D;
            double factor = half == 0D ? 0D : Math.min(1D, distance / half);
            return clampScore(88D - factor * 20D);
        }
        double ratio;
        if (value < BMI_PASS_LOW) {
            ratio = (BMI_PASS_LOW - value) / Math.max(BMI_PASS_LOW - scoreLow, 0.0001D);
        } else {
            ratio = (value - BMI_PASS_HIGH) / Math.max(scoreHigh - BMI_PASS_HIGH, 0.0001D);
        }
        double raw = 60D - ratio * 60D;
        return clampScore(raw);
    }

    private static void finalizeItemLevels(ScoreAnalysisResult result) {
        if (result == null) {
            return;
        }

        result.getWeakItems().clear();
        result.getStrongItems().clear();

        List<Map<String, Object>> scoredItems = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : result.getItemResults()) {
            if (item == null || Boolean.TRUE.equals(item.get("abnormal")) || !(item.get("normalizedScore") instanceof Number)) {
                continue;
            }
            scoredItems.add(item);
        }
        if (scoredItems.isEmpty()) {
            return;
        }

        int bucketCount = Math.max(1, (int) Math.ceil(scoredItems.size() * 0.3D));
        List<Map<String, Object>> asc = new ArrayList<Map<String, Object>>(scoredItems);
        asc.sort(Comparator.comparingInt(ScoreAnalyzeServiceImpl::itemScore));
        List<Map<String, Object>> desc = new ArrayList<Map<String, Object>>(scoredItems);
        desc.sort((a, b) -> Integer.compare(itemScore(b), itemScore(a)));

        LinkedHashSet<String> weakSet = new LinkedHashSet<String>();
        LinkedHashSet<String> strongSet = new LinkedHashSet<String>();

        for (Map<String, Object> item : scoredItems) {
            String itemName = String.valueOf(item.get("itemName"));
            int score = itemScore(item);
            boolean pass = !Boolean.FALSE.equals(item.get("pass"));
            if (!pass || score < 60) {
                weakSet.add(itemName);
            }
            if (pass && score >= 88) {
                strongSet.add(itemName);
            }
        }

        for (int i = 0; i < asc.size() && i < bucketCount; i++) {
            Map<String, Object> item = asc.get(i);
            String itemName = String.valueOf(item.get("itemName"));
            int score = itemScore(item);
            if (score <= 78 || weakSet.isEmpty()) {
                weakSet.add(itemName);
            }
        }

        for (int i = 0; i < desc.size() && i < bucketCount; i++) {
            Map<String, Object> item = desc.get(i);
            String itemName = String.valueOf(item.get("itemName"));
            int score = itemScore(item);
            if (score >= 65 || strongSet.isEmpty()) {
                strongSet.add(itemName);
            }
        }

        for (String itemName : new ArrayList<String>(weakSet)) {
            if (!strongSet.contains(itemName)) {
                continue;
            }
            Integer score = findItemScore(scoredItems, itemName);
            if (score == null) {
                weakSet.remove(itemName);
                strongSet.remove(itemName);
            } else if (score >= 78) {
                weakSet.remove(itemName);
            } else {
                strongSet.remove(itemName);
            }
        }

        for (Map<String, Object> item : scoredItems) {
            String itemName = String.valueOf(item.get("itemName"));
            int score = itemScore(item);
            if (weakSet.contains(itemName)) {
                addUnique(result.getWeakItems(), itemName);
                item.put("level", score < 60 ? "薄弱" : "待提升");
            } else if (strongSet.contains(itemName)) {
                addUnique(result.getStrongItems(), itemName);
                item.put("level", score >= 85 ? "优势" : "相对优势");
            } else if (score >= 75) {
                item.put("level", "良好");
            } else if (score >= 60) {
                item.put("level", "中等");
            } else {
                item.put("level", "待提升");
            }
        }
    }

    private static Integer findItemScore(List<Map<String, Object>> items, String itemName) {
        if (items == null || StringUtils.isBlank(itemName)) {
            return null;
        }
        for (Map<String, Object> item : items) {
            if (item != null && itemName.equals(String.valueOf(item.get("itemName")))) {
                return itemScore(item);
            }
        }
        return null;
    }

    private static int itemScore(Map<String, Object> item) {
        Object value = item == null ? null : item.get("normalizedScore");
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static void addUnique(List<String> items, String value) {
        if (items == null || StringUtils.isBlank(value) || items.contains(value)) {
            return;
        }
        items.add(value);
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
