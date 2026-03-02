package com.warmplace.controller;

import com.warmplace.dto.PostResponse;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
import com.warmplace.security.UserPrincipal;
import com.warmplace.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts().stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(PostResponse.fromEntity(post));
    }

    @GetMapping("/gallery/{galleryId}")
    public ResponseEntity<List<PostResponse>> getPostsByGalleryId(@PathVariable Long galleryId) {
        List<PostResponse> posts = postService.getPostsByGalleryId(galleryId).stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestBody Post post,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User author = new User();
        author.setId(userPrincipal.getUser().getId());
        Post created = postService.createPost(post, author);
        return ResponseEntity.ok(PostResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody Post post) {
        Post updated = postService.updatePost(id, post);
        return ResponseEntity.ok(PostResponse.fromEntity(updated));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponse> incrementLikes(@PathVariable Long id) {
        Post updated = postService.incrementLikes(id);
        return ResponseEntity.ok(PostResponse.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
