package com.yagubogu.reward.dto.v1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GifticonRecipientRequest(
        @NotBlank
        @Pattern(
                regexp = "^010(?:[- ]?\\d{4}){2}$",
                message = "Invalid recipient phone number"
        )
        String phoneNumber
) {
}
