package org.godn.interactionservice.controller;

import org.godn.interactionservice.dto.LikeRequestDto;
import org.godn.interactionservice.dto.PostRequestDto;
import org.godn.interactionservice.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<UUID> createPost(
            @RequestBody PostRequestDto request
    ) {
        UUID savedPost = postService.savePost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }

    @PostMapping("{postId}/comments")
    public ResponseEntity<String> createComment(
            @PathVariable String postId
    ) {
        return ResponseEntity.ok("create COMMENT Called!!");
    }

    @PostMapping("{postId}/like")
    public ResponseEntity<String> createLike(
            @PathVariable String postId,
            @RequestBody LikeRequestDto request
    ) {
        postService.likePost(postId, request.getUserId());
        return ResponseEntity.ok("Post liked successfully");
    }
}
