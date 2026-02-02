package br.com.avaliacao.apimusicmanagement;

import br.com.avaliacao.apimusicmanagement.config.integration.RegionalIntegrationProperties;
import br.com.avaliacao.apimusicmanagement.config.security.AppSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppSecurityProperties.class, RegionalIntegrationProperties.class})
public class ApimusicmanagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApimusicmanagementApplication.class, args);
    }

}
