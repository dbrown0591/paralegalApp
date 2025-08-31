package com.paralegal.paralegalApp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(@NotBlank(message = "content is required") @Size(max = 2000)
                                   String content) {
}
