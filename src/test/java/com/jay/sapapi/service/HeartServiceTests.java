package com.jay.sapapi.service;

import com.github.javafaker.Faker;
import com.jay.sapapi.domain.MemberRole;
import com.jay.sapapi.dto.HeartDTO;
import com.jay.sapapi.dto.MemberDTO;
import com.jay.sapapi.dto.PostDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.NoSuchElementException;

@SpringBootTest
@Log4j2
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HeartServiceTests {

    @Autowired
    private PostService postService;

    @Autowired
    private HeartService heartService;

    @Autowired
    private MemberService memberService;

    private final Faker faker = new Faker();

    private Long postId, userId;

    @BeforeAll
    public void setup() {
        Assertions.assertNotNull(postService, "PostService should not be null");
        Assertions.assertNotNull(heartService, "HeartService should not be null");
        Assertions.assertNotNull(memberService, "MemberService should not be null");

        log.info(postService.getClass().getName());
        log.info(heartService.getClass().getName());
        log.info(memberService.getClass().getName());
    }

    @Test
    @BeforeEach
    public void testRegister() {

        MemberDTO writerDTO = MemberDTO.builder()
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .nickname(faker.name().name())
                .role(MemberRole.USER)
                .build();
        Long writerId = memberService.register(writerDTO);

        postId = postService.register(PostDTO.builder()
                .title(faker.book().title())
                .content(faker.lorem().sentence())
                .writerId(writerId)
                .build());

        for (int i = 0; i < 5; i++) {
            MemberDTO memberDTO = MemberDTO.builder()
                    .email(faker.internet().emailAddress())
                    .password(faker.internet().password())
                    .nickname(faker.name().name())
                    .role(MemberRole.USER)
                    .build();
            userId = memberService.register(memberDTO);

            heartService.register(HeartDTO.builder()
                    .postId(postId)
                    .userId(userId)
                    .build());
        }

    }

    @Test
    public void testGet() {
        HeartDTO heartDTO = heartService.get(postId, userId);
        Assertions.assertNotNull(heartDTO);
        log.info("HeartDTO: " + heartDTO);
    }

    @Test
    public void testGetListByPostId() {
        List<HeartDTO> result = heartService.getHeartsByPost(postId);
        log.info("List: " + result);
    }

    @Test
    public void testRemove() {
        heartService.remove(postId, userId);
        Assertions.assertThrows(NoSuchElementException.class, () -> heartService.get(postId, userId));
    }

    @Test
    public void testDeleteByPost() {
        postService.remove(postId);

        List<HeartDTO> hearts = heartService.getHeartsByPost(postId);
        Assertions.assertTrue(hearts.isEmpty(), "Hearts should be empty");
    }

}
