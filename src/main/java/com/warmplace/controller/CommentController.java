package com.warmplace.controller;

import com.warmplace.dto.CommentResponse;
import com.warmplace.entity.Comment;
import com.warmplace.entity.User;
import com.warmplace.security.UserPrincipal;
import com.warmplace.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId).stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @RequestBody Comment comment,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User author = new User();
        author.setId(userPrincipal.getUser().getId());
        author.setNickname(userPrincipal.getUser().getNickname());
        Comment created = commentService.createComment(comment, author);
        return ResponseEntity.ok(CommentResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id, @RequestBody String content) {
        Comment updated = commentService.updateComment(id, content);
        return ResponseEntity.ok(CommentResponse.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
