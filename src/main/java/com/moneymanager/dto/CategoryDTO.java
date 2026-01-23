package com.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {

    private long id;
    private String name;
    private String type;
    private String icon;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
    private Long profileId;
}
