package cz.nx1.ip2location;

import cz.nx1.ip2location.IP2LocationDownloadService.Download;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.junit4.SpringRunner;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit test of {@link Download}.
 *
 * @author petr.stuchlik
 * @since 2018-09-24
 */
@RunWith(SpringRunner.class)
public class DownloadTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void whenDownloadTo_thenNoExceptionIsThrown() throws Exception {
        // Given:
        HttpClient httpClient = mock(HttpClient.class);
        doAnswer(invocation -> {
            Files.write(invocation.getArgument(1), "dummy file content".getBytes());
            return null;
        }).when(httpClient).download(any(URL.class), any(Path.class));

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        Path result = service.download("DB1").to(tempDir.newFolder("i2l-test").toPath());

        // Then:
        assertThat(result, is(not(nullValue())));
        assertThat(result.toString(), containsString("/i2l-test/IP2LOCATION_DB1_"));
        assertThat(result.toString(), endsWith(".zip"));

        URL expectedURL = new URL("https://www.ip2location.com/download?token=1234&file=DB1");
        ArgumentCaptor<Path> expectedDestination = ArgumentCaptor.forClass(Path.class);
        verify(httpClient).download(eq(expectedURL), expectedDestination.capture());
        assertThat(expectedDestination.getValue().toString(), containsString("/i2l-test/IP2LOCATION_DB1_"));
        assertThat(expectedDestination.getValue().toString(), endsWith(".zip"));
    }

    @Test
    public void whenDownloadedFileDoesNotExist_thenExceptionIsThrown() throws Exception {
        // Given:
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Downloaded dump is empty.");

        HttpClient httpClient = mock(HttpClient.class);

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        Path result = service.download("DB1").to(tempDir.newFolder("i2l-test").toPath());

        // Then:
        thrown.reportMissingExceptionWithMessage("File size check failed: no exception thrown.");
    }

    @Test
    public void whenDownloadedFileIsEmpty_thenExceptionIsThrown() throws Exception {
        // Given:
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Downloaded dump is empty.");

        HttpClient httpClient = mock(HttpClient.class);
        doAnswer(invocation -> {
            Files.createFile(invocation.getArgument(1));
            return null;
        }).when(httpClient).download(any(URL.class), any(Path.class));

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        Path result = service.download("DB1").to(tempDir.newFolder("i2l-test").toPath());

        // Then:
        thrown.reportMissingExceptionWithMessage("File size check failed: no exception thrown.");
    }

    @Test
    public void whenDownloadTypeIsInvalid_thenExceptionIsThrown() throws Exception {
        // Given:
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid database type requested: 'DB 1'.");

        HttpClient httpClient = mock(HttpClient.class);

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        Path result = service.download("DB 1").to(tempDir.newFolder("i2l-test").toPath());

        // Then:
        thrown.reportMissingExceptionWithMessage("DB type check failed: no exception thrown.");
    }

    @Test
    public void whenDestinationDirIsNotDir_thenExceptionIsThrown() throws Exception {
        // Given:
        File downloadDir = tempDir.newFile("i2l-test");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Given dir '" + downloadDir.getAbsolutePath() + "' is not a directory.");

        HttpClient httpClient = mock(HttpClient.class);

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        Path result = service.download("DB1").to(downloadDir.toPath());

        // Then:
        thrown.reportMissingExceptionWithMessage("Download dir check failed: no exception thrown.");
    }

    @Test
    public void whenDestinationDirCannotBeCreated_thenExceptionIsThrown() throws Exception {
        // Given:
        File downloadDirParent = tempDir.newFolder("i2l-test");
        File downloadDir = new File(downloadDirParent, "non-existing");
        assertThat(downloadDirParent.setWritable(false), is(true));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Could not create dir '" + downloadDir.getAbsolutePath() + "'.");

        HttpClient httpClient = mock(HttpClient.class);

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        Path result = service.download("DB1").to(downloadDir.toPath());

        // Then:
        thrown.reportMissingExceptionWithMessage("Download dir check failed: no exception thrown.");
    }

    @Test
    public void whenDestinationDirIsNotWritable_thenExceptionIsThrown() throws Exception {
        // Given:
        File downloadDir = tempDir.newFolder("i2l-test");
        assertThat(downloadDir.setWritable(false), is(true));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Given dir '" + downloadDir.getAbsolutePath() + "' is not writable.");

        HttpClient httpClient = mock(HttpClient.class);

        // When:
        IP2LocationDownloadService service = new IP2LocationDownloadService(httpClient, "1234");
        Path result = service.download("DB1").to(downloadDir.toPath());

        // Then:
        thrown.reportMissingExceptionWithMessage("Download dir check failed: no exception thrown.");
    }
}
