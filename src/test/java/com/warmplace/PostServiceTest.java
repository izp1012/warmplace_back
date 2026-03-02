package com.warmplace;

import com.warmplace.entity.Gallery;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
import com.warmplace.repository.PostRepository;
import com.warmplace.service.GalleryService;
import com.warmplace.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void createPost_정상_저장되고_갤러리_카운트_증가() {
        // given
        Gallery gallery = Gallery.builder().id(1L).name("테스트 갤러리").build();
        when(galleryService.getGalleryById(1L)).thenReturn(gallery);

        User author = User.builder().id(10L).username("testuser").build();

        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .gallery(Gallery.builder().id(1L).build())
                .images(List.of())
                .build();

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        // when
        Post saved = postService.createPost(post, author);

        // then
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository, times(1)).save(captor.capture());
        verify(galleryService, times(1)).incrementPostCount(1L);

        Post captured = captor.getValue();
        assertThat(captured.getGallery().getId()).isEqualTo(1L);
        assertThat(captured.getAuthor().getId()).isEqualTo(10L);
        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getTitle()).isEqualTo("제목");
        assertThat(saved.getContent()).isEqualTo("내용");
    }
}

