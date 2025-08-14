package com.c203.autobiography.global.security.oauth2;

import com.c203.autobiography.domain.member.dto.AuthProvider;
import com.c203.autobiography.domain.member.dto.Role;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        AuthProvider provider =  AuthProvider.from(oAuth2UserRequest.getClientRegistration().getRegistrationId());
        String providerId = oAuth2User.getName();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        
        // 임의 닉네임 생성
        String rawNickname = name != null ? name : email.split("@")[0];
        String nickname = rawNickname + "_" + System.currentTimeMillis() % 10000; // 중복 방지
        
        // OAuth2 사용자용 랜덤 패스워드 생성 (예측 불가능)
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .email(email)
                                .name(name)
                                .nickname(nickname)
                                .password(randomPassword)
                                .provider(provider)
                                .providerId(providerId)
                                .role(Role.MEMBER)
                                .build()
                ));

        return new CustomOAuth2User(member, oAuth2User.getAttributes());

    }

}
