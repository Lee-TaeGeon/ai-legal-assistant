package com.taegeon.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taegeon.backend.domain.Member;
import com.taegeon.backend.dto.SignupRequest;
import com.taegeon.backend.exception.CustomException;
import com.taegeon.backend.exception.ErrorCode;
import com.taegeon.backend.repository.MemberRepository;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public void signup(SignupRequest request) {

        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(request.password());

        Member member = Member.builder()
                .email(request.email())
                .passwordHash(passwordHash)
                .nickname(request.nickname())
                .role("USER")
                .build();
        

        memberRepository.save(member);
    }
}