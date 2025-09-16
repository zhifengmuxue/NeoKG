package top.zfmx.neokgbackend.config;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

/**
 * @author lima
 */
@Configuration
public class TikaConfiguration {

    @Bean
    public Tika tika() throws Exception {
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("tika-config.xml");

        TikaConfig tikaConfig;
        if (inputStream != null) {
            tikaConfig = new TikaConfig(inputStream);
        } else {
            tikaConfig = TikaConfig.getDefaultConfig();
        }

        Detector detector = tikaConfig.getDetector();
        Parser parser = new AutoDetectParser(tikaConfig);

        return new Tika(detector, parser);
    }
}
