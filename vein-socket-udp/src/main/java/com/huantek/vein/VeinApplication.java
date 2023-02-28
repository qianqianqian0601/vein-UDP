package com.huantek.vein;

import com.huantek.vein.util.LostFrame;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableAsync
public class VeinApplication extends SpringBootServletInitializer {
    public static void main(String[] args) throws IOException {
        //LostFrame.logOut();
        SpringApplication.run(VeinApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(VeinApplication.class);
    }
}
