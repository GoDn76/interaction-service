package org.godn.interactionservice.controller;

import org.godn.interactionservice.dto.request.CommentRequestDto;
import org.godn.interactionservice.dto.request.LikeRequestDto;
import org.godn.interactionservice.dto.request.PostRequestDto;
import org.godn.interactionservice.dto.response.CommentResponseDto;
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
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable String postId,
            @RequestBody CommentRequestDto request
    ) {
        CommentResponseDto response = postService.addComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
