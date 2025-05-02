package com.sprint.part3.sb01_monew_team6.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.part3.sb01_monew_team6.storage.s3.S3LogStorage;

@ExtendWith(MockitoExtension.class)
class LogUploadSchedulerTest {

	@TempDir
	private Path tempDir;

	@Mock
	private S3LogStorage storage;
	@InjectMocks
	private LogUploadScheduler scheduler;

	@BeforeEach
	void setUp() {
		scheduler = new LogUploadScheduler(
			storage,
			tempDir.toString()
		);
	}

	@Test
	@DisplayName("uploadLogEveryWeekSuccessfully 정상 호출 시 정상 동작한다")
	void uploadLogEveryWeekSuccessfully() throws Exception {
		//given
		LocalDate now = LocalDate.now();
		for (int i = 1; i <= 7; ++i) {
			Path file = tempDir.resolve("application-" + now.minusDays(i) + ".log");
			Files.write(file, ("content" + i).getBytes());
		}

		doNothing().when(storage).uploadZip(any(Path.class));

		//when
		scheduler.uploadLogEveryWeek();

		//then
		String zipName = String.format("logs-%s-to-%s.zip",
			now.minusDays(7), now.minusDays(1));
		Path zipPath = tempDir.resolve(zipName);
		assertThat(Files.exists(zipPath)).isTrue();

		try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
			assertThat(zipFile.size()).isEqualTo(7);
			for (int i = 1; i <= 7; ++i) {
				String entryName = "application-" + now.minusDays(i) + ".log";
				assertThat(zipFile.getEntry(entryName)).isNotNull();
			}
		}

		verify(storage, times(1)).uploadZip(zipPath);
	}
}