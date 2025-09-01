package hs.kr.entrydsm.application.global.config

import hs.kr.entrydsm.domain.calculator.aggregates.Calculator
import hs.kr.entrydsm.domain.evaluator.aggregates.ExpressionEvaluator
import hs.kr.entrydsm.domain.lexer.aggregates.LexerAggregate
import hs.kr.entrydsm.domain.parser.aggregates.LRParser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainConfig {
    
    @Bean
    fun calculator(): Calculator {
        return Calculator.createDefault()
    }
    
    @Bean
    fun expressionEvaluator(): ExpressionEvaluator {
        return ExpressionEvaluator.create()
    }
    
    @Bean
    fun lexerAggregate(): LexerAggregate {
        return LexerAggregate()
    }
    
    @Bean
    fun lrParser(): LRParser {
        return LRParser.createDefault()
    }
}