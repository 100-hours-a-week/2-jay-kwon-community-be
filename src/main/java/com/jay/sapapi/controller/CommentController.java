package com.jay.sapapi.controller;

import com.jay.sapapi.dto.comment.request.CommentCreateRequestDTO;
import com.jay.sapapi.dto.comment.request.CommentModifyRequestDTO;
import com.jay.sapapi.dto.comment.response.CommentResponseDTO;
import com.jay.sapapi.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public Map<String, Object> get(@PathVariable Long commentId) {
        CommentResponseDTO dto = commentService.get(commentId);
        return Map.of("message", "success", "data", dto);
    }

    @PostMapping("/")
    @PreAuthorize("#dto.userId == authentication.principal.userId")
    public ResponseEntity<?> register(@Valid CommentCreateRequestDTO dto) {
        long commentId = commentService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "registerSuccess", "data", Map.of("id", commentId)));
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("#dto.userId == authentication.principal.userId")
    public Map<String, Object> modify(@PathVariable Long commentId, @Valid CommentModifyRequestDTO dto) {
        commentService.modify(commentId, dto);
        return Map.of("message", "modifySuccess");
    }

    @DeleteMapping("/{commentId}")
    public Map<String, Object> remove(@PathVariable Long commentId) {
        commentService.remove(commentId);
        return Map.of("message", "removeSuccess");
    }

}
