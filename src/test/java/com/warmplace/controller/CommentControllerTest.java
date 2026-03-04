package com.warmplace.controller;

import com.warmplace.entity.Comment;
import com.warmplace.entity.Gallery;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
import com.warmplace.repository.CommentRepository;
import com.warmplace.repository.GalleryRepository;
import com.warmplace.repository.PostRepository;
import com.warmplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GalleryRepository galleryRepository;

    private String token;
    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() throws Exception {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        galleryRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .isActive(true)
                .build();
        userRepository.save(testUser);

        Gallery gallery = Gallery.builder()
                .name("테스트 갤러리")
                .category("general")
                .postCount(0)
                .build();
        galleryRepository.save(gallery);

        testPost = Post.builder()
                .title("테스트 게시글")
                .content("내용")
                .author(testUser)
                .gallery(gallery)
                .likes(0)
                .build();
        postRepository.save(testPost);

        token = getToken();
    }

    @DisplayName("게시글별 댓글 조회")
    @Test
    void getCommentsByPostId_Success() throws Exception {
        Comment comment1 = Comment.builder()
                .content("첫 번째 댓글")
                .post(testPost)
                .author(testUser)
                .build();
        Comment comment2 = Comment.builder()
                .content("두 번째 댓글")
                .post(testPost)
                .author(testUser)
                .build();
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        mockMvc.perform(get("/api/comments/post/" + testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("첫 번째 댓글"))
                .andExpect(jsonPath("$[1].content").value("두 번째 댓글"));
    }

    @DisplayName("댓글 조회 - 댓글이 없는 경우")
    @Test
    void getCommentsByPostId_Empty() throws Exception {
        mockMvc.perform(get("/api/comments/post/" + testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @DisplayName("댓글 생성")
    @Test
    void createComment_Success() throws Exception {
        String requestBody = String.format("""
            {
                "content": "새 댓글입니다!",
                "post": {"id": %d}
            }
            """, testPost.getId());

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("새 댓글입니다!"));

        assertThat(commentRepository.findByPostId(testPost.getId())).hasSize(1);
    }

    @DisplayName("댓글 수정")
    @Test
    void updateComment_Success() throws Exception {
        Comment comment = Comment.builder()
                .content("기존 댓글")
                .post(testPost)
                .author(testUser)
                .build();
        comment = commentRepository.save(comment);

        mockMvc.perform(put("/api/comments/" + comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"수정된 댓글입니다\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 댓글입니다"));

        Comment updated = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo("수정된 댓글입니다");
    }

    @DisplayName("댓글 삭제")
    @Test
    void deleteComment_Success() throws Exception {
        Comment comment = Comment.builder()
                .content("삭제할 댓글")
                .post(testPost)
                .author(testUser)
                .build();
        comment = commentRepository.save(comment);

        mockMvc.perform(delete("/api/comments/" + comment.getId()))
                .andExpect(status().isNoContent());

        assertThat(commentRepository.findById(comment.getId())).isEmpty();
    }

    private String getToken() throws Exception {
        String loginJson = """
            {
                "username": "testuser",
                "password": "password123"
            }
            """;
        
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
    }
}
