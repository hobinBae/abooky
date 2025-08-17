package com.c203.autobiography.domain.auth.service;

import com.c203.autobiography.domain.auth.dto.ForgotPasswordRequest;
import com.c203.autobiography.domain.member.entity.Member;

public interface EmailService {
    void sendPasswordResetEmail(ForgotPasswordRequest request);
}
