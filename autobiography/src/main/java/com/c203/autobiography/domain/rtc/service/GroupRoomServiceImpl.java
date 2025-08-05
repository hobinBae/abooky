package com.c203.autobiography.domain.rtc.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import io.livekit.livekit_server_sdk.RoomService;
import io.livekit.livekit_server_sdk.RoomService.RoomOptions;

@Service
public class GroupRoomServiceImpl implements GroupRoomService {

    @Value("${livekit.serverUrl}")
    private String serverUrl;

    @Value("${livekit.apiKey}")
    private String apiKey;

    @Value("${livekit.apiSecret}")
    private String apiSecret;


    @Override
    public String getRoomName(Long groupId) {
        return "group-"+groupId;
    }

    @Override
    public void createRoom(Long groupId) throws Exception {
        String roomName = getRoomName(groupId);
        RoomService roomService = new RoomService(serverUrl, apiKey, apiSecret);
        try {
            roomService.createRoom(roomName, new RoomOptions().setEmptyTimeout(60));
        } catch (Exception e) {
            // 이미 방이 존재하는 경우 로그 후 무시
            if (!e.getMessage().contains("already exists")) {
                log.error("방 생성 실패: {}", e.getMessage());
                throw e;
            }
        }
    }

    @Override
    public String generateToken(Long groupId, String identity, String userName) throws Exception {
        createRoom(groupId);
        return new AccessToken(apiKey, apiSecret)
                .setIdentity(identity)
                .setName(userName)
                .toJwt();
    }

    @Override
    public void deleteRoom(Long groupId) throws Exception {
        String roomName = getRoomName(groupId);
        new RoomService(serverUrl, apiKey, apiSecret).deleteRoom(roomName);
    }
}
