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
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import com.sprint.part3.sb01_monew_team6.config.TestDataJpaConfig;
import com.sprint.part3.sb01_monew_team6.entity.Notification;
import com.sprint.part3.sb01_monew_team6.entity.User;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("test")
@Import(TestDataJpaConfig.class)
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

		Long userId = user.getId();
		Instant createdAt = Instant.now();
		Pageable pageable = PageRequest.of(0, 50, DESC, "createdAt");

		//when
		Slice<Notification> notifications = notificationRepository.findAllByUserId(userId, createdAt, createdAt, pageable);

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

		Long userId = user.getId();

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

		Long userId = user.getId();

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

		Long userId = user.getId();

	    //when
		notificationRepository.updateAllByUserId(userId);

	    //then
		List<Notification> all = notificationRepository.findAll();
		assertThat(all).allMatch(Notification::isConfirmed);
	}

	@Test
	@DisplayName("updateByUserId 정상 호출 시 정상 값 반환")
	public void updateByUserIdSuccessfully() throws Exception {
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

		Long userId = user.getId();
		Long notificationId = notification.getId();

	    //when
		notificationRepository.updateByUserId(userId, notificationId);

	    //then
		Notification found = notificationRepository.findById(notificationId).orElse(null);
		assertThat(found).isNotNull();
		assertThat(found.isConfirmed()).isTrue();
	}

	@Test
	@DisplayName("deleteAll 정상 호출 시 정상 값 반환")
	public void deleteAllSuccessfully() throws Exception {
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

	    //when
		notificationRepository.deleteAllOlderThanWeek(
			Instant.parse("9999-04-22T00:00:00Z"),
			Limit.of(1)
		);

	    //then
		List<Notification> found = notificationRepository.findAll();
		assertThat(found.isEmpty()).isTrue();
	}
}