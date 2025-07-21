package com.example.boot.KisAutoTrade.Mapper;

import com.example.boot.KisAutoTrade.DTO.TokenRes;
import com.example.boot.KisAutoTrade.Entity.Token;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TokenMapper {
    public Token toToken(TokenRes tokenRes) {
        return Token.builder()
                .accessToken(tokenRes.accessToken())
                .expiration(tokenRes.accessTokenTokenExpired())
                .build();
    }
}
