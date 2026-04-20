    package org.godn.interactionservice.service;

    import org.godn.interactionservice.dto.RequestDto;
    import org.godn.interactionservice.repository.UserRepository;
    import org.springframework.stereotype.Service;

    import java.util.UUID;

    @Service
    public class PostService {
        private final ViralityService viralityService;
        private final UserRepository userRepository;


        private final Long USER_LIKE_SCORE = 20L;
//        private final Long BOT_LIKE_SCORE = 0L;
        private final Long USER_COMMENT_SCORE = 50L;
        private final Long BOT_COMMENT_SCORE = 1L;


        public PostService(
                ViralityService viralityService,
                UserRepository userRepository
        ) {
            this.viralityService = viralityService;
            this.userRepository = userRepository;
        }

        public void likePost(RequestDto request) {
            boolean isUser = userRepository.existsById(UUID.fromString(request.getAuthorId()));

            if(isUser) {
                viralityService.increaseScore(request.getPostId(), USER_LIKE_SCORE);
            }
        }


    }
