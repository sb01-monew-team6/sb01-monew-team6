package com.sprint.part3.sb01_monew_team6.repository.notification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.part3.sb01_monew_team6.entity.Notification;
import com.sprint.part3.sb01_monew_team6.entity.QNotification;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<Notification> findAllByUserId(Long userId, Instant cursor, Instant after, Pageable pageable) {

		QNotification notification = QNotification.notification;

		List<Notification> content = queryFactory
			.selectFrom(notification)
			.where(
				userIdEq(userId),
				notification.confirmed.isFalse(),
				createdAtBefore(cursor),
				createdAtBefore(after)
			)
			.orderBy(getOrderSpecifier(pageable.getSort()).toArray(new OrderSpecifier[0]))
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = content.size() > pageable.getPageSize();

		if (hasNext) {
			content.remove(content.size() - 1);
		}

		return new SliceImpl<>(content, pageable, hasNext);
	}

	private List<OrderSpecifier<?>> getOrderSpecifier(Sort sort) {
		QNotification notification = QNotification.notification;
		List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

		for (Sort.Order order : sort) {
			Order direction = order.isAscending() ? Order.ASC : Order.DESC;

			if (order.getProperty().equals("createdAt")) {
				orderSpecifiers.add(new OrderSpecifier<>(direction, notification.createdAt));
			}
		}

		return orderSpecifiers;
	}

	private BooleanExpression userIdEq(Long userId) {
		return userId != null ? QNotification.notification.user.id.eq(userId) : null;
	}

	private BooleanExpression createdAtBefore(Instant createdAt) {
		return createdAt != null ? QNotification.notification.createdAt.lt(createdAt) : null;
	}
}
