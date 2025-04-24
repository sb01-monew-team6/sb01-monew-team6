package com.sprint.part3.sb01_monew_team6.dto.news;

import java.time.ZonedDateTime;

public record ExternalNewsItem(
    String provider,
    String originalLink,
    String link,
    String title,
    ZonedDateTime pubDate,
    String description
    ) {

}
