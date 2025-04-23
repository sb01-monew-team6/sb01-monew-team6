package com.sprint.part3.sb01_monew_team6.repository.notification;

import static com.sprint.part3.sb01_monew_team6.entity.enums.ResourceType.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.*;
import static org.springframework.data.domain.Sort.Direction.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import com.sprint.part3.sb01_monew_team6.config.TestConfig;
import com.sprint.part3.sb01_monew_team6.entity.Notification;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("test")
@Import(TestConfig.class)
class NotificationRepositoryTest {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private TestEntityManager em;
	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("findAllByUserId 정상 호출 시 정상 slice 반환 ")
	public void findAllByUserIdSuccessfully() throws Exception {
		//given
		User user = new User("email@email.com", "nickname", "123456", false);
		userRepository.save(user);

		Notification notification = Notification.createNotification(
			user,
			"hello",
			COMMENT,
			1L,
			false
		);

		notificationRepository.save(notification);
		em.flush();
		em.clear();

		Long userId = 1L;
		Instant createdAt = Instant.now();
		Pageable pageable = PageRequest.of(0, 50, DESC, "createdAt");

		//when
		Slice<Notification> notifications = notificationRepository.findAllByUserId(userId, createdAt, pageable);

		//then
		assertThat(notifications.getContent().size()).isEqualTo(1);
		assertThat(notifications.getContent().get(0).getContent()).isEqualTo("hello");
		assertThat(notifications.getSize()).isEqualTo(50);
		assertThat(notifications.hasNext()).isFalse();
	}

	@Test
	@DisplayName("countByUserIdAndConfirmedFalse 정상 호출 시 정상 count 반환 ")
	public void countByUserIdAndConfirmedFalseSuccessfully() throws Exception {
		//given
		User user = new User("email@email.com", "nickname", "123456", false);
		userRepository.save(user);

		Notification notification = Notification.createNotification(
			user,
			"hello",
			COMMENT,
			1L,
			false
		);

		notificationRepository.save(notification);
		em.flush();
		em.clear();

		Long userId = 1L;

		//when
		long count = notificationRepository.countByUserIdAndConfirmedFalse(userId);

		//then
		assertThat(count).isEqualTo(1);
	}

	@Test
	@DisplayName("countByUserIdAndConfirmedFalse 호출 시 알림이 없다면 0 반환 ")
	public void getZeroWhenFilteredNotificationNonExistWhileCountByUserIdAndConfirmedFalse() throws Exception {
		//given
		User user = new User("email@email.com", "nickname", "123456", false);
		userRepository.save(user);

		Notification notification = Notification.createNotification(
			user,
			"hello",
			COMMENT,
			1L,
			true
		);

		notificationRepository.save(notification);
		em.flush();
		em.clear();

		Long userId = 1L;

		//when
		long count = notificationRepository.countByUserIdAndConfirmedFalse(userId);

		//then
		assertThat(count).isEqualTo(0);
	}

	@Test
	@DisplayName("updateAllByUserId 정상 호출 시 정상 값 반환")
	public void updateAllByUserIdSuccessfully() throws Exception {
	    //given
		User user = new User("email@email.com", "nickname", "123456", false);
		userRepository.save(user);

		Notification notification = Notification.createNotification(
			user,
			"hello",
			COMMENT,
			1L,
			false
		);

		notificationRepository.save(notification);
		em.flush();
		em.clear();

		Long userId = 1L;

	    //when
		notificationRepository.updateAllByUserId(userId);
		em.flush();
		em.clear();

	    //then
		List<Notification> all = notificationRepository.findAll();
		assertThat(all).allMatch(Notification::isConfirmed);
	}
}