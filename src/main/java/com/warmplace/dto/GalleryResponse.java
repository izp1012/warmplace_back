package com.warmplace.dto;

import com.warmplace.entity.Gallery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryResponse {
    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private Integer postCount;
    private String category;
    private LocalDateTime createdAt;

    public static GalleryResponse fromEntity(Gallery gallery) {
        return GalleryResponse.builder()
                .id(gallery.getId())
                .name(gallery.getName())
                .description(gallery.getDescription())
                .coverImage(gallery.getCoverImage())
                .postCount(gallery.getPostCount())
                .category(gallery.getCategory())
                .createdAt(gallery.getCreatedAt())
                .build();
    }
}
