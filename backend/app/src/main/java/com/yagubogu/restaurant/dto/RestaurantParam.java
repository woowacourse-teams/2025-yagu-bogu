package com.yagubogu.restaurant.dto;

public record RestaurantParam(
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
