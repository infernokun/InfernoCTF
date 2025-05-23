package com.infernokun.infernoctf.models.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FlagAnswer {
    private String flag;
    private String username;
    private String questionId;
    @Builder.Default
    private LocalDateTime submissionTime = LocalDateTime.now();
}
