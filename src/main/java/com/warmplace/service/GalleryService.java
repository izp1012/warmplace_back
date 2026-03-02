package com.warmplace.service;

import com.warmplace.entity.Gallery;
import com.warmplace.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;

    public List<Gallery> getAllGalleries() {
        return galleryRepository.findAll();
    }

    public Gallery getGalleryById(Long id) {
        return galleryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gallery not found"));
    }

    public List<Gallery> getGalleriesByCategory(String category) {
        return galleryRepository.findByCategory(category);
    }

    @Transactional
    public Gallery createGallery(Gallery gallery) {
        return galleryRepository.save(gallery);
    }

    @Transactional
    public Gallery updateGallery(Long id, Gallery galleryDetails) {
        Gallery gallery = getGalleryById(id);
        gallery.setName(galleryDetails.getName());
        gallery.setDescription(galleryDetails.getDescription());
        gallery.setCoverImage(galleryDetails.getCoverImage());
        gallery.setCategory(galleryDetails.getCategory());
        return galleryRepository.save(gallery);
    }

    @Transactional
    public void incrementPostCount(Long galleryId) {
        Gallery gallery = getGalleryById(galleryId);
        gallery.setPostCount(gallery.getPostCount() + 1);
        galleryRepository.save(gallery);
    }

    @Transactional
    public void deleteGallery(Long id) {
        galleryRepository.deleteById(id);
    }
}
