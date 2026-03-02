package com.warmplace.dto;

import com.warmplace.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private Long galleryId;
    private String galleryName;
    private String title;
    private String content;
    private List<String> images;
    private Long authorId;
    private String authorNickname;
    private LocalDateTime createdAt;
    private Integer likes;

    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .galleryId(post.getGallery().getId())
                .galleryName(post.getGallery().getName())
                .title(post.getTitle())
                .content(post.getContent())
                .images(post.getImages())
                .authorId(post.getAuthor().getId())
                .authorNickname(post.getAuthor().getNickname())
                .createdAt(post.getCreatedAt())
                .likes(post.getLikes())
                .build();
    }
}
