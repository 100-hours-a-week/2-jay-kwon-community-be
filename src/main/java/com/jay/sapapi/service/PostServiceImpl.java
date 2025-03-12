package com.jay.sapapi.service;

import com.jay.sapapi.domain.Member;
import com.jay.sapapi.domain.Post;
import com.jay.sapapi.dto.PostDTO;
import com.jay.sapapi.repository.PostRepository;
import com.jay.sapapi.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public PostDTO get(Long postId) {
        Object result = postRepository.getPostByPostId(postId);
        if (result == null) {
            throw new NoSuchElementException("postNotFound");
        }
        Object[] arr = (Object[]) result;
        return entityToDTO((Post) arr[0], (Member) arr[1]);
    }

    @Override
    public void incrementViewCount(Long postId) {
        Optional<Post> result = postRepository.findById(postId);
        Post post = result.orElseThrow(() -> new NoSuchElementException("postNotFound"));
        post.incrementViewCount();
        postRepository.save(post);
    }

    @Override
    public List<PostDTO> getList() {
        List<Post> postList = postRepository.findAllWithWriter();
        return postList.stream().map(post -> entityToDTO(post, post.getWriter())).toList();
    }

    @Override
    public Long register(PostDTO postDTO) {
        if (postDTO.getTitle() == null) {
            throw new CustomValidationException("invalidPostTitle");
        } else if (postDTO.getContent() == null) {
            throw new CustomValidationException("invalidPostContent");
        }

        Post post = dtoToEntity(postDTO);
        Post result = postRepository.save(post);
        return result.getPostId();
    }

    @Override
    public void modify(PostDTO postDTO) {
        Optional<Post> result = postRepository.findById(postDTO.getPostId());
        Post post = result.orElseThrow(() -> new NoSuchElementException("postNotFound"));
        post.changeTitle(postDTO.getTitle());
        post.changeContent(postDTO.getContent());

        if (postDTO.getPostImageUrl() != null && !postDTO.getPostImageUrl().isEmpty()) {
            post.changePostImageUrl(postDTO.getPostImageUrl());
        }

        postRepository.save(post);
    }

    @Override
    public void remove(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("postNotFound");
        }
        postRepository.deleteById(postId);
    }

    @Override
    public Post dtoToEntity(PostDTO postDTO) {
        return Post.builder()
                .postId(postDTO.getPostId())
                .writer(Member.builder().userId(postDTO.getWriterId()).build())
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .viewCount(postDTO.getViewCount() != null ? postDTO.getViewCount() : 0)
                .postImageUrl(postDTO.getPostImageUrl())
                .build();
    }

}
