package com.warmplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warmplace.dto.PostResponse;
import com.warmplace.entity.Gallery;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GalleryRepository galleryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private User testUser;
    private Gallery testGallery;

    @BeforeEach
    void setUp() throws Exception {
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

        testGallery = Gallery.builder()
                .name("테스트 갤러리")
                .description("설명")
                .category("general")
                .postCount(0)
                .build();
        galleryRepository.save(testGallery);

        token = getToken();
    }

    @DisplayName("모든 게시글 조회")
    @Test
    void getAllPosts_Success() throws Exception {
        Post post1 = Post.builder()
                .title("제목1")
                .content("내용1")
                .author(testUser)
                .gallery(testGallery)
                .likes(0)
                .build();
        Post post2 = Post.builder()
                .title("제목2")
                .content("내용2")
                .author(testUser)
                .gallery(testGallery)
                .likes(5)
                .build();
        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[1].title").exists());
    }

    @DisplayName("게시글 ID로 조회")
    @Test
    void getPostById_Success() throws Exception {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .author(testUser)
                .gallery(testGallery)
                .likes(10)
                .build();
        post = postRepository.save(post);

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.content").value("내용"))
                .andExpect(jsonPath("$.likes").value(10));
    }

    @DisplayName("게시글 ID로 조회 - 존재하지 않는 경우")
    @Test
    void getPostById_NotFound() throws Exception {
        mockMvc.perform(get("/api/posts/99999"))
                .andExpect(status().isInternalServerError());
    }

    @DisplayName("갤러리별 게시글 조회")
    @Test
    void getPostsByGalleryId_Success() throws Exception {
        Gallery gallery2 = Gallery.builder()
                .name("다른 갤러리")
                .category("other")
                .postCount(0)
                .build();
        galleryRepository.save(gallery2);

        Post post1 = Post.builder()
                .title("갤러리1 글")
                .content("내용")
                .author(testUser)
                .gallery(testGallery)
                .likes(0)
                .build();
        Post post2 = Post.builder()
                .title("갤러리2 글")
                .content("내용")
                .author(testUser)
                .gallery(gallery2)
                .likes(0)
                .build();
        postRepository.save(post1);
        postRepository.save(post2);

        mockMvc.perform(get("/api/posts/gallery/" + testGallery.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("갤러리1 글"));
    }

    @DisplayName("게시글 생성")
    @Test
    void createPost_Success() throws Exception {
        String requestBody = String.format("""
            {
                "title": "새 게시글",
                "content": "새 내용",
                "gallery": {"id": %d}
            }
            """, testGallery.getId());

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("새 게시글"))
                .andExpect(jsonPath("$.content").value("새 내용"));

        assertThat(postRepository.findAll()).hasSize(1);
    }

    @DisplayName("게시글 수정")
    @Test
    void updatePost_Success() throws Exception {
        Post post = Post.builder()
                .title("기존 제목")
                .content("기존 내용")
                .author(testUser)
                .gallery(testGallery)
                .likes(0)
                .build();
        post = postRepository.save(post);

        String requestBody = """
            {
                "title": "수정된 제목",
                "content": "수정된 내용"
            }
            """;

        mockMvc.perform(put("/api/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @DisplayName("게시글 좋아요 증가")
    @Test
    void incrementLikes_Success() throws Exception {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .author(testUser)
                .gallery(testGallery)
                .likes(5)
                .build();
        post = postRepository.save(post);

        mockMvc.perform(post("/api/posts/" + post.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(6));

        Post updated = postRepository.findById(post.getId()).orElseThrow();
        assertThat(updated.getLikes()).isEqualTo(6);
    }

    @DisplayName("게시글 삭제")
    @Test
    void deletePost_Success() throws Exception {
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .author(testUser)
                .gallery(testGallery)
                .likes(0)
                .build();
        post = postRepository.save(post);

        mockMvc.perform(delete("/api/posts/" + post.getId()))
                .andExpect(status().isNoContent());

        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    private String getToken() throws Exception {
        String loginJson = """
            {
                "username": "testuser",
                "password": "password123"
            }
            """;
        
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        return response.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
    }
}
