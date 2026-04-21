package com.service;

import com.entity.CeshichengjiEntity;

/**
 * 大模型个性化建议服务
 */
public interface AiAdviceService {
    /**
     * 生成建议文本（中文），用于回填到 ceshichengji.ceshipingjia
     */
    String generateAdvice(CeshichengjiEntity<?> scoreRecord);
}

