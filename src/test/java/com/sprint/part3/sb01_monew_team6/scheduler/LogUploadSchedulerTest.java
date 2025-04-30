package com.sprint.part3.sb01_monew_team6.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

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
	Path tempDir;

	@Mock
	private S3LogStorage storage;
	@InjectMocks
	private LogUploadScheduler scheduler;

	@Test
	@DisplayName("")
	void uploadLogEveryWeekSuccessfully() throws Exception {
		//given
		LocalDate now = LocalDate.now();
		for (int i = 1; i <= 7; ++i) {
			Path file = tempDir.resolve("application-" + now.minusDays(i) + ".log");
			Files.write(file, ("content" + i).getBytes());
		}

		//when
		scheduler.uploadLogEveryWeek();

		//then
		String zipName = String.format("logs-%s-to-%s.zip",
			now.minusDays(7), now.minusDays(1));
		Path zipPath = tempDir.resolve(zipName);

		assertThat(Files.exists(zipPath)).isTrue();
		verify(storage, times(1)).uploadZip(zipPath);
	}
}