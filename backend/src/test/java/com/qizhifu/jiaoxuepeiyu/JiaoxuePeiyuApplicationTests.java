package com.qizhifu.jiaoxuepeiyu;

import com.qizhifu.jiaoxuepeiyu.bootstrap.BootstrapAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class JiaoxuePeiyuApplicationTests {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private BootstrapAdminService bootstrapAdminService;

    @Test
    void contextLoads() {
    }
}
