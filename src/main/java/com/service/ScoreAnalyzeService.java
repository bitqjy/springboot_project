package com.service;

import com.entity.CeshichengjiEntity;
import com.service.dto.ScoreAnalysisResult;

/**
 * 体测成绩分析与异常检测
 */
public interface ScoreAnalyzeService {

    /**
     * 对单条成绩进行分析
     */
    ScoreAnalysisResult analyze(CeshichengjiEntity<?> record);

    /**
     * 把分析结果回写到成绩实体（异常标记、强弱项、综合评分/评级）
     */
    void fillComputedFields(CeshichengjiEntity<?> record, ScoreAnalysisResult result);
}

