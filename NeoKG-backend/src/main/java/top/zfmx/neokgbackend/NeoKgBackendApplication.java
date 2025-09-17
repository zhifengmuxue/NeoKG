package top.zfmx.neokgbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lima
 */
@SpringBootApplication
@MapperScan("top.zfmx.neokgbackend.mapper")
public class NeoKgBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeoKgBackendApplication.class, args);
    }

}
