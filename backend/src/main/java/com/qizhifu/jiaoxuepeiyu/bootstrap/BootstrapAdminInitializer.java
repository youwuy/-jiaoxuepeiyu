package com.qizhifu.jiaoxuepeiyu.bootstrap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

    private final BootstrapAdminService service;
    private final String username;
    private final String password;
    private final String realName;
    private final String phone;

    public BootstrapAdminInitializer(BootstrapAdminService service,
                                     @Value("${app.bootstrap.admin.username:}") String username,
                                     @Value("${app.bootstrap.admin.password:}") String password,
                                     @Value("${app.bootstrap.admin.real-name:}") String realName,
                                     @Value("${app.bootstrap.admin.phone:}") String phone) {
        this.service = service;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.phone = phone;
    }

    @Override
    public void run(ApplicationArguments args) {
        service.initialize(new BootstrapAdminCommand(username, password, realName, phone));
    }
}
