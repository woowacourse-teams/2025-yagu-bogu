package com.yagubogu.auth.service;

import com.yagubogu.auth.client.AuthGateway;
import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LoginResponse.MemberResponse;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.token.JwtProvider;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final AuthGateway authGateway;
    private final JwtProvider jwtProvider;
    private final List<AuthValidator<? extends AuthResponse>> authValidators;

    public LoginResponse login(final LoginRequest request) {
        AuthResponse response = authGateway.validateToken(request);
        validateToken(response, OAuthProvider.GOOGLE);

        Optional<Member> memberOptional = memberRepository.findBySub(response.sub());
        boolean isExisting = memberOptional.isPresent();
        Member member = memberOptional.orElseGet(() -> memberRepository.save(response.toMember()));
        MemberClaims memberClaims = MemberClaims.from(member);

        String accessToken = jwtProvider.createAccessToken(memberClaims);
        String refreshToken = jwtProvider.createRefreshToken(memberClaims);

        return new LoginResponse(accessToken, refreshToken, isExisting, MemberResponse.from(member));
    }

    private void validateToken(final AuthResponse response, final OAuthProvider provider) {
        authValidators.stream()
                .filter(v -> v.supports(provider))
                .findFirst()
                .map(validator -> {
                    invokeValidator(validator, response);
                    return true;
                })
                .orElseThrow(() -> new UnsupportedOperationException("No validator for: " + provider));
    }

    @SuppressWarnings("unchecked")
    private <T extends AuthResponse> void invokeValidator(AuthValidator<?> validator, AuthResponse response) {
        ((AuthValidator<T>) validator).validate((T) response);
    }
}
