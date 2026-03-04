package com.warmplace.controller;

import com.warmplace.entity.Gallery;
import com.warmplace.repository.GalleryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GalleryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GalleryRepository galleryRepository;

    @BeforeEach
    void setUp() {
        galleryRepository.deleteAll();
    }

    @DisplayName("모든 갤러리 조회")
    @Test
    void getAllGalleries_Success() throws Exception {
        Gallery gallery1 = Gallery.builder()
                .name("갤러리1")
                .description("설명1")
                .category("art")
                .postCount(5)
                .build();
        Gallery gallery2 = Gallery.builder()
                .name("갤러리2")
                .description("설명2")
                .category("photo")
                .postCount(10)
                .build();
        galleryRepository.save(gallery1);
        galleryRepository.save(gallery2);

        mockMvc.perform(get("/api/galleries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("갤러리1"))
                .andExpect(jsonPath("$[1].name").value("갤러리2"));
    }

    @DisplayName("갤러리 ID로 조회")
    @Test
    void getGalleryById_Success() throws Exception {
        Gallery gallery = Gallery.builder()
                .name("테스트 갤러리")
                .description("설명")
                .category("general")
                .postCount(3)
                .build();
        gallery = galleryRepository.save(gallery);

        mockMvc.perform(get("/api/galleries/" + gallery.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트 갤러리"))
                .andExpect(jsonPath("$.description").value("설명"))
                .andExpect(jsonPath("$.category").value("general"))
                .andExpect(jsonPath("$.postCount").value(3));
    }

    @DisplayName("갤러리 ID로 조회 - 존재하지 않는 경우")
    @Test
    void getGalleryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/galleries/99999"))
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("카테고리별 갤러리 조회")
    @Test
    void getGalleriesByCategory_Success() throws Exception {
        Gallery gallery1 = Gallery.builder()
                .name("사진 갤러리")
                .category("photo")
                .postCount(0)
                .build();
        Gallery gallery2 = Gallery.builder()
                .name("예술 갤러리")
                .category("art")
                .postCount(0)
                .build();
        Gallery gallery3 = Gallery.builder()
                .name("사진 갤러리2")
                .category("photo")
                .postCount(0)
                .build();
        galleryRepository.save(gallery1);
        galleryRepository.save(gallery2);
        galleryRepository.save(gallery3);

        mockMvc.perform(get("/api/galleries/category/photo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("photo"))
                .andExpect(jsonPath("$[1].category").value("photo"));
    }

    @DisplayName("카테고리별 조회 - 존재하지 않는 카테고리")
    @Test
    void getGalleriesByCategory_Empty() throws Exception {
        mockMvc.perform(get("/api/galleries/category/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @DisplayName("갤러리 생성")
    @Test
    void createGallery_Success() throws Exception {
        String requestBody = """
            {
                "name": "새 갤러리",
                "description": "새로운 갤러리입니다",
                "category": "tech",
                "coverImage": "image.jpg"
            }
            """;

        mockMvc.perform(post("/api/galleries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새 갤러리"))
                .andExpect(jsonPath("$.description").value("새로운 갤러리입니다"))
                .andExpect(jsonPath("$.category").value("tech"));

        assertThat(galleryRepository.findAll()).hasSize(1);
    }

    @DisplayName("갤러리 수정")
    @Test
    void updateGallery_Success() throws Exception {
        Gallery gallery = Gallery.builder()
                .name("기존 이름")
                .description("기존 설명")
                .category("old")
                .postCount(0)
                .build();
        gallery = galleryRepository.save(gallery);

        String requestBody = """
            {
                "name": "수정된 이름",
                "description": "수정된 설명",
                "category": "new",
                "coverImage": "newimage.jpg"
            }
            """;

        mockMvc.perform(put("/api/galleries/" + gallery.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된 이름"))
                .andExpect(jsonPath("$.description").value("수정된 설명"))
                .andExpect(jsonPath("$.category").value("new"));

        Gallery updated = galleryRepository.findById(gallery.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("수정된 이름");
    }

    @DisplayName("갤러리 삭제")
    @Test
    void deleteGallery_Success() throws Exception {
        Gallery gallery = Gallery.builder()
                .name("삭제할 갤러리")
                .description("설명")
                .category("test")
                .postCount(0)
                .build();
        gallery = galleryRepository.save(gallery);

        mockMvc.perform(delete("/api/galleries/" + gallery.getId()))
                .andExpect(status().isNoContent());

        assertThat(galleryRepository.findById(gallery.getId())).isEmpty();
    }
}
