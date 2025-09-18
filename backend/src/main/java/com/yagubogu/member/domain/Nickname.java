package com.yagubogu.member.domain;

import com.yagubogu.global.exception.UnprocessableEntityException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Nickname {

    private static final int MAX_LENGTH = 12;

    @Column(name = "nickname", unique = true, nullable = false, length = MAX_LENGTH)
    private String value;

    public Nickname(final String value) {
        validate(value);
        this.value = value;
    }

    private void validate(final String value) {
        if (value == null || value.isBlank()) {
            throw new UnprocessableEntityException("Nickname must not be empty.");
        }
        System.out.println(value);
        if (value.length() > MAX_LENGTH) {
            throw new UnprocessableEntityException("Nickname must be " + MAX_LENGTH + " characters or fewer.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
