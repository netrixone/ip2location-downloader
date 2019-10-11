[![Build Status](https://img.shields.io/circleci/build/github/netrixone/ip2location-downloader?style=flat-square)](https://circleci.com/gh/netrixone/ip2location-downloader)
[![Code Coverage](https://img.shields.io/codecov/c/github/netrixone/ip2location-downloader?style=flat-square)](https://codecov.io/gh/netrixone/ip2location-downloader)
[![MIT License](https://img.shields.io/github/license/netrixone/ip2location-downloader?style=flat-square)](https://github.com/netrixone/ip2location-downloader/blob/master/LICENSE)

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
  IP2LOCATION_DOWNLOAD_FREQUENCY=MONTH \
  IP2LOCATION_DOWNLOAD_TYPE=DB1 \
  IP2LOCATION_DOWNLOAD_TOKEN=1234-4567-abc \
  java -jar target/ip2location-1.0-SNAPSHOT.jar
```

### Configuration properties

- `IP2LOCATION_DOWNLOAD_DIR [string]` - a dir where the dumps will get downloaded to. If the dir does not exist,
an attempt will be made to create it.
- `IP2LOCATION_DOWNLOAD_PERIOD [HOUR|DAY|MOTH]` - the frequency of download
- `IP2LOCATION_DOWNLOAD_TYPE [string]` - type of the [IP2Location database](https://www.ip2location.com/database) to download
- `IP2LOCATION_DOWNLOAD_TOKEN [string]` - IP2Location download token (aka. API key)
- `IP2LOCATION_DOWNLOAD_CRON [string|number] = 0 * * * * *` - [cron-like expression](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html#cron--) (in UTC time zone) when the download check kicks in
