package com.nini.niaicodeking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot应用程序的主启动类
 * 使用@SpringBootApplication注解标记这是一个Spring Boot应用
 */
@SpringBootApplication
@MapperScan("com.nini.niaicodeking.mapper")
public class NiAiCodeKingApplication {

    /**
     * 程序的入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 使用SpringApplication.run()方法启动Spring Boot应用
        SpringApplication.run(NiAiCodeKingApplication.class, args);
    }

}
