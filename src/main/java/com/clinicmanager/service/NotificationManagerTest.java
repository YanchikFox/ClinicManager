//package com.clinicmanager.service;
//
//import com.clinicmanager.model.entities.Notification;
//import com.clinicmanager.repository.NotificationRepository;
//import com.clinicmanager.time.TimeManager;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//class NotificationManagerTest {
//
//    private final TimeManager timeManager = TimeManager.getInstance();
//    private LocalDateTime previousTime;
//
//    @BeforeEach
//    void setUp() {
//        previousTime = timeManager.getCurrentTime();
//    }
//
//    @AfterEach
//    void tearDown() {
//        timeManager.setCurrentTime(previousTime);
//    }
//
//    @Test
//    void createNotificationUsesVirtualTime() {
//        LocalDateTime virtualTime = LocalDateTime.of(2024, 1, 1, 12, 0);
//        timeManager.setCurrentTime(virtualTime);
//        InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
//        NotificationManager notificationManager = new NotificationManager(repository, timeManager);
//
//        Notification notification = notificationManager.createNotification(1, "Test message");
//
//        assertEquals(virtualTime, notification.timestamp());
//    }
//
//    private static class InMemoryNotificationRepository extends NotificationRepository {
//
//        InMemoryNotificationRepository() {
//            super("jdbc:sqlite::memory:");
//        }
//
//        @Override
//        public int save(Notification n) {
//            return 0;
//        }
//
//        @Override
//        public void update(Notification n) {
//        }
//
//        @Override
//        public Notification findById(int id) {
//            return null;
//        }
//
//        @Override
//        public List<Notification> findAll() {
//            return Collections.emptyList();
//        }
//
//        @Override
//        public List<Notification> findByPersonId(int personId) {
//            return Collections.emptyList();
//        }
//
//        @Override
//        public List<Notification> findUnreadByPersonId(int personId) {
//            return Collections.emptyList();
//        }
//    }
//}
