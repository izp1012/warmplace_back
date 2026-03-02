package com.warmplace.config;

import com.warmplace.entity.Comment;
import com.warmplace.entity.Gallery;
import com.warmplace.entity.Post;
import com.warmplace.entity.User;
import com.warmplace.repository.CommentRepository;
import com.warmplace.repository.GalleryRepository;
import com.warmplace.repository.PostRepository;
import com.warmplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataInitializer {

    private final GalleryRepository galleryRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (galleryRepository.count() == 0) {
                initGalleries();
            }
            Optional<User> authorOpt = userRepository.findAll().stream().findFirst();
            if (authorOpt.isEmpty()) {
                log.warn("No users found. Skipping post/comment initialization.");
                return;
            }
            User author = authorOpt.get();
            if (postRepository.count() == 0) {
                initPosts(author);
            }
            if (commentRepository.count() == 0) {
                initComments(author);
            }
        };
    }

    private void initGalleries() {
        log.info("Initializing galleries from seed data");
        List<Gallery> galleries = List.of(
                Gallery.builder()
                        .name("감성 카페")
                        .description("아늑하고 따뜻한 카페를 소개하는 갤러리입니다.")
                        .coverImage("https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=600&h=400&fit=crop")
                        .postCount(12)
                        .category("카페")
                        .build(),
                Gallery.builder()
                        .name("자연 힐링")
                        .description("숲, 바다, 산 등 자연에서 힐링할 수 있는 곳들입니다.")
                        .coverImage("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&h=400&fit=crop")
                        .postCount(8)
                        .category("자연")
                        .build(),
                Gallery.builder()
                        .name("맛집 탐방")
                        .description("특별한 음식과 추억을 만들어주는 맛집들입니다.")
                        .coverImage("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=600&h=400&fit=crop")
                        .postCount(15)
                        .category("음식")
                        .build(),
                Gallery.builder()
                        .name("여행 일기")
                        .description("다녀온 여행지에서의 특별한 순간들을 기록합니다.")
                        .coverImage("https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=600&h=400&fit=crop")
                        .postCount(20)
                        .category("여행")
                        .build(),
                Gallery.builder()
                        .name("책 읽는 공간")
                        .description("책과 함께하는 여유로운 시간의 공간입니다.")
                        .coverImage("https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=600&h=400&fit=crop")
                        .postCount(6)
                        .category("문화")
                        .build(),
                Gallery.builder()
                        .name("나의 방")
                        .description("내가 사랑하는 개인 공간을 소개합니다.")
                        .coverImage("https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600&h=400&fit=crop")
                        .postCount(9)
                        .category("인테리어")
                        .build()
        );
        galleryRepository.saveAll(galleries);
    }

    private void initPosts(User author) {
        log.info("Initializing posts from seed data");
        List<Gallery> galleries = galleryRepository.findAll();
        Gallery g1 = galleries.get(0);
        Gallery g2 = galleries.get(1);
        Gallery g3 = galleries.get(2);
        Gallery g4 = galleries.get(3);

        List<Post> posts = List.of(
                Post.builder()
                        .gallery(g1)
                        .title("항정에서 느낀 따뜻함")
                        .content("처음 이 카페에 들어섰을 때, 바닥에 깔린 따뜻한 나무 바닥과 함께 부드러운 음악이 흘러나왔습니다. 사장님이 직접 내려주신 라떼는 너무 예쁜 라터링이 되어 있었고, 창밖으로 보이는 정원의 나무들이 계절의 변화를 알려주고 있었습니다. 이렇게 일상에서 잠시 벗어나 나만의 시간을 보낼 수 있는 곳, 추천합니다.")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&h=600&fit=crop",
                                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&h=600&fit=crop"
                        ))
                        .author(author)
                        .likes(24)
                        .build(),
                Post.builder()
                        .gallery(g1)
                        .title("밤의 몰디브")
                        .content("야간에 방문한 이 카페는 또 다른 매력을 발산했습니다. 어둠 속에서 부드럽게 빛나는 조명과 함께 일출을 즐기며 읽던 책을 읽었습니다. 커피 맛도 정말 좋았고 무엇보다 조용해서 딱 좋은 곳이었습니다.")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800&h=600&fit=crop"
                        ))
                        .author(author)
                        .likes(18)
                        .build(),
                Post.builder()
                        .gallery(g1)
                        .title("디저트가 이색적인甜")
                        .content("이 카페의 케이크는 그냥 먹는 게 아니라 감상해야 합니다. 생크림이 정말 부드럽고 과일의 신선함이 그대로 전달됩니다. 특히 시즌마다 다른 디저트가 나와서,每一次 방문이 새롭습니다.")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=800&h=600&fit=crop"
                        ))
                        .author(author)
                        .likes(31)
                        .build(),
                Post.builder()
                        .gallery(g2)
                        .title("속초 해변의 일출")
                        .content("凌晨 5시에 일어난 것은 이景色를 보기 위함이었다. 해가 떠오를 때 바다 위로 금빛이 펼쳐지는 모습은 말로는 다 표현할 수 없다. 찬란히 빛나던 해가 내 마음까지 따뜻하게 해주었다.")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&h=600&fit=crop",
                                "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=800&h=600&fit=crop"
                        ))
                        .author(author)
                        .likes(45)
                        .build(),
                Post.builder()
                        .gallery(g2)
                        .title("남이섬에서 만난 가을")
                        .content("단풍이最美的 시절에 방문했습니다. 나무들이 주황색과 赤색으로 물들어 있고,湖면에 비치는 풍경은 그림처럼 아름다웠습니다. 힐링이 하고 싶다면 강추입니다!")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=600&fit=crop"
                        ))
                        .author(author)
                        .likes(38)
                        .build(),
                Post.builder()
                        .gallery(g3)
                        .title("역전할머니맥주")
                        .content("이 집의 대표 메뉴는 물론 할머니 맥주! 그리고 accompanying 해산물. 전사로 나온 새우는 너무나 신선했고, 본 메뉴인 해산물 튀김은 바삭바삭 너무 맛있었습니다. 특히 양념이 일품이었습니다.")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&h=600&fit=crop",
                                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&h=600&fit=crop"
                        ))
                        .author(author)
                        .likes(52)
                        .build(),
                Post.builder()
                        .gallery(g3)
                        .title("또cery 이ayakan 라멘")
                        .content("오랜만에 찾는 라멘집. 진한 국물과 면이 완벽하게 조화를 이루고 있습니다. 특히 표고버섯이 들어가 있어서 깊은 맛이 납니다. 가면 반드시 추천 메뉴를 드세요!")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&h=600&fit=crop"
                        ))
                        .author(author)
                        .likes(27)
                        .build(),
                Post.builder()
                        .gallery(g4)
                        .title("큐슈 여행")
                        .content("일본의 큐슈 지방으로 여행을 떠났습니다. 특히 후쿠오카의 거리가 정말 아늑했습니다. 가볍게 돌아다니기 좋았고, 특히 가라아게가 맛있었습니다. 다음에는 온천도 가보고 싶어요!")
                        .images(List.of(
                                "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&h=600&fit=crop",
                                "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800&h=600&fit=crop"
                        ))
                        .author(author)
                        .likes(33)
                        .build()
        );

        postRepository.saveAll(posts);
    }

    private void initComments(User author) {
        log.info("Initializing comments from seed data");
        List<Post> posts = postRepository.findAll();
        if (posts.isEmpty()) {
            return;
        }
        Post p1 = posts.get(0);
        Post p4 = posts.size() > 3 ? posts.get(3) : p1;
        Post p6 = posts.size() > 5 ? posts.get(5) : p1;
        Post p8 = posts.size() > 7 ? posts.get(7) : posts.get(posts.size() - 1);

        List<Comment> comments = List.of(
                Comment.builder()
                        .post(p1)
                        .author(author)
                        .content("정말 아름다운 곳이네요! 다음에 저도 가봐야겠습니다.")
                        .build(),
                Comment.builder()
                        .post(p1)
                        .author(author)
                        .content("라떼 라터링이 정말 예쁘네요! 어떤 라터링인지 궁금합니다.")
                        .build(),
                Comment.builder()
                        .post(p4)
                        .author(author)
                        .content("역시 바다의 일출은 최고입니다! 저도 가보고 싶어요.")
                        .build(),
                Comment.builder()
                        .post(p4)
                        .author(author)
                        .content("사진이 정말 잘나왔네요. 어떤 카메라로 찍으셨나요?")
                        .build(),
                Comment.builder()
                        .post(p6)
                        .author(author)
                        .content("해산물 튀김 유명한 곳이죠! 다음에 가봐야겠습니다.")
                        .build(),
                Comment.builder()
                        .post(p8)
                        .author(author)
                        .content("큐슈 저도 가려고 계획중이에요! 추천해주신 곳 있어서 좋습니다.")
                        .build()
        );

        commentRepository.saveAll(comments);
    }
}

package com.warmplace.config;

import com.warmplace.entity.*;
import com.warmplace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
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
        if (userRepository.count() == 0) {
            initUsers();
            initGalleries();
            initPosts();
            initComments();
            log.info("=== 모든 더미 데이터 생성 완료 ===");
        } else {
            log.info("=== 기존 데이터가 존재하여 스킵 ===");
        }
    }

    private void initUsers() {
        List<User> users = Arrays.asList(
            createUser("user1", "user1@example.com", "password123", "따뜻한나"),
            createUser("user2", "user2@example.com", "password123", "밤을 좋아하는"),
            createUser("user3", "user3@example.com", "password123", "달콤한나"),
            createUser("user4", "user4@example.com", "password123", "바다의 아기"),
            createUser("user5", "user5@example.com", "password123", "산책러"),
            createUser("user6", "user6@example.com", "password123", "배달하는펭귄"),
            createUser("user7", "user7@example.com", "password123", "라멘러버"),
            createUser("user8", "user8@example.com", "password123", "여행쟁이")
        );
        userRepository.saveAll(users);
        log.info("=== 더미 사용자 데이터 {}개 생성 완료 ===", users.size());
    }

    private void initGalleries() {
        List<Gallery> galleries = Arrays.asList(
            Gallery.builder()
                .name("감성 카페")
                .description("아늑하고 따뜻한 카페를 소개하는 갤러리입니다.")
                .coverImage("https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=600&h=400&fit=crop")
                .postCount(3)
                .category("카페")
                .build(),
            Gallery.builder()
                .name("자연 힐링")
                .description("숲, 바다, 산 등 자연에서 힐링할 수 있는 곳들입니다.")
                .coverImage("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&h=400&fit=crop")
                .postCount(2)
                .category("자연")
                .build(),
            Gallery.builder()
                .name("맛집 탐방")
                .description("특별한 음식과 추억을 만들어주는 맛집들입니다.")
                .coverImage("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=600&h=400&fit=crop")
                .postCount(2)
                .category("음식")
                .build(),
            Gallery.builder()
                .name("여행 일기")
                .description("다녀온 여행지에서의 특별한 순간들을 기록합니다.")
                .coverImage("https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=600&h=400&fit=crop")
                .postCount(1)
                .category("여행")
                .build(),
            Gallery.builder()
                .name("책 읽는 공간")
                .description("책과 함께하는 여유로운 시간의 공간입니다.")
                .coverImage("https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=600&h=400&fit=crop")
                .postCount(0)
                .category("문화")
                .build(),
            Gallery.builder()
                .name("나의 방")
                .description("내가 사랑하는 개인 공간을 소개합니다.")
                .coverImage("https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600&h=400&fit=crop")
                .postCount(0)
                .category("인테리어")
                .build()
        );
        galleryRepository.saveAll(galleries);
        log.info("=== 더미 갤러리 데이터 {}개 생성 완료 ===", galleries.size());
    }

    private void initPosts() {
        List<User> users = userRepository.findAll();
        List<Gallery> galleries = galleryRepository.findAll();

        User user1 = users.get(0);
        User user2 = users.get(1);
        User user3 = users.get(2);
        User user4 = users.get(3);
        User user5 = users.get(4);
        User user6 = users.get(5);
        User user7 = users.get(6);
        User user8 = users.get(7);

        Gallery cafeGallery = galleries.get(0);
        Gallery natureGallery = galleries.get(1);
        Gallery foodGallery = galleries.get(2);
        Gallery travelGallery = galleries.get(3);

        List<Post> posts = Arrays.asList(
            Post.builder()
                .gallery(cafeGallery)
                .title("항정에서 느낀 따뜻함")
                .content("처음 이 카페에 들어섰을 때, 바닥에 깔린 따뜻한 나무 바닥과 함께 부드러운 음악이 흘러나왔습니다. 사장님이 직접 내려주신 라떼는 너무 예쁜 라터링이 되어 있었고, 창밖으로 보이는 정원의 나무들이 계절의 변화를 알려주고 있었습니다. 이렇게 일상에서 잠시 벗어나 나만의 시간을 보낼 수 있는 곳, 추천합니다.")
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&h=600&fit=crop",
                    "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&h=600&fit=crop"
                ))
                .author(user1)
                .likes(24)
                .build(),
            Post.builder()
                .gallery(cafeGallery)
                .title("밤의 몰디브")
                .content("야간에 방문한 이 카페는 또 다른 매력을 발산했습니다. 어둠 속에서 부드럽게 빛나는 조명과 함께 일출을 즐기며 읽던 책을 읽었습니다. 커피 맛도 정말 좋았고 무엇보다 조용해서 딱 좋은 곳이었습니다.")
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800&h=600&fit=crop"
                ))
                .author(user2)
                .likes(18)
                .build(),
            Post.builder()
                .gallery(cafeGallery)
                .title("디저트가 이색적인甜")
                .content("이 카페의 케이크는 그냥 먹는 게 아니라 감상해야 합니다. 생크림이 정말 부드럽고 과일의 신선함이 그대로 전달됩니다. 특히 시즌마다 다른 디저트가 나와서,每一次 방문이 새롭습니다.")
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=800&h=600&fit=crop"
                ))
                .author(user3)
                .likes(31)
                .build(),
            Post.builder()
                .gallery(natureGallery)
                .title("속초 해변의 일출")
                .content("凌晨 5시에 일어난 것은 이景色를 보기 위함이었다. 해가 떠오를 때 바다 위로 금빛이 펼쳐지는 모습은 말로는 다 표현할 수 없다. 찬란히 빛나던 해가 내 마음까지 따뜻하게 해주었다.")
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&h=600&fit=crop",
                    "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=800&h=600&fit=crop"
                ))
                .author(user4)
                .likes(45)
                .build(),
            Post.builder()
                .gallery(natureGallery)
                .title("남이섬에서 만난 가을")
                .content("단풍이最美的 시절에 방문했습니다. 나무들이 주황색과 赤색으로 물들어 있고,湖면에 비치는 풍경은 그림처럼 아름다웠습니다. 힐링이 하고 싶다면 강추입니다!")
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=600&fit=crop"
                ))
                .author(user5)
                .likes(38)
                .build(),
            Post.builder()
                .gallery(foodGallery)
                .title("역전할머니맥주")
                .content("이 집의 대표 메뉴는 물론 할머니 맥주! 그리고 accompanying 해산물. 전사로 나온 새우는 너무나 신선했고, 본 메뉴인 해산물 튀김은 바삭바삭 너무 맛있었습니다. 특히 양념이 일품이었습니다.")
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&h=600&fit=crop",
                    "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&h=600&fit=crop"
                ))
                .author(user6)
                .likes(52)
                .build(),
            Post.builder()
                .gallery(foodGallery)
                .title("또cery 이ayakan 라멘")
                .content("오랜만에 찾는 라멘집. 진한 국물과 면이 완벽하게 조화를 이루고 있습니다. 특히 표고버섯이 들어가 있어서 깊은 맛이 납니다. 가면 반드시 추천 메뉴를 드세요!")
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&h=600&fit=crop"
                ))
                .author(user7)
                .likes(27)
                .build(),
            Post.builder()
                .gallery(travelGallery)
                .title("큐슈 여행")
                .content("일본의 큐슈 지방으로 여행을 떠났습니다. 특히 후쿠오카의 거리가 정말 아늑했습니다. 가볍게 돌아다니기 좋았고, 특히 가라아게가 맛있었습니다. 다음에는 온천도 가보고 싶어요!")
                .images(Arrays.asList(
                    "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&h=600&fit=crop",
                    "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800&h=600&fit=crop"
                ))
                .author(user8)
                .likes(33)
                .build()
        );
        postRepository.saveAll(posts);
        log.info("=== 더미 게시글 데이터 {}개 생성 완료 ===", posts.size());
    }

    private void initComments() {
        List<User> users = userRepository.findAll();
        List<Post> posts = postRepository.findAll();

        Post post1 = posts.get(0);
        Post post4 = posts.get(3);
        Post post6 = posts.get(5);
        Post post8 = posts.get(7);

        List<Comment> comments = Arrays.asList(
            Comment.builder()
                .post(post1)
                .author(users.get(0))
                .content("정말 아름다운 곳이네요! 다음에 저도 가봐야겠습니다.")
                .build(),
            Comment.builder()
                .post(post1)
                .author(users.get(1))
                .content("라떼 라터링이 정말 예쁘네요! 어떤 라터링인지 궁금합니다.")
                .build(),
            Comment.builder()
                .post(post4)
                .author(users.get(2))
                .content("역시 바다의 일출은 최고입니다! 저도 가보고 싶어요.")
                .build(),
            Comment.builder()
                .post(post4)
                .author(users.get(3))
                .content("사진이 정말 잘나왔네요. 어떤 카메라로 찍으셨나요?")
                .build(),
            Comment.builder()
                .post(post6)
                .author(users.get(4))
                .content("해산물 튀김 유명한 곳이죠! 다음에 가봐야겠습니다.")
                .build(),
            Comment.builder()
                .post(post8)
                .author(users.get(5))
                .content("큐슈 저도 가려고 계획중이에요! 추천해주신 곳 있어서 좋습니다.")
                .build()
        );
        commentRepository.saveAll(comments);
        log.info("=== 더미 댓글 데이터 {}개 생성 완료 ===", comments.size());
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
