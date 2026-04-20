    package org.godn.interactionservice.service;

    import org.godn.interactionservice.dto.LikeRequestDto;
    import org.godn.interactionservice.dto.PostRequestDto;
    import org.godn.interactionservice.entity.Post;
    import org.godn.interactionservice.repository.PostRepository;
    import org.godn.interactionservice.repository.UserRepository;
    import org.springframework.stereotype.Service;

    import java.util.UUID;

    @Service
    public class PostService {
        private final ViralityService viralityService;
        private final UserRepository userRepository;
        private final PostRepository postRepository;


        private final Long USER_LIKE_SCORE = 20L;
//        private final Long BOT_LIKE_SCORE = 0L;
        private final Long USER_COMMENT_SCORE = 50L;
        private final Long BOT_COMMENT_SCORE = 1L;


        public PostService(
                ViralityService viralityService,
                UserRepository userRepository,
                PostRepository postRepository
        ) {
            this.viralityService = viralityService;
            this.userRepository = userRepository;
            this.postRepository = postRepository;
        }

        public UUID savePost(PostRequestDto request) {
            Post post = new Post();
            post.setAuthorId(UUID.fromString(request.getAuthorId()));
            post.setContent(request.getContent());

            return postRepository.save(post).getId();
            // Not using builder for now....
        }

        public void likePost(String postId, String userId) {
            boolean isUser = userRepository.existsById(UUID.fromString(userId));

            if(isUser) {
                viralityService.increaseScore(postId, USER_LIKE_SCORE);
            }
        }

    }
