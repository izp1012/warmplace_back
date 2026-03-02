package com.warmplace.service;

import com.warmplace.entity.Comment;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
import com.warmplace.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    @Transactional
    public Comment createComment(Comment comment, User author) {
        Post post = postService.getPostById(comment.getPost().getId());
        comment.setPost(post);
        comment.setAuthor(author);
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long id, String content) {
        Comment comment = getCommentById(id);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
