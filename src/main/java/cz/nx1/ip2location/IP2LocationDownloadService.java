package cz.nx1.ip2location;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isWritable;
import static java.nio.file.Files.list;
import static java.nio.file.Files.size;

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

    /**
     * Creates a new IP2LocationDownloadService using a given {@code httpClient}
     * and {@code downloadToken}.
     */
    public IP2LocationDownloadService(HttpClient httpClient, @Value("${ip2location.download.token}") String downloadToken) {
        this.httpClient = httpClient;
        this.downloadToken = downloadToken;
    }

    /**
     * Checks if an IP2Location DB of a given {@code type} has been downloaded.
     */
    public DownloadCheckBuilder isDownloaded(String type) {
        return new DownloadCheck(type);
    }

    /**
     * Prepares a download of the latest DB of a given {@code type} from IP2Location API.
     */
    public Download download(String type) {
        return new Download(type);
    }

    /**
     * Builder step interface for a download check.
     */
    protected interface DownloadCheckBuilder {

        /**
         * Checks if an IP2Location DB has been downloaded in a given {@code dir}.
         */
        DownloadCheck in(Path dir);
    }

    /**
     * Download check implementation.
     */
    protected class DownloadCheck implements DownloadCheckBuilder {

        private String downloadType;
        private Path downloadDir;

        protected DownloadCheck(String type) {
            this.downloadType = type;
        }

        @Override
        public DownloadCheck in(Path dir) {
            this.downloadDir = dir;
            return this;
        }

        /**
         * Checks if an IP2Location DB has been downloaded during a given {@code period}.
         */
        public boolean during(DownloadPeriod period) {
            boolean recentFileExists;

            SimpleDateFormat dateFormat;
            switch (period) {
                case HOUR:
                    dateFormat = new SimpleDateFormat("yyyyMMdd-HH");
                    break;

                case DAY:
                    dateFormat = new SimpleDateFormat("yyyyMMdd-");
                    break;

                case MONTH:
                    dateFormat = new SimpleDateFormat("yyyyMM");
                    break;

                default:
                    throw new IllegalStateException("Download period '%s' is not supported.");
            }
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            String filePrefix = String.format("IP2LOCATION_%s_%s", downloadType, dateFormat.format(new Date()));
            String fileSuffix = ".zip";

            try {
                recentFileExists = list(downloadDir)
                    .filter(file -> file.getFileName().toString().startsWith(filePrefix))
                    .filter(file -> file.getFileName().toString().endsWith(fileSuffix))
                    .anyMatch(entry -> entry.toFile().length() > 0);
            } catch (IOException e) {
                // Cannot read the dir: file does not exists -> eat.
                recentFileExists = false;
            }

            return recentFileExists;
        }
    }

    /**
     * Download implementation.
     */
    protected class Download {

        private final String downloadType;

        public Download(String type) {
            validateDownloadType(type);
            this.downloadType = type;
        }

        /**
         * Downloads the latest DB from IP2Location API to a file in a given {@code dir} and returns it.
         * <p>
         * The downloaded dump file is named using a following pattern:
         * {@code "IP2LOCATION_TYPE_yyyyMMdd-HHmmss.SSSS.csv"}.
         * If the file already exists, it is replaced.
         * </p>
         *
         * @throws IOException if an I/O exception occurs
         */
        public Path to(Path dir) throws IOException {
            if (exists(dir)) {
                if (!isDirectory(dir)) {
                    throw new IllegalArgumentException(String.format("Given dir '%s' is not a directory.", dir));
                } else if (!isWritable(dir)) {
                    throw new IllegalArgumentException(String.format("Given dir '%s' is not writable.", dir));
                }
            } else if (!dir.toFile().mkdirs()) {
                throw new IllegalArgumentException(String.format("Could not create dir '%s'.", dir));
            }

            Path destination = createDestinationFile(downloadType, dir);
            if (exists(destination) && !deleteIfExists(destination)) {
                throw new IllegalArgumentException(String.format("Could not delete file '%s'.", destination));
            }

            LOG.info("Downloading IP2Location '{}' to '{}'.", downloadType, destination);

            URL dumpUrl = new URL("https://www.ip2location.com/download?token=" + downloadToken + "&file=" + downloadType);
            httpClient.download(dumpUrl, destination);

            // Check what we've received.
            if (!exists(destination) || size(destination) == 0) {
                // Maybe just try again later.
                throw new IllegalStateException("Downloaded dump is empty.");
            }

            LOG.info("Download finished ({} B).", size(destination));

            return destination;
        }

        protected void validateDownloadType(String downloadType) {
            if (!downloadType.matches("(DB|PX)[0-9]{1,2}")) {
                throw new IllegalArgumentException(String.format("Invalid database type requested: '%s'.", downloadType));
            }
        }

        protected Path createDestinationFile(String dbType, Path parentDir) {
            SimpleDateFormat fileNameFormat = new SimpleDateFormat(FILE_NAME_DATE_PATTERN);
            fileNameFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String fileName = String.format("IP2LOCATION_%s_%s.zip", dbType, fileNameFormat.format(new Date()));
            return parentDir.resolve(fileName);
        }
    }
}
