package com.warmplace.service;

import com.warmplace.entity.Gallery;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
import com.warmplace.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final GalleryService galleryService;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public List<Post> getPostsByGalleryId(Long galleryId) {
        return postRepository.findByGalleryId(galleryId);
    }

    public List<Post> getPostsByAuthorId(Long authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    @Transactional
    public Post createPost(Post post, User author) {
        Gallery gallery = galleryService.getGalleryById(post.getGallery().getId());
        post.setGallery(gallery);
        post.setAuthor(author);
        Post savedPost = postRepository.save(post);
        galleryService.incrementPostCount(gallery.getId());
        return savedPost;
    }

    @Transactional
    public Post updatePost(Long id, Post postDetails) {
        Post post = getPostById(id);
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setImages(postDetails.getImages());
        return postRepository.save(post);
    }

    @Transactional
    public Post incrementLikes(Long id) {
        Post post = getPostById(id);
        post.setLikes(post.getLikes() + 1);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}
