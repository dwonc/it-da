package com.project.itda.domain.admin.controller;

import com.project.itda.domain.admin.dto.response.AdminDashboardResponse;
import com.project.itda.domain.admin.entity.AdminUser;
import com.project.itda.domain.admin.repository.AdminUserRepository;
import com.project.itda.domain.admin.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    //대시보드 조회 ==============================================================

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard(HttpSession session) {

        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AdminDashboardResponse response = adminService.getDashboard(adminId);
        return ResponseEntity.ok(response);
    }
}