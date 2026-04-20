package org.godn.interactionservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @PostMapping
    public ResponseEntity createPost() {
        return ResponseEntity.ok("create POST Called!!");
    }

    @PostMapping("{postId}/comments")
    public ResponseEntity createComment(
            @PathVariable String postId
    ) {
        return ResponseEntity.ok("create COMMENT Called!!");
    }

    @PostMapping("{postId}/like")
    public ResponseEntity createLike(
            @PathVariable String postId
    ) {
        return ResponseEntity.ok("create LIKE Called!!");
    }
}
