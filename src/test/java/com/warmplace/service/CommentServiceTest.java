package com.warmplace.service;

import com.warmplace.entity.Comment;
import com.warmplace.entity.Gallery;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
import com.warmplace.repository.CommentRepository;
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
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostService postService;

    @InjectMocks
    private CommentService commentService;

    private User testAuthor;
    private Gallery testGallery;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testAuthor = User.builder()
                .id(1L)
                .username("testuser")
                .nickname("테스트유저")
                .build();

        testGallery = Gallery.builder()
                .id(1L)
                .name("테스트 갤러리")
                .postCount(0)
                .build();

        testPost = Post.builder()
                .id(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .author(testAuthor)
                .gallery(testGallery)
                .likes(0)
                .build();

        testComment = Comment.builder()
                .id(1L)
                .content("테스트 댓글")
                .post(testPost)
                .author(testAuthor)
                .build();
    }

    @DisplayName("모든 댓글 조회")
    @Test
    void getAllComments_Success() {
        when(commentRepository.findAll()).thenReturn(List.of(testComment));

        List<Comment> comments = commentService.getAllComments();

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("테스트 댓글");
    }

    @DisplayName("댓글 ID로 조회")
    @Test
    void getCommentById_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        Comment comment = commentService.getCommentById(1L);

        assertThat(comment).isNotNull();
        assertThat(comment.getContent()).isEqualTo("테스트 댓글");
    }

    @DisplayName("댓글 ID로 조회 - 존재하지 않는 경우")
    @Test
    void getCommentById_NotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getCommentById(999L))
                .hasMessageContaining("Comment not found");
    }

    @DisplayName("게시글별 댓글 조회")
    @Test
    void getCommentsByPostId_Success() {
        when(commentRepository.findByPostId(1L)).thenReturn(List.of(testComment));

        List<Comment> comments = commentService.getCommentsByPostId(1L);

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getPost().getId()).isEqualTo(1L);
    }

    @DisplayName("댓글 생성")
    @Test
    void createComment_Success() {
        Post post = Post.builder().id(1L).build();
        Comment newComment = Comment.builder()
                .content("새 댓글")
                .post(post)
                .build();

        when(postService.getPostById(1L)).thenReturn(testPost);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Comment saved = commentService.createComment(newComment, testAuthor);

        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getContent()).isEqualTo("새 댓글");
        assertThat(saved.getAuthor().getId()).isEqualTo(1L);
        
        verify(commentRepository).save(any(Comment.class));
    }

    @DisplayName("댓글 수정")
    @Test
    void updateComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment updated = commentService.updateComment(1L, "수정된 댓글");

        assertThat(updated.getContent()).isEqualTo("수정된 댓글");
        verify(commentRepository).save(testComment);
    }

    @DisplayName("댓글 삭제")
    @Test
    void deleteComment_Success() {
        doNothing().when(commentRepository).deleteById(1L);

        commentService.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
    }
}
