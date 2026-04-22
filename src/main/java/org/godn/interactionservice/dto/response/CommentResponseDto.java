package org.godn.interactionservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {
    private UUID id;
    private UUID authorId;
    private String content;
    private Integer depthLevel;
}
