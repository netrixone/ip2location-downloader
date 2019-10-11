# IP2Location Downloader

**A microservice that regularly downloads IP2Location dump files with a given period.**

## Build from sources

**Requirements:**

- [x] JDK 1.8
- [x] Maven 3.3+

```bash
mvn clean install
```

## Basic usage

**Requirements:**

- [x] JRE 1.8
- [x] IP2Location download key (token)

```bash
$ IP2LOCATION_DOWNLOAD_DIR=/tmp \
  IP2LOCATION_DOWNLOAD_PERIOD=P1D \
  IP2LOCATION_DOWNLOAD_TYPE=DB1 \
  IP2LOCATION_DOWNLOAD_TOKEN=1234-4567-abc \
  java -jar target/ip2location-1.0-SNAPSHOT.jar
```

### Configuration properties

- `IP2LOCATION_DOWNLOAD_DIR [string]` - a dir where the dumps will get downloaded to. If the dir does not exist,
an attempt will be made to create it.
- `IP2LOCATION_DOWNLOAD_PERIOD [string|number]` - time period for download to start in milliseconds or 
a [java.time.Duration compliant](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html?is-external=true#parse-java.lang.CharSequence-)
- `IP2LOCATION_DOWNLOAD_TYPE [string]` - type of the [IP2Location database](https://www.ip2location.com/database) to download
- `IP2LOCATION_DOWNLOAD_TOKEN [string]` - IP2Location download token (aka. API key)
