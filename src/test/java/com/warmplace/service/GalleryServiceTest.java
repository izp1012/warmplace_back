package com.warmplace.service;

import com.warmplace.entity.Gallery;
import com.warmplace.repository.GalleryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {

    @Mock
    private GalleryRepository galleryRepository;

    @InjectMocks
    private GalleryService galleryService;

    private Gallery testGallery;

    @BeforeEach
    void setUp() {
        testGallery = Gallery.builder()
                .id(1L)
                .name("테스트 갤러리")
                .description("설명")
                .category("general")
                .postCount(5)
                .build();
    }

    @DisplayName("모든 갤러리 조회")
    @Test
    void getAllGalleries_Success() {
        when(galleryRepository.findAll()).thenReturn(List.of(testGallery));

        List<Gallery> galleries = galleryService.getAllGalleries();

        assertThat(galleries).hasSize(1);
        assertThat(galleries.get(0).getName()).isEqualTo("테스트 갤러리");
    }

    @DisplayName("갤러리 ID로 조회")
    @Test
    void getGalleryById_Success() {
        when(galleryRepository.findById(1L)).thenReturn(Optional.of(testGallery));

        Gallery gallery = galleryService.getGalleryById(1L);

        assertThat(gallery).isNotNull();
        assertThat(gallery.getName()).isEqualTo("테스트 갤러리");
    }

    @DisplayName("갤러리 ID로 조회 - 존재하지 않는 경우")
    @Test
    void getGalleryById_NotFound() {
        when(galleryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> galleryService.getGalleryById(999L))
                .hasMessageContaining("Gallery not found");
    }

    @DisplayName("카테고리별 갤러리 조회")
    @Test
    void getGalleriesByCategory_Success() {
        when(galleryRepository.findByCategory("general")).thenReturn(List.of(testGallery));

        List<Gallery> galleries = galleryService.getGalleriesByCategory("general");

        assertThat(galleries).hasSize(1);
        assertThat(galleries.get(0).getCategory()).isEqualTo("general");
    }

    @DisplayName("갤러리 생성")
    @Test
    void createGallery_Success() {
        Gallery newGallery = Gallery.builder()
                .name("새 갤러리")
                .description("새 설명")
                .category("tech")
                .postCount(0)
                .build();

        when(galleryRepository.save(any(Gallery.class))).thenAnswer(invocation -> {
            Gallery saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Gallery saved = galleryService.createGallery(newGallery);

        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getName()).isEqualTo("새 갤러리");
        verify(galleryRepository).save(newGallery);
    }

    @DisplayName("갤러리 수정")
    @Test
    void updateGallery_Success() {
        Gallery updateData = Gallery.builder()
                .name("수정된 이름")
                .description("수정된 설명")
                .category("art")
                .coverImage("newimage.jpg")
                .build();

        when(galleryRepository.findById(1L)).thenReturn(Optional.of(testGallery));
        when(galleryRepository.save(any(Gallery.class))).thenReturn(testGallery);

        Gallery updated = galleryService.updateGallery(1L, updateData);

        assertThat(updated.getName()).isEqualTo("수정된 이름");
        assertThat(updated.getDescription()).isEqualTo("수정된 설명");
        verify(galleryRepository).save(testGallery);
    }

    @DisplayName("갤러리 포스트 수 증가")
    @Test
    void incrementPostCount_Success() {
        when(galleryRepository.findById(1L)).thenReturn(Optional.of(testGallery));
        when(galleryRepository.save(any(Gallery.class))).thenReturn(testGallery);

        galleryService.incrementPostCount(1L);

        verify(galleryRepository).save(testGallery);
    }

    @DisplayName("갤러리 삭제")
    @Test
    void deleteGallery_Success() {
        doNothing().when(galleryRepository).deleteById(1L);

        galleryService.deleteGallery(1L);

        verify(galleryRepository).deleteById(1L);
    }
}
