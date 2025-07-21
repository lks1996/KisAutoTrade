package com.example.boot.KisAutoTrade.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob    // @Lob 어노테이션을 추가하여 긴 문자열을 데이터베이스에 저장할 수 있게 합니다.
    private String accessToken;
    private LocalDateTime expiration;
    private String type;
}
