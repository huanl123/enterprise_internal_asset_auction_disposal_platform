package com.waidp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 企业废旧资产内部拍卖与处置平台 - 主启动类
 */
@SpringBootApplication
public class WaidpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WaidpBackendApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("企业废旧资产内部拍卖与处置平台启动成功！");
        System.out.println("访问地址: http://localhost:8081");
        System.out.println("========================================\n");
    }
}
