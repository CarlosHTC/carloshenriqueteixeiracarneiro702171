package br.com.avaliacao.apimusicmanagement;

import br.com.avaliacao.apimusicmanagement.config.security.AppSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableConfigurationProperties(AppSecurityProperties.class)
class ApimusicmanagementApplicationTests {

    @Test
    void contextLoads() {
    }

}
