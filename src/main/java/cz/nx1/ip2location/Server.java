package cz.nx1.ip2location;

import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JndiDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication(exclude = {
    JmxAutoConfiguration.class,
    SpringApplicationAdminJmxAutoConfiguration.class,
    JndiDataSourceAutoConfiguration.class,
})
@EnableScheduling
@EnableRetry
@RequiredArgsConstructor
@Slf4j
public class Server {

    private final IP2LocationDownloadService downloaderService;

    @Value("${ip2location.download.type}")
    private String downloadType;

    @Value("${ip2location.download.period}")
    private DownloadPeriod downloadPeriod;

    @Value("${ip2location.download.dir}")
    private Path downloadDir;

    /**
     * Downloads the latest IP2Location DB file.
     */
    @Scheduled(cron = "${ip2location.download.cron:0 * * * * *}", zone = "UTC")
    @Retryable(backoff = @Backoff(delay = 60000, multiplier = 5))
    public void runDownload() throws IOException {
        if (downloaderService.isDownloaded(downloadType).in(downloadDir).during(downloadPeriod)) {
            LOG.debug("Download is not needed.");
            return;
        }

        downloaderService.download(downloadType).to(downloadDir);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Server.class)
            .web(WebApplicationType.NONE)
            .run(args);
    }
}
