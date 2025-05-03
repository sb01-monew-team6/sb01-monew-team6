package com.sprint.part3.sb01_monew_team6.scheduler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sprint.part3.sb01_monew_team6.storage.s3.S3LogStorage;

@Component
public class LogUploadScheduler {

  private final S3LogStorage storage;

  private final String logDir;

  private static final int LOG_DATE_BEGIN_INDEX = 12;
  private static final int LOG_DATE_END_INDEX = 22;

  @Autowired
  public LogUploadScheduler(
      S3LogStorage storage,
      @Value("${log.dir}") String logDir
  ) {
    this.storage = storage;
    this.logDir = logDir;
  }

  @Scheduled(cron = "0 0 0 * * Mon")
  public void uploadLogEveryWeek() throws IOException {
    Path dir = Paths.get(logDir);
    LocalDate today = LocalDate.now();
    List<Path> toZip = preprocessCompress(dir, today);

    if (toZip.isEmpty())
      return;

    String zipName = String.format("logs-%s-to-%s.zip",
        today.minusDays(7), today.minusDays(1));
    Path zipPath = dir.resolve(zipName);

    processCompress(zipPath, toZip);

    storage.uploadZip(zipPath);
  }

  private static void processCompress(Path zipPath, List<Path> toZip) throws IOException {
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
      for (Path file : toZip) {
        ZipEntry entry = new ZipEntry(file.getFileName().toString());
        zos.putNextEntry(entry);
        Files.copy(file, zos);
        zos.closeEntry();
      }
    }
  }

  private List<Path> preprocessCompress(Path dir, LocalDate today) throws IOException {
    List<Path> toZip;
    try (Stream<Path> stream = Files.list(dir)) {
      toZip = stream.filter(path -> {
            String name = path.getFileName().toString();
            if (name.length() < LOG_DATE_END_INDEX)
              return false;
            String date = name.substring(LOG_DATE_BEGIN_INDEX, LOG_DATE_END_INDEX);
            return IntStream.rangeClosed(1, 7)
                .anyMatch(i -> date.equals(today.minusDays(i).toString()));
          })
          .toList();
    }
    return toZip;
  }
}