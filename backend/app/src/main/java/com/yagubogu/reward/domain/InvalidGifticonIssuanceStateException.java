package com.yagubogu.reward.domain;

public class InvalidGifticonIssuanceStateException extends RuntimeException {

    public InvalidGifticonIssuanceStateException(
            final String action,
            final GifticonIssuanceStatus actual
    ) {
        super("Gifticon issuance cannot " + action + ": status=" + actual);
    }

    public InvalidGifticonIssuanceStateException(
            final String action,
            final GifticonIssuanceStatus expected,
            final GifticonIssuanceStatus actual
    ) {
        super("Gifticon issuance cannot " + action + ": expected=" + expected + ", actual=" + actual);
    }
}
