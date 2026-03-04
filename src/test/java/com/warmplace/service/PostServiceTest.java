package com.warmplace.service;

import com.warmplace.entity.Gallery;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
import com.warmplace.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private GalleryService galleryService;

    @InjectMocks
    private PostService postService;

    private User testAuthor;
    private Gallery testGallery;
    private Post testPost;

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
    }

    @DisplayName("모든 게시글 조회")
    @Test
    void getAllPosts_Success() {
        when(postRepository.findAll()).thenReturn(List.of(testPost));

        List<Post> posts = postService.getAllPosts();

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).isEqualTo("테스트 제목");
    }

    @DisplayName("게시글 ID로 조회")
    @Test
    void getPostById_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        Post post = postService.getPostById(1L);

        assertThat(post).isNotNull();
        assertThat(post.getTitle()).isEqualTo("테스트 제목");
    }

    @DisplayName("게시글 ID로 조회 - 존재하지 않는 경우")
    @Test
    void getPostById_NotFound() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(999L))
                .hasMessageContaining("Post not found");
    }

    @DisplayName("갤러리별 게시글 조회")
    @Test
    void getPostsByGalleryId_Success() {
        when(postRepository.findByGalleryId(1L)).thenReturn(List.of(testPost));

        List<Post> posts = postService.getPostsByGalleryId(1L);

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getGallery().getId()).isEqualTo(1L);
    }

    @DisplayName("작성자별 게시글 조회")
    @Test
    void getPostsByAuthorId_Success() {
        when(postRepository.findByAuthorId(1L)).thenReturn(List.of(testPost));

        List<Post> posts = postService.getPostsByAuthorId(1L);

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getAuthor().getId()).isEqualTo(1L);
    }

    @DisplayName("게시글 생성")
    @Test
    void createPost_Success() {
        Gallery gallery = Gallery.builder().id(1L).build();
        Post newPost = Post.builder()
                .title("새 제목")
                .content("새 내용")
                .gallery(gallery)
                .build();

        when(galleryService.getGalleryById(1L)).thenReturn(testGallery);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Post saved = postService.createPost(newPost, testAuthor);

        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getTitle()).isEqualTo("새 제목");
        
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getAuthor().getId()).isEqualTo(1L);
        
        verify(galleryService).incrementPostCount(1L);
    }

    @DisplayName("게시글 수정")
    @Test
    void updatePost_Success() {
        Post updateData = Post.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Post updated = postService.updatePost(1L, updateData);

        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
        verify(postRepository).save(testPost);
    }

    @DisplayName("게시글 좋아요 증가")
    @Test
    void incrementLikes_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        Post updated = postService.incrementLikes(1L);

        assertThat(updated.getLikes()).isEqualTo(1);
        verify(postRepository).save(testPost);
    }

    @DisplayName("게시글 삭제")
    @Test
    void deletePost_Success() {
        doNothing().when(postRepository).deleteById(1L);

        postService.deletePost(1L);

        verify(postRepository).deleteById(1L);
    }
}
