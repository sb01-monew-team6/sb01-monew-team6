# 👨🏻‍💻 **PART3_6팀**
# 🏦 MoNew : MongoDB 및 PostgreSQL 백업 및 복구 시스템

<br>

[![codecov](https://codecov.io/gh/sb01-monew-team6/sb01-monew-team6/graph/badge.svg?token=SLMOCAHHUK)](https://codecov.io/gh/sb01-monew-team6/sb01-monew-team6)

## **팀원 구성**

| ![Image](https://github.com/user-attachments/assets/fbde43d6-0bc7-41c9-8ffe-c9413dccff00) | ![Image](https://github.com/user-attachments/assets/2565605b-04fe-4efa-9bab-5ff0927275e0) | ![Image](https://github.com/user-attachments/assets/fab2c5da-8c55-49e1-a731-27640e8fe36c) | ![Image](https://github.com/user-attachments/assets/9d9655f4-e839-4b2d-91a7-2a93a1ce418a) | ![Image](https://github.com/user-attachments/assets/06233c96-0a59-4f63-a6bb-a37688d5b3aa) |
|------|-----------------------------------------------------------------------------------------------|--------|------|------|
| **전민기** | **이규석**                                                                                       | **설유일** | **손동혁** | **백승헌** |
| mingi3070@gmail.com | khss8070@gmail.com                                                                                         | tjf7894@gmail.com | sondonghyuk0304@gmail.com | rnsdls1996@gmail.com |
| [github.com/mingi96](https://github.com/mingi96) | [github.com/impmonzz](https://github.com/impmonzz)                                            | [github.com/you1-2](https://github.com/you1-2) | [github.com/sondonghyuk](https://github.com/sondonghyuk) | [github.com/FrogBaek](https://github.com/FrogBaek) |


## **프로젝트 링크**
- ⭐ 프로젝트 배포 : <a href="http://my-alb-1195026143.ap-northeast-2.elb.amazonaws.com">🏦 monew</a>
- 🔗 프로젝트 문서 (다른 모든 프로젝트 링크들) : <a href="https://lateral-polyanthus-1e5.notion.site/Sprint-Team6-1d7b1cbaf4c580dcbd44e02429fed94c">프로젝트 문서</a>

<br>


<br>

## **프로젝트 소개**
### **< 여러 뉴스 API를 기반으로 사용자 맞춤형 뉴스를 제공하고, 댓글 및 좋아요 기능을 통해 사용자 간 소통을 지원하는 백엔드 중심의 뉴스 플랫폼 >**

🕛 **프로젝트 기간**: 2025.04.16 ~ 2025.05.13
<br>

## **기술 스택**
💻 **Backend**
- Java 17
- Spring Boot
- Spring Data JPA
- Spring Validation
- Spring AOP
- Spring Batch

💽 **Database**
- PostgreSQL
- MongoDB

### ☁️ Infra & DevOps
- AWS ECS (배포)
- AWS RDS (PostgreSQL)
- AWS S3 (로그 적재)
- Docker & Docker Compose
- GitHub Actions (CI/CD)

⚡ **공통 Tool**
- Git & Github
- Discord
- Notion

## **팀원별 구현 기능 상세**
### 👤 전민기
![Image](https://github.com/user-attachments/assets/ec97a6ca-96ed-4314-ba9a-f3d43daa1916)
![Image](https://github.com/user-attachments/assets/d1f4f3a6-0701-4262-82fa-0562a43de6e9)

- **댓글 등록 및 목록 조회 API**
    - 댓글 등록, 목록 조회 기능 구현 (Spring Data JPA + DTO 설계)
    - 정렬(createdAt, likeCount) 및 커서 기반 페이지네이션 구현
    - 댓글 좋아요 및 좋아요 취소 API 구현 (중복 방지 로직 및 커스텀 예외 처리)

<br>

### 👤 이규석

<자신이 개발한 기능에 대한 이미지>

- **사용자 관리 API**
    - 회원가입 및 로그인 기능 구현 (이메일 중복 검사 포함)
    - 사용자 정보 수정 기능 구현 (닉네임 수정만 가능)
    - 사용자 삭제 기능 구현 (논리 삭제 및 물리 삭제 분리 처리)
    - 로그인 성공 시 사용자 ID를 요청 헤더(MoNew-Request-User-ID)에 포함하도록 설정
    - 로그인한 사용자만 댓글/좋아요 등 기능 접근 가능하도록 인증 처리


- **관심사 관리 API**
    - 관심사 등록/조회/수정/삭제 API 구현
    - 이름 유사도 검사(80%)를 통한 중복 등록 제한 로직 구현
    - 관심사 키워드는 다중 등록 가능하며 뉴스 기사 검색에 활용
    - 관심사 이름 및 키워드를 통한 부분 일치 검색 기능 구현
    - 구독 기능 구현 (구독/구독 취소 API 및 구독자 수 관리)
    - 관심사 이름/구독자 수 기준 정렬 및 커서 페이지네이션 구현

<br>

### 👤 설유일

<자신이 개발한 기능에 대한 이미지>

- **댓글 수정 및 삭제 API**
    - 본인이 작성한 댓글만 수정할 수 있도록 인증 기반 PATCH API 구현
    - 논리 삭제(/comments/{id}) 시 isDeleted 값을 true로 변경
    - 물리 삭제(/comments/{id}/hard) 시 관련 좋아요 기록까지 함께 제거되도록 처리
    - 삭제 시 예외처리 및 권한 확인 로직 구현 (작성자 외 접근 차단)


- **CI/CD 파이프라인 구축**
  - GitHub Actions를 활용한 CI/CD 파이프라인 설계 및 구현 
  - PR 생성 시 테스트 자동 수행 및 테스트 커버리지 측정(JaCoCo)

<br>

### 👤 손동혁

<자신이 개발한 기능에 대한 이미지>

- **뉴스 기사 관리 API**
    - 기사 수집 배치 구현 (Naver API 및 RSS 기반 기사 수집)
    - 기사 등록 시 중복 링크 제거 및 관심 키워드 필터링 적용
    - 기사 목록 조회 기능 구현 (제목, 요약, 관심사, 출처, 날짜 등 필터링)
    - 기사 정렬 + 커서 기반 페이지네이션 구현 (날짜, 댓글 수, 조회 수 정렬)
    - 기사 논리/물리 삭제 기능 구현 및 복구 기능 지원
    - 기사 백업 기능 구현 (S3 업로드, 날짜 기준 백업/복구 처리)

<br>

### 👤 백승헌

<자신이 개발한 기능에 대한 이미지>

- **알림 관리 API**
    - 알림 목록 조회, 단건 확인, 전체 확인 API 구현 
    - 구독 중인 관심사 관련 기사 등록 시 자동 알림 생성 
    - 작성한 댓글에 좋아요가 눌렸을 경우 알림 생성 
    - 확인된 알림은 isChecked 필드로 구분하며, 일주일 지난 알림 자동 삭제 (배치 기반)


- **사용자 활동 내역 관리 API**
    - 사용자별 활동 내역 조회 API 구현 
    - 최근 작성한 댓글 / 좋아요 누른 댓글 / 본 뉴스 기사 목록을 최대 10개까지 조회 
    - 사용자별 구독 관심사 목록 함께 포함되도록 구성

<br>


## 📂 **파일 구조**
```
📂 src
└── 📂 main
    ├── 📂 java
    │   └── 📂 sb01_monew_team6
    │       ├── 📂 cient
    │       │   ├── 📂 impl   
    │       │   │   └── 📄 GenericRssNewsClientImpl.java
    │       │   │   └── 📄 NaverNewsClientImpl.java
    │       │   ├── 📄 NaverNewsClient.java
    │       │   ├── 📄 RssNewsClient.java
    │       │   └── 📄 RssProperties.java
    │       │── 📂 config
    │       │   ├── 📄 AsyncConfig.java
    │       │   ├── 📄 BatchConfig.java
    │       │   ├── 📄 JpaConfig.java
    │       │   ├── 📄 MDCLoggingFilter.java
    │       │   ├── 📄 MonewRequestUserInterceptor.java
    │       │   ├── 📄 MongoConfig.java
    │       │   ├── 📄 QueryDslConfig.java
    │       │   ├── 📄 S3ClientConfig.java
    │       │   ├── 📄 SchedulerConfig.java
    │       │   ├── 📄 SchedulingConfig.java
    │       │   ├── 📄 SecurityConfig.java
    │       │   ├── 📄 WebClientConfig.java
    │       │   └── 📄 WebConfig.java
    │       ├── 📂 controller
    │       │   ├── 📄 ArticleController.java
    │       │   ├── 📄 ArticleViewController.java
    │       │   ├── 📄 CommentController.java
    │       │   ├── 📄 InterestController.java
    │       │   ├── 📄 NewsCollectionController.java
    │       │   ├── 📄 NotificationController.java
    │       │   ├── 📄 SubscriptionController.java
    │       │   ├── 📄 UserActivityController.java
    │       │   └── 📄 UserController.java
    │       ├── 📂 convertor    
    │       │   └── 📄 ResourceTypeConverter.java
    │       ├── 📂 dto
    │       │   ├── 📂 comment
    │       │   │   └── 📄 CommentUpdateRequest.java
    │       │   ├── 📂 news
    │       │   │   ├── 📄 ArticleDto.java
    │       │   │   ├── 📄 ArticleRestoreResultDto.java
    │       │   │   ├── 📄 ArticleViewDto.java
    │       │   │   ├── 📄 CollectResponse.java
    │       │   │   ├── 📄 CursorPageRequestArticleDto.java
    │       │   │   └── 📄 ExternalNewsItem.java
    │       │   ├── 📂 notification
    │       │   │   └── 📄 NotificationDto.java
    │       │   ├── 📂 user_activity
    │       │   │   ├── 📄 ArticleViewHistoryDto.java
    │       │   │   ├── 📄 CommentHistoryDto.java
    │       │   │   ├── 📄 CommentLikeHistoryDto.java
    │       │   │   ├── 📄 SubscriptionHistoryDto.java
    │       │   │   └── 📄 UserActivityDto.java
    │       │   ├── 📄 CommentActivityDto
    │       │   ├── 📄 CommentDto
    │       │   ├── 📄 CommentLikeActivityDto
    │       │   ├── 📄 CommentLikeDto
    │       │   ├── 📄 CommentRegisterRequest
    │       │   ├── 📄 CommentUpdateRequest
    │       │   ├── 📄 CursorPageResponseInterestDto
    │       │   ├── 📄 ErrorResponse
    │       │   ├── 📄 InterestCreateRequestDto
    │       │   ├── 📄 InterestDto
    │       │   ├── 📄 InterestUpdateRequestDto
    │       │   ├── 📄 PageResponse
    │       │   ├── 📄 SubscriptionDto
    │       │   ├── 📄 UserDto
    │       │   ├── 📄 UserLoginRequest
    │       │   ├── 📄 UserNicknameUpdateRequest
    │       │   └── 📄 UserRegisterRequest
    │       ├── 📂 entity
    │       │   ├── 📂 base
    │       │   │   ├── 📄 BaseDocument.java
    │       │   │   ├── 📄 BaseEntity.java
    │       │   │   └── 📄 BaseUpdatableEntity.java
    │       │   ├── 📂 enums
    │       │   │   ├── 📄 ResourceType.java
    │       │   │   └── 📄 UserActivityType.java
    │       │   ├── 📄 ArticleView.java
    │       │   ├── 📄 Comment.java
    │       │   ├── 📄 CommentLike.java
    │       │   ├── 📄 Interest.java
    │       │   ├── 📄 NewsArticle.java
    │       │   ├── 📄 NewsArticleInterest.java
    │       │   ├── 📄 NewsArticleInterestId.java
    │       │   ├── 📄 Notification.java
    │       │   ├── 📄 Subscription.java
    │       │   ├── 📄 User.java
    │       │   └── 📄 UserActivity.java
    │       ├── 📂 event
    │       │   ├── 📄 NotificationCreateEvent.java
    │       │   ├── 📄 UserActivityAddEvent.java
    │       │   └── 📄 UserActivityRemoveEvent.java
    │       ├── 📂 exception
    │       │   ├── 📂 comment
    │       │   │   ├── 📄 CommentException.java
    │       │   │   ├── 📄 CommentNotFoundException.java
    │       │   │   ├── 📄 CommentNotSoftDeletedException.java
    │       │   │   └── 📄 CommentValidationException.java
    │       │   ├── 📂 interest
    │       │   │   ├── 📄 InterestAlreadyExistsException.java
    │       │   │   ├── 📄 InterestException.java
    │       │   │   ├── 📄 InterestNameTooSimilarException.java
    │       │   │   └── 📄 InterestNotFoundException.java
    │       │   ├── 📂 news
    │       │   │   └── 📄 NewsException.java
    │       │   ├── 📂 notification
    │       │   │   ├── 📄 NotificationDomainException.java
    │       │   │   └── 📄 NotificationException.java
    │       │   ├── 📂 subscription
    │       │   │   ├── 📄 SubscriptionAlreadyExistsException.java
    │       │   │   ├── 📄 SubscriptionException.java
    │       │   │   └── 📄 SubscriptionNotFoundException.java
    │       │   ├── 📂 user
    │       │   │   ├── 📄 EmailAlreadyExistsException.java
    │       │   │   ├── 📄 LoginFailedException.java
    │       │   │   ├── 📄 UserException.java
    │       │   │   └── 📄 UserNotFoundException.java
    │       │   ├── 📂 user_activity
    │       │   │   ├── 📄 UserActivityDomainException.java
    │       │   │   └── 📄 UserActivityException.java
    │       │   ├── 📄 ErrorCode.java
    │       │   ├── 📄 GlobalExceptionHandler.java
    │       │   └── 📄 MonewException.java
    │       ├── 📂 handler
    │       │   ├── 📄 NotificationEventHandler.java
    │       │   └── 📄 UserActivityEventHandler.java
    │       ├── 📂 mapper
    │       │   ├── 📂 news
    │       │   ├── 📄 ArticleViewMapper.java
    │       │   ├── 📂 user_activity
    │       │   │   ├── 📄 ArticleViewHistoryMapper.java
    │       │   │   ├── 📄 CommentHistoryMapper.java
    │       │   │   ├── 📄 CommentLikeHistoryMapper.java
    │       │   │   ├── 📄 SubscriptionHistoryMapper.java
    │       │   │   └──📄 UserActivityMapper.java
    │       │   ├── 📄 NotificationMapper.java
    │       │   └── 📄 PageResponseMapper.java
    │       ├── 📂 repository
    │       │   ├── 📂 news
    │       │   │   ├── 📄 ArticleViewRepository.java
    │       │   │   ├── 📄 NewsArticleInterestRepository.java
    │       │   │   ├── 📄 NewsArticleRepository.java
    │       │   │   ├── 📄 NewsArticleRepositoryCustom.java
    │       │   │   └── 📄 NewsArticleRepositoryImpl.java
    │       │   ├── 📂 notification
    │       │   │   ├── 📄 NotificationRepository.java
    │       │   │   ├── 📄 NotificationRepositoryCustom.java
    │       │   │   └── 📄 NotificationRepositoryImpl.java
    │       │   ├── 📂 user_activity
    │       │   │   ├── 📄 UserActivityRepository.java
    │       │   │   ├── 📄 UserActivityRepositoryCustom.java
    │       │   │   └── 📄 UserActivityRepositoryImpl.java
    │       │   ├── 📄 CommentLikeRepository.java
    │       │   ├── 📄 CommentRepository.java
    │       │   ├── 📄 InterestRepository.java
    │       │   ├── 📄 InterestRepositoryCustom.java
    │       │   ├── 📄 InterestRepositoryImpl.java
    │       │   ├── 📄 SubscriptionRepository.java
    │       │   └── 📄 UserRepository.java
    │       ├── 📂 scheduler
    │       │   ├── 📄 LogUploadScheduler.java
    │       │   └── 📄 NotificationScheduler.java
    │       ├── 📂 service
    │       │   ├── 📂 impl
    │       │   │   ├── 📄 ArticleServiceImpl.java
    │       │   │   ├── 📄 ArticleViewServiceImpl.java
    │       │   │   ├── 📄 CommentLikeServiceImpl.java
    │       │   │   ├── 📄 CommentServiceImpl.java
    │       │   │   ├── 📄 InterestServiceImpl.java
    │       │   │   ├── 📄 NewsCollectionServiceImpl.java
    │       │   │   ├── 📄 NotificationServiceImpl.java
    │       │   │   ├── 📄 SubscriptionServiceImpl.java
    │       │   │   ├── 📄 UserActivityServiceImpl.java
    │       │   │   └── 📄 UserServiceImpl.java
    │       │   ├── 📄 ArticleService.java
    │       │   ├── 📄 ArticleViewService.java
    │       │   ├── 📄 CommentLikeService.java
    │       │   ├── 📄 CommentService.java
    │       │   ├── 📄 InterestService.java
    │       │   ├── 📄 NewsCollectionService.java
    │       │   ├── 📄 NotificationService.java
    │       │   ├── 📄 SubscriptionService.java
    │       │   ├── 📄 UserActivityService.java
    │       │   └── 📄 UserService.java
    │       ├── 📂 storage.s3
    │       │   ├── 📄 ArticleBackupTasklet.java
    │       │   └── 📄 S3LogStorage.java
    │       ├── 📂 validation
    │       │   ├── 📂 group
    │       │   │   ├── 📄 NotificationValidationGroup.java
    │       │   │   └── 📄 UserActivityValidationGroup.java
    │       │   ├── 📂 user_activity
    │       │   │   ├── 📄 ArticleViewHistoryValidator.java
    │       │   │   ├── 📄 CommentHistoryValidator.java
    │       │   │   ├── 📄 CommentLikeHistoryValidator.java
    │       │   │   ├── 📄 SubscriptionHistoryValidator.java
    │       │   │   └── 📄 UserActivityValidator.java
    │       │   └── 📄 UserActivityEventValidatorDispatcher.java
    │       └── 📄 Sb01MonewTeam6Application.java
    └── 📂 resources
        │   ├── 📂 static
        │   ├── 📂 assets
        │   └── 📄 favicon.ico
        ├── 📄 application.yml
        ├── 📄 application-dev.yml        
        ├── 📄 application-prod.yml
        └── 📄 logback-spring.xml       
```

<br>


<br>


## 🌐 **배포 링크**
http://my-alb-1195026143.ap-northeast-2.elb.amazonaws.com

<br>


## ☁️ **ERD**
![Image](https://github.com/user-attachments/assets/8a3a5ff0-7ab0-4393-8c01-a8ce4b7b593e)




