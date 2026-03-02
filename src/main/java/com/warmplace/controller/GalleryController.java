package com.warmplace.controller;

import com.warmplace.dto.GalleryResponse;
import com.warmplace.entity.Gallery;
import com.warmplace.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/galleries")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    @GetMapping
    public ResponseEntity<List<GalleryResponse>> getAllGalleries() {
        List<GalleryResponse> galleries = galleryService.getAllGalleries().stream()
                .map(GalleryResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(galleries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GalleryResponse> getGalleryById(@PathVariable Long id) {
        Gallery gallery = galleryService.getGalleryById(id);
        return ResponseEntity.ok(GalleryResponse.fromEntity(gallery));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<GalleryResponse>> getGalleriesByCategory(@PathVariable String category) {
        List<GalleryResponse> galleries = galleryService.getGalleriesByCategory(category).stream()
                .map(GalleryResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(galleries);
    }

    @PostMapping
    public ResponseEntity<GalleryResponse> createGallery(@RequestBody Gallery gallery) {
        Gallery created = galleryService.createGallery(gallery);
        return ResponseEntity.ok(GalleryResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GalleryResponse> updateGallery(@PathVariable Long id, @RequestBody Gallery gallery) {
        Gallery updated = galleryService.updateGallery(id, gallery);
        return ResponseEntity.ok(GalleryResponse.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGallery(@PathVariable Long id) {
        galleryService.deleteGallery(id);
        return ResponseEntity.noContent().build();
    }
}
