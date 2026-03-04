package com.warmplace.config;

import com.warmplace.entity.*;
import com.warmplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
//@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GalleryRepository galleryRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("=== 기존 데이터 존재 → 더미 데이터 생성 스킵 ===");
            return;
        }

        initUsers();
        initGalleries();
        initPosts();
        initComments();

        log.info("=== 모든 더미 데이터 생성 완료 ===");
    }

    private void initUsers() {
        List<User> users = Arrays.asList(
                createUser("user1", "user1@example.com", "password123", "따뜻한나"),
                createUser("user2", "user2@example.com", "password123", "밤을좋아해"),
                createUser("user3", "user3@example.com", "password123", "달콤한나"),
                createUser("user4", "user4@example.com", "password123", "바다소년"),
                createUser("user5", "user5@example.com", "password123", "산책러"),
                createUser("user6", "user6@example.com", "password123", "배달펭귄"),
                createUser("user7", "user7@example.com", "password123", "라멘러버"),
                createUser("user8", "user8@example.com", "password123", "여행쟁이")
        );
        userRepository.saveAll(users);
    }

    private void initGalleries() {
        List<Gallery> galleries = Arrays.asList(
                Gallery.builder()
                        .name("감성 카페")
                        .description("아늑하고 따뜻한 카페를 소개하는 공간입니다.")
                        .coverImage("https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=600&h=400&fit=crop")
                        .postCount(3)
                        .category("카페")
                        .build(),
                Gallery.builder()
                        .name("자연 힐링")
                        .description("숲과 바다, 산에서 힐링할 수 있는 장소입니다.")
                        .coverImage("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&h=400&fit=crop")
                        .postCount(2)
                        .category("자연")
                        .build(),
                Gallery.builder()
                        .name("맛집 탐방")
                        .description("특별한 음식과 추억을 남길 수 있는 맛집 모음입니다.")
                        .coverImage("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=600&h=400&fit=crop")
                        .postCount(2)
                        .category("음식")
                        .build(),
                Gallery.builder()
                        .name("여행 일기")
                        .description("여행지에서의 특별한 순간을 기록하는 공간입니다.")
                        .coverImage("https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=600&h=400&fit=crop")
                        .postCount(1)
                        .category("여행")
                        .build()
        );
        galleryRepository.saveAll(galleries);
    }

    private void initPosts() {
        List<User> users = userRepository.findAll();
        List<Gallery> galleries = galleryRepository.findAll();

        Gallery cafe = galleries.get(0);
        Gallery nature = galleries.get(1);
        Gallery food = galleries.get(2);
        Gallery travel = galleries.get(3);

        List<Post> posts = Arrays.asList(
                Post.builder()
                        .gallery(cafe)
                        .title("항정에서 느낀 따뜻함")
                        .content("처음 들어섰을 때 나무 바닥의 온기와 잔잔한 음악이 인상적이었습니다. 사장님이 내려주신 라떼와 창밖 정원의 풍경이 하루의 피로를 녹여주었습니다.")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&h=600&fit=crop"
                        ))
                        .author(users.get(0))
                        .likes(24)
                        .build(),

                Post.builder()
                        .gallery(nature)
                        .title("속초 해변의 일출")
                        .content("새벽 5시에 일어나 바라본 해변의 일출은 말로 다 표현할 수 없었습니다. 바다 위로 퍼지는 금빛 햇살이 마음까지 따뜻하게 만들었습니다.")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&h=600&fit=crop"
                        ))
                        .author(users.get(3))
                        .likes(45)
                        .build(),

                Post.builder()
                        .gallery(food)
                        .title("해산물 맛집 방문기")
                        .content("신선한 해산물과 바삭한 튀김이 인상적이었습니다. 특히 양념이 깊은 맛을 더해줘 다시 방문하고 싶은 곳입니다.")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&h=600&fit=crop"
                        ))
                        .author(users.get(5))
                        .likes(52)
                        .build(),

                Post.builder()
                        .gallery(travel)
                        .title("큐슈 여행 기록")
                        .content("후쿠오카 거리는 아늑했고 음식도 훌륭했습니다. 다음에는 온천 여행도 함께 계획해보고 싶습니다.")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&h=600&fit=crop"
                        ))
                        .author(users.get(7))
                        .likes(33)
                        .build()
        );

        postRepository.saveAll(posts);
    }

    private void initComments() {
        List<User> users = userRepository.findAll();
        List<Post> posts = postRepository.findAll();

        List<Comment> comments = Arrays.asList(
                Comment.builder()
                        .post(posts.get(0))
                        .author(users.get(1))
                        .content("분위기가 정말 좋아 보이네요. 저도 가보고 싶습니다.")
                        .build(),
                Comment.builder()
                        .post(posts.get(1))
                        .author(users.get(2))
                        .content("일출 사진이 인상적입니다. 직접 보면 더 감동일 것 같아요.")
                        .build(),
                Comment.builder()
                        .post(posts.get(2))
                        .author(users.get(4))
                        .content("해산물 정말 맛있어 보입니다. 방문 리스트에 추가해야겠어요.")
                        .build(),
                Comment.builder()
                        .post(posts.get(3))
                        .author(users.get(6))
                        .content("큐슈 여행 정보 감사합니다. 많은 도움이 되었습니다.")
                        .build()
        );

        commentRepository.saveAll(comments);
    }

    private User createUser(String username, String email, String password, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        return user;
    }
}