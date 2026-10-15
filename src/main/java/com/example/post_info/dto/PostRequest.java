package com.example.post_info.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostRequest {
    @NotBlank(message = "PostName is required")
    @Size(min = 7, max = 50, message = "Name must be 7 to 50 characters")
    private String postName;

    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 200, message = "Content is required 7 to 200 character")
    private String postContent;
}
