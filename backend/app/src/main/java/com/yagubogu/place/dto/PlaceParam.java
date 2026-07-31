package com.yagubogu.place.dto;

public record PlaceParam(
        String contentId,
        Long stadiumId,
        String title,
        String address,
        double mapX,
        double mapY,
        Integer distance,
        String tel,
        String imageUrl
) {}
