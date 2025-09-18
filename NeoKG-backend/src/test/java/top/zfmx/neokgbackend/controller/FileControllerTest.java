package top.zfmx.neokgbackend.controller;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author li ma
 * @version 0.0.1
 **/
@SpringBootTest
public class FileControllerTest {

    @Autowired
    private Tika tika;

    @Test
    public void test() throws TikaException, IOException {
        Path path = Paths.get("/Users/lima/code/project/NeoKG/test-file/test.word");
        File file = path.toFile();
        String parse = tika.parseToString(file);
        System.out.println(parse);
    }
}
