package com.c203.autobiography.domain.rtc.service;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomServiceClient;
import livekit.LivekitModels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupRoomServiceImpl implements GroupRoomService {

    private final RoomServiceClient roomServiceClient;
    private final AccessTokenFactory tokenFactory;


    @Override
    public String getRoomName(Long groupId) {
        return "group-"+groupId;
    }

    @Override
    public void createRoom(Long groupId) throws Exception {
        String roomName = getRoomName(groupId);
        try {
            Response<LivekitModels.Room> resp =
                    roomServiceClient.createRoom(roomName).execute();
            if (!resp.isSuccessful()) {
                throw new IllegalStateException("Room creation failed: " + resp.errorBody().string());
            }
        } catch (IllegalStateException e) {
            // 이미 존재할 때는 무시
            if (!e.getMessage().contains("already exists")) {
                log.error("[LiveKit] 방 생성 실패: {}", e.getMessage());
                throw e;
            }
        }
    }

    @Override
    public String generateToken(Long groupId, String identity, String userName) throws Exception {
        createRoom(groupId);
        // 토큰 생성
        AccessToken token = tokenFactory.create(identity, userName, getRoomName(groupId));
        return token.toJwt();
    }

    @Override
    public void deleteRoom(Long groupId) throws Exception {
        String roomName = getRoomName(groupId);
        Response<Void> resp = roomServiceClient.deleteRoom(roomName).execute();
        if (!resp.isSuccessful()) {
            log.error("[LiveKit] 방 삭제 실패: {}", resp.errorBody().string());
            throw new IllegalStateException("Failed to delete room");
        }
    }
}
