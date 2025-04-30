package com.sprint.part3.sb01_monew_team6.storage.s3;

import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Testcontainers
class S3LogStorageTest {

	private static final String BUCKET = "monew";

	@Container
	static final S3MockContainer S3_MOCK = new S3MockContainer("4.1.1")
		.withInitialBuckets(BUCKET);

	private S3Client s3Client;

	@BeforeEach
	void setUp() {

		S3Configuration config = S3Configuration.builder()
			.pathStyleAccessEnabled(true)
			.build();

		s3Client = S3Client.builder()
			.endpointOverride(URI.create(S3_MOCK.getHttpEndpoint()))
			.serviceConfiguration(config)
			.build();
	}

	@Test
	@DisplayName("S3에 로그를 정상적으로 적재한다")
	void uploadLogSuccessfully() {
		//given
		String content = "25-04-29 18:27:16.421 [Thread-5] INFO  S3Test [ | ] - {}";

		//when
		s3Client.putObject(
			PutObjectRequest.builder()
				.bucket(BUCKET)
				.key("application-2025-04-29.log")
				.build(),
			RequestBody.fromString(content, UTF_8)
		);

		//then
		ResponseBytes<GetObjectResponse> response = s3Client.getObject(GetObjectRequest.builder()
				.bucket(BUCKET)
				.key("application-2025-04-29.log")
				.build(),
			ResponseTransformer.toBytes()
		);

		assertThat(response.asUtf8String()).isEqualTo(content);
	}
}