package com.project.itda.domain.social.controller;

import com.project.itda.domain.auth.dto.SessionUser;
import com.project.itda.domain.social.dto.response.ChatParticipantResponse;
import com.project.itda.domain.social.dto.response.ChatRoomResponse;
import com.project.itda.domain.social.service.ChatRoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final HttpSession httpSession;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@RequestBody Map<String, String> params) {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();

        String roomName = (String) params.get("roomName");
        Object maxPartObj = params.get("maxParticipants");
        Integer maxParticipants = (maxPartObj instanceof Integer) ? (Integer) maxPartObj : 10;
        String description = (String) params.get("description");
        String location = (String) params.get("location");
        String category = (String) params.get("category");

        ChatRoomResponse response = chatRoomService.createChatRoomWithAllInfo(
                roomName, user.getEmail(), maxParticipants, description, location, category
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getRooms() {
        List<ChatRoomResponse> rooms = chatRoomService.findAllRoomsAsResponse();
        return ResponseEntity.ok(rooms);
    }

    // ❌ 제거: REST API 엔드포인트
    // @PostMapping("/rooms/{roomId}/read")
    // public ResponseEntity<Void> markAsRead(@PathVariable Long roomId) {
    //     SessionUser user = (SessionUser) httpSession.getAttribute("user");
    //     if (user != null) {
    //         chatRoomService.updateLastReadAt(roomId, user.getEmail());
    //         return ResponseEntity.ok().build();
    //     }
    //     return ResponseEntity.status(401).build();
    // }

    @GetMapping("/rooms/{roomId}/members")
    public ResponseEntity<List<ChatParticipantResponse>> getRoomMembers(
            @PathVariable Long roomId,
            HttpSession httpSession
    ) {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        Long currentUserId = (user != null) ? user.getUserId() : null;
        List<ChatParticipantResponse> members = chatRoomService.getParticipantList(roomId, currentUserId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<List<ChatRoomResponse>> getMyRooms() {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();
        List<ChatRoomResponse> myRooms = chatRoomService.findMyRooms(user.getEmail());
        return ResponseEntity.ok(myRooms);
    }

    @PutMapping("/rooms/{roomId}/notice")
    public ResponseEntity<Void> updateNotice(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> request
    ) {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();
        String notice = request.get("notice");
        chatRoomService.updateNotice(roomId, notice, user.getEmail());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<ChatParticipantResponse>> searchUsers(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        Long currentUserId = (user != null) ? user.getUserId() : null;
        return ResponseEntity.ok(chatRoomService.searchUsers(keyword, currentUserId));
    }

    @PostMapping("/rooms/{roomId}/invite")
    public ResponseEntity<Void> inviteMember(
            @PathVariable Long roomId,
            @RequestBody Map<String, Long> request,
            @SessionAttribute(name = "user", required = false) SessionUser sessionUser
    ) {
        if (sessionUser == null) {
            return ResponseEntity.status(401).build();
        }

        Long targetUserId = request.get("targetUserId");

        if (targetUserId == null) {
            System.out.println("❌ 초대 실패: targetUserId가 null입니다. 요청 데이터: " + request);
            return ResponseEntity.badRequest().build();
        }

        chatRoomService.inviteMember(roomId, targetUserId, sessionUser.getEmail());
        return ResponseEntity.ok().build();
    }
}