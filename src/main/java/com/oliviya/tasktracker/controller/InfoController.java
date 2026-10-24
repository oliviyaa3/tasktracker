package com.oliviya.tasktracker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oliviya.tasktracker.payload.AppInfoDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Info", description = "Application information")
@RestController
@RequestMapping("/api/info")
public class InfoController {

    private final String appName;
    private final String appVersion;

    public InfoController(@Value("${app.name}") String appName,
            @Value("${app.version}") String appVersion) {
        this.appName = appName;
        this.appVersion = appVersion;
    }

    @Operation(summary = "Get application name and version")
    @GetMapping
    public ResponseEntity<AppInfoDTO> getInfo() {
        return ResponseEntity.ok(new AppInfoDTO(appName, appVersion));
    }
}