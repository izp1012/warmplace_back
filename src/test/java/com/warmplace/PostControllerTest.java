package com.warmplace;

import com.warmplace.controller.PostController;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
import com.warmplace.security.UserPrincipal;
import com.warmplace.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    @Test
    void createPost_요청시_서비스_호출되고_PostResponse_반환() {
        // given
        User user = User.builder().id(10L).username("testuser").build();
        UserPrincipal principal = new UserPrincipal(user);

        Post request = Post.builder()
                .title("제목")
                .content("내용")
                .build();

        Post saved = Post.builder()
                .id(100L)
                .title("제목")
                .content("내용")
                .author(user)
                .build();

        when(postService.createPost(any(Post.class), any(User.class))).thenReturn(saved);

        // when
        ResponseEntity<?> response = postController.createPost(request, principal);

        // then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
    }
}

