package com.jay.sapapi.controller;

import com.jay.sapapi.dto.post.request.PostCreateRequestDTO;
import com.jay.sapapi.dto.post.request.PostModifyRequestDTO;
import com.jay.sapapi.dto.post.response.PostResponseDTO;
import com.jay.sapapi.service.CommentService;
import com.jay.sapapi.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    private final CommentService commentService;

    @GetMapping("/{postId}")
    public Map<String, Object> get(@PathVariable Long postId) {
        postService.incrementViewCount(postId);
        PostResponseDTO dto = postService.get(postId);
        return Map.of("message", "success", "data", dto);
    }

    @GetMapping("/{postId}/comments")
    public Map<String, Object> getCommentsByPost(@PathVariable Long postId) {
        return Map.of("message", "success", "data", commentService.getCommentsByPostId(postId));
    }

    @GetMapping("/")
    public Map<String, Object> getAll() {
        List<PostResponseDTO> result = postService.getList();
        return Map.of("message", "success", "data", result);
    }

    @PostMapping("/")
    @PreAuthorize("#dto.userId == authentication.principal.userId")
    public ResponseEntity<?> register(@Valid PostCreateRequestDTO dto) {
        long postId = postService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "registerSuccess", "data", Map.of("id", postId)));
    }

    @PutMapping("/{postId}")
    @PreAuthorize("#dto.userId == authentication.principal.userId")
    public Map<String, Object> modify(@PathVariable Long postId, @Valid PostModifyRequestDTO dto) {
        postService.modify(postId, dto);
        return Map.of("message", "modifySuccess");
    }

    @DeleteMapping("/{postId}")
    public Map<String, Object> remove(@PathVariable Long postId) {
        postService.remove(postId);
        return Map.of("message", "postDeleted");
    }

}
