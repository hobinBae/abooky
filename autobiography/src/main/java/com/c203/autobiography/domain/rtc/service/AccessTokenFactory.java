package com.c203.autobiography.domain.rtc.service;

import io.livekit.server.AccessToken;
import io.livekit.server.Room;
import io.livekit.server.RoomJoin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenFactory {

    @Value("${livekit.apiKey}")
    private String apiKey;

    @Value("${livekit.apiSecret}")
    private String apiSecret;

    /**
     * 주어진 identity, userName으로 JWT AccessToken을 생성합니다.
     */
    public AccessToken create(String identity, String userName, String roomName) {
        AccessToken token = new AccessToken(apiKey, apiSecret);
        token.setIdentity(identity);
        token.setName(userName);
        token.setMetadata(userName +"@" + roomName);
        token.addGrants(new RoomJoin(true), new Room(roomName));
        return token;
    }
}
