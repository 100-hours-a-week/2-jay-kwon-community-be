package com.jay.sapapi.service;

import com.jay.sapapi.domain.PostLike;
import com.jay.sapapi.dto.PostLikeDTO;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
public interface PostLikeService {

    PostLikeDTO get(Long postId, Long userId);

    List<PostLikeDTO> getHeartsByPost(Long postId);

    Long register(PostLikeDTO postLikeDTO);

    void remove(Long postId, Long userId);

    PostLike dtoToEntity(PostLikeDTO postLikeDTO);

    default PostLikeDTO entityToDTO(PostLike postLike) {
        return PostLikeDTO.builder()
                .id(postLike.getId())
                .postId(postLike.getPost().getId())
                .userId(postLike.getMember().getId())
                .regDate(postLike.getRegDate())
                .build();
    }

}
