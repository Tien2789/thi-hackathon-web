package com.ontop.wms.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ontop.wms.entity.Notification;
import com.ontop.wms.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final NotificationRepository notificationRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Notification>> getUnreadAlerts() {
        return ResponseEntity.ok(notificationRepository.findByIsReadFalse());
    }
}
