package top.zfmx.neokgbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NeoKG API")
                        .version("1.0.0")
                        .description("基于知识库的知识图谱构建工具"));
    }
}
