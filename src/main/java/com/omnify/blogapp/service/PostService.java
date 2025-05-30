package com.omnify.blogapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.omnify.blogapp.dto.PostDto;
import com.omnify.blogapp.dto.PostRequest;
import com.omnify.blogapp.exception.*;
import com.omnify.blogapp.model.Post;
import com.omnify.blogapp.model.User;
import com.omnify.blogapp.repository.PostRepository;
import com.omnify.blogapp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<PostDto> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::convertToDto);
    }

    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return convertToDto(post);
    }

    public PostDto createPost(PostRequest postRequest) {
        User currentUser = getCurrentUser();

        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setAuthor(currentUser);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepository.save(post);
        return convertToDto(savedPost);
    }

    public PostDto updatePost(Long id, PostRequest postRequest) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        User currentUser = getCurrentUser();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }

        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);
        return convertToDto(updatedPost);
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        User currentUser = getCurrentUser();
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    private PostDto convertToDto(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());
        postDto.setCreatedAt(post.getCreatedAt());
        postDto.setUpdatedAt(post.getUpdatedAt());
        postDto.setAuthorEmail(post.getAuthorEmail());
        return postDto;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}