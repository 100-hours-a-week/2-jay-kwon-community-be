package com.jay.sapapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeartDTO {

    private Long heartId, postId, userId;

    private LocalDateTime regDate;

}
