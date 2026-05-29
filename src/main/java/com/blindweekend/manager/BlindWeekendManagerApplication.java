package com.blindweekend.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 不期周末管理后台 - 启动类
 *
 * @author BlindWeekend Team
 * @version 1.0.0
 */
@SpringBootApplication
public class BlindWeekendManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlindWeekendManagerApplication.class, args);
        System.out.println("========================================");
        System.out.println("  不期周末管理后台启动成功!");
        System.out.println("  本地访问地址: http://localhost:8080/login.html");
        System.out.println("========================================");
    }
}
