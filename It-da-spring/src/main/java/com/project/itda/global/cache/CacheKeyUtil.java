package com.project.itda.global.cache;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component("cacheKeyUtil")
public class CacheKeyUtil {

    public String matchKey(Long userId, List<Long> meetingIds) {
        List<Long> sorted = new ArrayList<>(meetingIds == null ? List.of() : meetingIds);
        Collections.sort(sorted);

        String joined = sorted.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String hash = sha256Hex(joined).substring(0, 16);
        return "match:" + userId + ":" + hash;
    }

    private String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
