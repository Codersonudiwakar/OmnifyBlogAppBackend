package com.omnify.blogapp.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.omnify.blogapp.dto.PostDto;
import com.omnify.blogapp.dto.PostRequest;
import com.omnify.blogapp.service.PostService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public Page<PostDto> getAllPosts(Pageable pageable) {
        return postService.getAllPosts(pageable);
    }

    @GetMapping("/{id}")
    public PostDto getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public PostDto createPost(@RequestBody PostRequest postRequest) {
        return postService.createPost(postRequest);
    }

    @PreAuthorize("@postSecurity.isAuthor(#id, authentication.name)")
    @PutMapping("/{id}")
    public PostDto updatePost(@PathVariable Long id, @RequestBody PostRequest postRequest) {
        return postService.updatePost(id, postRequest);
    }

    @PreAuthorize("@postSecurity.isAuthor(#id, authentication.name)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Post deleted successfully");
    }
}

