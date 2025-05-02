package com.sprint.part3.sb01_monew_team6.storage.s3;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3LogStorage {

	private final S3Client s3Client;
	private final String bucket;

	@Autowired
	public S3LogStorage(
		S3Client s3Client,
		@Value("${storage.s3.bucket}") String bucket
	) {
		this.s3Client = s3Client;
		this.bucket = bucket;
	}

	public void uploadZip(Path zipPath) {
		String key = zipPath.getFileName().toString();

		PutObjectRequest request = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build();

		s3Client.putObject(request, RequestBody.fromFile(zipPath));
	}
}
