package com.paykernelfun.launcher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan({"com.paykernelfun.launcher.infra.persistence", "com.paykernelfun.launcher.biz.sample.persistence"})
@EnableScheduling
public class PayKernelFunLauncherApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayKernelFunLauncherApplication.class, args);
    }
}
