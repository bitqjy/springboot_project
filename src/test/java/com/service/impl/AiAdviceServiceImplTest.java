package com.service.impl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiAdviceServiceImplTest {

    @Test
    void resolvesDeepSeekBaseUrlWithoutV1Path() throws Exception {
        Method method = AiAdviceServiceImpl.class.getDeclaredMethod("resolveChatCompletionsUrl", String.class);
        method.setAccessible(true);

        Object result = method.invoke(new AiAdviceServiceImpl(), "https://api.deepseek.com");

        assertEquals("https://api.deepseek.com/chat/completions", result);
    }

    @Test
    void preservesExplicitChatCompletionsUrl() throws Exception {
        Method method = AiAdviceServiceImpl.class.getDeclaredMethod("resolveChatCompletionsUrl", String.class);
        method.setAccessible(true);

        Object result = method.invoke(new AiAdviceServiceImpl(), "https://api.deepseek.com/chat/completions");

        assertEquals("https://api.deepseek.com/chat/completions", result);
    }
}
