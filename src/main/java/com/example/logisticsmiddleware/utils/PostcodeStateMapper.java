package com.example.logisticsmiddleware.utils;

import org.springframework.stereotype.Component;

@Component
public class PostcodeStateMapper {
    public String getStateFromPostcode(String postcode) {
        if (postcode == null || postcode.length() < 5) {
            return null;
        }

        try {
            int prefix = Integer.parseInt(postcode.substring(0, 2));
            return switch (prefix) {
                case 1, 2 -> "Perlis";
                case 4, 5, 6, 7, 8, 9 -> "Kedah";  // broader for accuracy
                case 10, 11, 12, 13, 14 -> "Pulau Pinang";
                case 15, 16 -> "Kelantan";
                case 17, 18 -> "Terengganu";
                case 20, 21, 22, 23, 24, 25, 26, 27, 28, 29 -> "Pahang";
                case 30, 31, 32, 33, 34, 35, 36, 37, 38, 39 -> "Perak";
                case 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69-> "Selangor";  // expanded for KL fringe
                case 50, 51, 52, 53, 54, 55, 56, 57, 58, 59 -> "Kuala Lumpur";
                // Putrajaya specifically narrower if needed, but most fall under Selangor/KL
                case 70, 71, 72, 73, 74 -> "Negeri Sembilan";
                case 75, 76, 77, 78 -> "Melaka";
                case 79, 80, 81, 82, 83, 84, 85, 86, 87 -> "Johor";
                case 88, 89, 93, 94, 95, 96, 97, 98 -> "Sarawak";
                case 90, 91, 92 -> "Sabah";
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
