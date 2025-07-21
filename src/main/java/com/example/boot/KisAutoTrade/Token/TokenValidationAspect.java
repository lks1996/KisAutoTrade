package com.example.boot.KisAutoTrade.Token;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TokenValidationAspect {
    private final TokenHolder tokenHolder;

    @Before("@annotation(RequireValidToken)")
    public void ensureValidToken() {
        tokenHolder.getAccessToken();
    }
}
