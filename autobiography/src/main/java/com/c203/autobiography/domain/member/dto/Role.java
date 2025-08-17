package com.c203.autobiography.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    MEMBER("MEMBER"),
    ADMIN("ADMIN");

    private final String key;
}

