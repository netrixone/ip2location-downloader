package cz.nx1.ip2location;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 * Unit test of {@link HttpClient}.
 *
 * @author petr.stuchlik
 * @since 2018-09-24
 */
@RunWith(SpringRunner.class)
public class HttpClientTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void whenDownload_thenNoExceptionIsThrown() throws Exception {
        // Given:
        File sourceFile = tempDir.newFile("source");
        Files.write(sourceFile.toPath(), "dummy content".getBytes());

        URL source = sourceFile.toURI().toURL();
        File destination = tempDir.newFile("destination");

        // When:
        HttpClient httpClient = new HttpClient();
        httpClient.download(source, destination);

        // Then:
        assertThat(Files.readAllLines(destination.toPath()), contains("dummy content"));
    }
}
