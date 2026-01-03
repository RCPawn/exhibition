package com.ruoyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RuoYiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuoYiApplication.class, args);
        System.out.println("\n" +
                "--------------------------------------------------\n" +
                "🎉🎉🎉 白族非遗智能展示与管理平台 启动成功 🎉🎉🎉\n" +
                " >> URL     : http://localhost:8080\n" +
                " >> NOTICE  : 系统进入实时监控状态\n" +
                " ---------------------------------------------------"
        );
    }
}
