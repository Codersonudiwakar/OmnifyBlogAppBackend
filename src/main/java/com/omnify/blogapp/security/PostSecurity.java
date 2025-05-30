package com.omnify.blogapp.security;

import com.omnify.blogapp.model.Post;
import com.omnify.blogapp.repository.PostRepository;
import org.springframework.stereotype.Component;

@Component("postSecurity")
public class PostSecurity {

    private final PostRepository postRepository;

    public PostSecurity(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public boolean isAuthor(Long postId, String username) {
        return postRepository.findById(postId)
            .map(post -> post.getAuthor().getEmail().equals(username)) // assuming username = email
            .orElse(false);
    }
}
