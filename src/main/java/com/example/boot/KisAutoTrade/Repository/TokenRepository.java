package com.example.boot.KisAutoTrade.Repository;

import com.example.boot.KisAutoTrade.Entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Transactional(readOnly = true)
    Optional<Token> findTopByTypeOrderByIdDesc(String type);
}
