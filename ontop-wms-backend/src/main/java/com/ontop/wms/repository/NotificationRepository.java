package com.ontop.wms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.Notification;
import com.ontop.wms.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    List<Notification> findByIsReadFalse();
}
