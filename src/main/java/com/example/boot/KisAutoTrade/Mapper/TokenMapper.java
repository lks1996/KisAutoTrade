package com.example.boot.KisAutoTrade.Mapper;

import com.example.boot.KisAutoTrade.DTO.TokenRes;
import com.example.boot.KisAutoTrade.Entity.Token;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class TokenMapper {
    public Token toToken(TokenRes tokenRes, String type) {
        return Token.builder()
                .accessToken(tokenRes.accessToken())
                .expiration(Date.from(tokenRes.accessTokenTokenExpired()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .type(type)
                .build();
    }
}
