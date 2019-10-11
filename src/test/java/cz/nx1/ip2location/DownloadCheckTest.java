package cz.nx1.ip2location;

import cz.nx1.ip2location.IP2LocationDownloadService.DownloadCheck;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.write;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit test of {@link DownloadCheck}.
 *
 * @author petr.stuchlik
 * @since 2018-09-24
 */
@RunWith(SpringRunner.class)
public class DownloadCheckTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void whenFileExistsInDownloadDir_duringLastHour_thenReturnsTrue() throws Exception {
        // Given:
        HttpClient httpClient = mock(HttpClient.class);
        Path downloadDir = tempDir.newFolder("i2l-test").toPath();
        write(downloadDir.resolve("IP2LOCATION_DB1_" + now() + ".zip"), "dummy content".getBytes());

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        boolean result = service.isDownloaded("DB1").in(downloadDir).during(DownloadPeriod.HOUR);

        // Then:
        assertThat(result, is(true));
    }

    @Test
    public void whenFileNotExistsInDownloadDir_duringLastHour_thenReturnsFalse() throws Exception {
        // Given:
        HttpClient httpClient = mock(HttpClient.class);
        Path downloadDir = tempDir.newFolder("i2l-test").toPath();
        write(downloadDir.resolve("IP2LOCATION_DB1_" + lastYear() + ".zip"), "dummy content".getBytes());

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        boolean result = service.isDownloaded("DB1").in(downloadDir).during(DownloadPeriod.HOUR);

        // Then:
        assertThat(result, is(false));
    }

    @Test
    public void whenFileExistsInDownloadDir_duringLastDay_thenReturnsTrue() throws Exception {
        // Given:
        HttpClient httpClient = mock(HttpClient.class);
        Path downloadDir = tempDir.newFolder("i2l-test").toPath();
        write(downloadDir.resolve("IP2LOCATION_DB1_" + now() + ".zip"), "dummy content".getBytes());

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        boolean result = service.isDownloaded("DB1").in(downloadDir).during(DownloadPeriod.DAY);

        // Then:
        assertThat(result, is(true));
    }

    @Test
    public void whenFileNotExistsInDownloadDir_duringLastDay_thenReturnsFalse() throws Exception {
        // Given:
        HttpClient httpClient = mock(HttpClient.class);
        Path downloadDir = tempDir.newFolder("i2l-test").toPath();
        write(downloadDir.resolve("IP2LOCATION_DB1_" + lastYear() + ".zip"), "dummy content".getBytes());

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        boolean result = service.isDownloaded("DB1").in(downloadDir).during(DownloadPeriod.DAY);

        // Then:
        assertThat(result, is(false));
    }

    @Test
    public void whenFileExistsInDownloadDir_duringLastMonth_thenReturnsTrue() throws Exception {
        // Given:
        HttpClient httpClient = mock(HttpClient.class);
        Path downloadDir = tempDir.newFolder("i2l-test").toPath();
        write(downloadDir.resolve("IP2LOCATION_DB1_" + now() + ".zip"), "dummy content".getBytes());

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        boolean result = service.isDownloaded("DB1").in(downloadDir).during(DownloadPeriod.MONTH);

        // Then:
        assertThat(result, is(true));
    }

    @Test
    public void whenFileNotExistsInDownloadDir_duringLastMonth_thenReturnsFalse() throws Exception {
        // Given:
        HttpClient httpClient = mock(HttpClient.class);
        Path downloadDir = tempDir.newFolder("i2l-test").toPath();
        write(downloadDir.resolve("IP2LOCATION_DB1_" + lastYear() + ".zip"), "dummy content".getBytes());

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        boolean result = service.isDownloaded("DB1").in(downloadDir).during(DownloadPeriod.MONTH);

        // Then:
        assertThat(result, is(false));
    }

    private String now() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HH");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    private String lastYear() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HH");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(
            new Date(Instant.now().atOffset(ZoneOffset.UTC).minus(1, ChronoUnit.YEARS).toInstant().toEpochMilli())
        );
    }
}
