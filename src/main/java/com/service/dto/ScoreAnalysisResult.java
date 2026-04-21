package com.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 体测成绩分析结果
 */
public class ScoreAnalysisResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean abnormal;
    private Integer compositeScore;
    private String rating;
    private List<String> abnormalMessages = new ArrayList<String>();
    private List<String> weakItems = new ArrayList<String>();
    private List<String> strongItems = new ArrayList<String>();
    private List<Map<String, Object>> itemResults = new ArrayList<Map<String, Object>>();

    public boolean isAbnormal() {
        return abnormal;
    }

    public void setAbnormal(boolean abnormal) {
        this.abnormal = abnormal;
    }

    public Integer getCompositeScore() {
        return compositeScore;
    }

    public void setCompositeScore(Integer compositeScore) {
        this.compositeScore = compositeScore;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public List<String> getAbnormalMessages() {
        return abnormalMessages;
    }

    public void setAbnormalMessages(List<String> abnormalMessages) {
        this.abnormalMessages = abnormalMessages;
    }

    public List<String> getWeakItems() {
        return weakItems;
    }

    public void setWeakItems(List<String> weakItems) {
        this.weakItems = weakItems;
    }

    public List<String> getStrongItems() {
        return strongItems;
    }

    public void setStrongItems(List<String> strongItems) {
        this.strongItems = strongItems;
    }

    public List<Map<String, Object>> getItemResults() {
        return itemResults;
    }

    public void setItemResults(List<Map<String, Object>> itemResults) {
        this.itemResults = itemResults;
    }

    public String weakItemsText() {
        return join(weakItems);
    }

    public String strongItemsText() {
        return join(strongItems);
    }

    public String abnormalText() {
        return join(abnormalMessages);
    }

    private static String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append("、");
            }
            sb.append(values.get(i));
        }
        return sb.toString();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("abnormal", abnormal);
        map.put("compositeScore", compositeScore);
        map.put("rating", rating);
        map.put("abnormalMessages", abnormalMessages);
        map.put("weakItems", weakItems);
        map.put("strongItems", strongItems);
        map.put("itemResults", itemResults);
        return map;
    }
}

