    package org.godn.interactionservice.service;

    import org.godn.interactionservice.dto.CommentRequestDto;
    import org.godn.interactionservice.dto.PostRequestDto;
    import org.godn.interactionservice.entity.Comment;
    import org.godn.interactionservice.entity.Post;
    import org.godn.interactionservice.repository.CommentRepository;
    import org.godn.interactionservice.repository.PostRepository;
    import org.godn.interactionservice.repository.UserRepository;
    import org.springframework.stereotype.Service;

    import java.time.Duration;
    import java.util.UUID;

    @Service
    public class PostService {
        private final RedisInteractionService redisInteractionService;
        private final UserRepository userRepository;
        private final PostRepository postRepository;
        private final CommentRepository commentRepository;


        //        private final Long BOT_LIKE_SCORE = 0L;
        private final Long USER_COMMENT_SCORE = 50L;
        private final Long BOT_COMMENT_SCORE = 1L;


        public PostService(
                RedisInteractionService redisInteractionService,
                UserRepository userRepository,
                PostRepository postRepository,
                CommentRepository commentRepository
        ) {
            this.redisInteractionService = redisInteractionService;
            this.userRepository = userRepository;
            this.postRepository = postRepository;
            this.commentRepository = commentRepository;
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
                Long USER_LIKE_SCORE = 20L;
                redisInteractionService.increaseScore(postId, USER_LIKE_SCORE);
            }
        }

        public void addComment(String postIdStr, CommentRequestDto request) {
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
                UUID postAuthorId = postRepository.findAuthorIdById(postId);
                if(postAuthorId == null) throw new RuntimeException("Post not found");

                redisInteractionService.botInteractionCooldown(authorId.toString(), postAuthorId.toString(), Duration.ofMinutes(10));
                redisInteractionService.limitBotInteraction(postIdStr, 100);
            }

            Comment comment = new Comment();

            Post postReference = postRepository.getReferenceById(postId); // proxy object to avoid unnecessary DB hit...
            comment.setPost(postReference);
            comment.setAuthorId(authorId);
            comment.setContent(request.getContent());
            comment.setDepthLevel(depth);
            commentRepository.save(comment);

            // 5. VIRALITY SCORE
            Long scoreDelta = isUser ? USER_COMMENT_SCORE : BOT_COMMENT_SCORE;
            redisInteractionService.increaseScore(postIdStr, scoreDelta);
        }
    }
