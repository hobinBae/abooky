package com.c203.autobiography.domain.rtc.service;

public interface GroupRoomService {
    /**
     * 그룹 ID로부터 LiveKit 방 이름을 생성합니다.
     */
    String getRoomName(Long groupId);

    /**
     * 해당 그룹 방을 생성합니다(없으면 새로 생성, 이미 있으면 무시)
     */
    void createRoom(Long groupId) throws Exception;

    /**
     * 그룹 방에 입장할 수 있는 JWT 토큰을 생성합니다.
     */
    String generateToken(Long groupId, String identity, String userName) throws Exception;

    /**
     * 그룹 방을 삭제합니다.
     */
    void deleteRoom(Long groupId) throws Exception;
}
