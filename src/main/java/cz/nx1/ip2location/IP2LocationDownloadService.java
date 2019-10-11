package cz.nx1.ip2location;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for downloading IP2Location dumps.
 *
 * @author stuchl4n3k
 * @since 2018-05-15
 */
@Service
@Slf4j
public class IP2LocationDownloadService {

    private static final String FILE_NAME_DATE_PATTERN = "yyyyMMdd-HHmmss.SSSS";

    private final HttpClient httpClient;

    private final String downloadToken;

    public IP2LocationDownloadService(HttpClient httpClient, @Value("${ip2location.download.token}") String downloadToken) {
        this.httpClient = httpClient;
        this.downloadToken = downloadToken;
    }

    /**
     * Prepares a download of the latest DB of a given {@code dbType} from IP2Location API.
     */
    public IP2LocationDownload download(String dbType) {
        if (!dbType.matches("(DB|PX)[0-9]{1,2}")) {
            throw new IllegalArgumentException(String.format("Invalid database type requested: '%s'.", dbType));
        }
        return new IP2LocationDownload(dbType);
    }

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    protected class IP2LocationDownload {

        private final String dbType;

        /**
         * Downloads the latest DB from IP2Location API to a file in a given {@code downloadDir} and returns it.
         * <p>
         * The downloaded dump file is named using a following pattern:
         * {@code "IP2LOCATION_TYPE_yyyyMMdd-HHmmss.SSSS.csv"}.
         * If the file already exists, it is replaced.
         * </p>
         *
         * @throws IOException if an I/O exception occurs
         */
        public File to(Path downloadDir) throws IOException {
            File destinationDir = downloadDir.toFile();
            if (destinationDir.exists()) {
                if (!destinationDir.isDirectory()) {
                    throw new IllegalArgumentException(String.format("Given dir '%s' is not a directory.", destinationDir));
                } else if (!destinationDir.canWrite()) {
                    throw new IllegalArgumentException(String.format("Given dir '%s' is not writable.", destinationDir));
                }
            } else if ( !destinationDir.mkdirs()) {
                throw new IllegalArgumentException(String.format("Could not create dir '%s'.", destinationDir));
            }

            File destination = createDestinationFile(dbType, destinationDir);
            if (destination.exists() && (!destination.canWrite() || !destination.delete())) {
                throw new IllegalArgumentException(String.format("Could not delete file '%s'.", destination));
            }

            LOG.info("Downloading IP2Location '{}' to '{}'.", dbType, destination.getAbsolutePath());

            URL dumpUrl = new URL("https://www.ip2location.com/download?token=" + downloadToken + "&file=" + dbType);
            httpClient.download(dumpUrl, destination);

            // Check what we've received.
            if (destination.length() == 0) {
                // Maybe just try again later.
                throw new IllegalStateException("Downloaded dump is empty.");
            }

            return destination;
        }

        protected File createDestinationFile(String dbType, File parentDir) {
            SimpleDateFormat fileNameFormat = new SimpleDateFormat(FILE_NAME_DATE_PATTERN);
            String fileName = String.format("IP2LOCATION_%s_%s.zip", dbType, fileNameFormat.format(new Date()));
            return new File(parentDir, fileName);
        }
    }
}
