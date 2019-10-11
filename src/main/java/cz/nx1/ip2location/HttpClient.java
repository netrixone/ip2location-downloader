package cz.nx1.ip2location;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * Very simple HTTP client.
 *
 * @author stuchl4n3k
 * @since 2018-05-15
 */
@Component
public class HttpClient {

    public void download(URL source, Path destination) throws IOException {
        try (InputStream in = source.openStream();
             OutputStream out = new BufferedOutputStream(new FileOutputStream(destination.toFile()))) {
            StreamUtils.copy(in, out);
        }
    }

}
