package org.godn.interactionservice.controller;

import org.godn.interactionservice.dto.*;
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
    public ResponseEntity<Response> createPost(
            @RequestBody PostRequestDto request
    ) {
        UUID savedPost = postService.savePost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Response(true, savedPost.toString()));
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
    public ResponseEntity<Response> createLike(
            @PathVariable String postId,
            @RequestBody LikeRequestDto request
    ) {
        postService.likePost(postId, request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new Response(true, "Liked Post Successfully!!!"));    }
}
