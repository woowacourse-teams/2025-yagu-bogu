package com.yagubogu.auth.service;

import com.yagubogu.auth.client.AuthGateway;
import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LoginResponse.MemberResponse;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.domain.Role;
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

        Optional<Member> memberOptional = memberRepository.findByOauthId(response.oauthId());
        boolean isNew = memberOptional.isEmpty();
        Member member = findOrCreateMember(isNew, response, memberOptional);
        MemberClaims memberClaims = MemberClaims.from(member);

        String accessToken = jwtProvider.createAccessToken(memberClaims);
        String refreshToken = jwtProvider.createRefreshToken(memberClaims);

        return new LoginResponse(accessToken, refreshToken, isNew, MemberResponse.from(member));
    }

    public MemberClaims makeMemberClaims(final String token){
        jwtProvider.validateAccessToken(token);
        Long memberId = jwtProvider.getMemberIdByAccessToken(token);
        Role role = jwtProvider.getRoleByAccessToken(token);

        return new MemberClaims(memberId, role);
    }

    private Member findOrCreateMember(final boolean isNew, final AuthResponse response,
                                      final Optional<Member> memberOptional) {
        if (isNew) {
            return memberRepository.save(response.toMember());
        }

        return memberOptional.get();
    }

    private void validateToken(final AuthResponse response, final OAuthProvider provider) {
        authValidators.stream()
                .filter(v -> v.supports(provider))
                .findFirst()
                .ifPresentOrElse(
                        validator -> invokeValidator(validator, response),
                        () -> {
                            throw new UnsupportedOperationException("No validator for: " + provider);
                        }
                );
    }

    @SuppressWarnings("unchecked")
    private <T extends AuthResponse> void invokeValidator(AuthValidator<?> validator, AuthResponse response) {
        ((AuthValidator<T>) validator).validate((T) response);
    }
}
