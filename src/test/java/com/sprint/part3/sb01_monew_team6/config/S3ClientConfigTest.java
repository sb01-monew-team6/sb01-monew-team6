package com.sprint.part3.sb01_monew_team6.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.AwsServiceClientConfiguration;
import software.amazon.awssdk.core.SdkServiceClientConfiguration;
import software.amazon.awssdk.services.s3.S3Client;

class S3ClientConfigTest {
	private S3ClientConfig config;

	@BeforeEach
	void setUp() {
		config = new S3ClientConfig();

		ReflectionTestUtils.setField(config, "accessKey", "access-key");
		ReflectionTestUtils.setField(config, "secretKey", "secret-key");
		ReflectionTestUtils.setField(config, "region", "region");
	}

	@Test
	@DisplayName("")
	void whenInvoking_s3Client_thenItBuildsNonNullClient() {
		//when & then
		S3Client client = config.s3Client();
		assertThat(client).isNotNull();

		SdkServiceClientConfiguration sdkConfig =
			ReflectionTestUtils.invokeMethod(client, "serviceClientConfiguration");
		assertThat(sdkConfig).isNotNull();

		AwsServiceClientConfiguration awsConfig = (AwsServiceClientConfiguration)sdkConfig;
		assertThat(awsConfig).isNotNull();

		AwsCredentialsProvider provider = (AwsCredentialsProvider)awsConfig.credentialsProvider();
		assertThat(provider).isNotNull();

		AwsBasicCredentials credentials = (AwsBasicCredentials)provider.resolveCredentials();
		assertThat(credentials.accessKeyId()).isEqualTo("access-key");
		assertThat(credentials.secretAccessKey()).isEqualTo("secret-key");
		assertThat(awsConfig.region().toString()).isEqualTo("region");
	}
}