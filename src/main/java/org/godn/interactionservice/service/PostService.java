    package org.godn.interactionservice.service;

    import jakarta.transaction.Transactional;
    import lombok.extern.slf4j.Slf4j;
    import org.godn.interactionservice.dto.request.CommentRequestDto;
    import org.godn.interactionservice.dto.request.PostRequestDto;
    import org.godn.interactionservice.dto.response.CommentResponseDto;
    import org.godn.interactionservice.entity.Comment;
    import org.godn.interactionservice.entity.Post;
    import org.godn.interactionservice.exception.UnauthorizedUserException;
    import org.godn.interactionservice.repository.BotRepository;
    import org.godn.interactionservice.repository.CommentRepository;
    import org.godn.interactionservice.repository.PostRepository;
    import org.godn.interactionservice.repository.UserRepository;
    import org.springframework.stereotype.Service;

    import java.time.Duration;
    import java.util.UUID;

    @Slf4j
    @Service
    public class PostService {
        private final RedisInteractionService redisInteractionService;
        private final UserRepository userRepository;
        private final PostRepository postRepository;
        private final CommentRepository commentRepository;
        private final BotRepository botRepository;


        public PostService(
                RedisInteractionService redisInteractionService,
                UserRepository userRepository,
                PostRepository postRepository,
                CommentRepository commentRepository,
                BotRepository botRepository
        ) {
            this.redisInteractionService = redisInteractionService;
            this.userRepository = userRepository;
            this.postRepository = postRepository;
            this.commentRepository = commentRepository;
            this.botRepository = botRepository;
        }

        @Transactional
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
                Long USER_LIKE_SCORE = 20L;
                redisInteractionService.increaseScore(postId, USER_LIKE_SCORE);
            }
        }

        @Transactional
        public CommentResponseDto addComment(String postIdStr, CommentRequestDto request) {
            UUID postId = UUID.fromString(postIdStr);
            UUID authorId = UUID.fromString(request.getAuthorId());

            int depth = 1;

            // If parent comment is provided null the reply will be considered direct reply to post...
            if (request.getParentCommentId() != null) {
                UUID parentId = UUID.fromString(request.getParentCommentId());

                Integer parentDepth = commentRepository.findDepthLevelById(parentId);
                if (parentDepth == null) throw new RuntimeException("Parent comment not found");

                depth = parentDepth + 1;
            }

            if (depth > 20) {
                throw new RuntimeException("Comment thread too deep.");
            }

            boolean isUser = userRepository.existsById(authorId);

            if(!isUser) {
//                if(!botRepository.existsById(authorId)) {
//                    log.info("Unauthorized interaction attempt by ID: {}", authorId);
//                    throw new UnauthorizedUserException("Unauthorized: Author does not exist.");
//                }
                UUID postAuthorId = postRepository.findAuthorIdById(postId);
                if(postAuthorId == null) throw new RuntimeException("Post not found");

                redisInteractionService.limitBotInteraction(postIdStr, 100);
                redisInteractionService.botInteractionCooldown(authorId.toString(), postAuthorId.toString(), Duration.ofMinutes(10));
            }

            Comment comment = new Comment();

            Post postReference = postRepository.getReferenceById(postId); // proxy object to avoid unnecessary DB hit...
            comment.setPost(postReference);
            comment.setAuthorId(authorId);
            comment.setContent(request.getContent());
            comment.setDepthLevel(depth);
            commentRepository.save(comment);

            // 5. VIRALITY SCORE
            //        long BOT_LIKE_SCORE = 0L;
            long USER_COMMENT_SCORE = 50L;
            long BOT_COMMENT_SCORE = 1L;
            Long score = isUser ? USER_COMMENT_SCORE : BOT_COMMENT_SCORE;
            redisInteractionService.increaseScore(postIdStr, score);

            CommentResponseDto response = new CommentResponseDto();
            response.setId(comment.getId());
            response.setAuthorId(comment.getAuthorId());
            response.setContent(comment.getContent());
            response.setDepthLevel(comment.getDepthLevel());

            return response;
        }
    }
