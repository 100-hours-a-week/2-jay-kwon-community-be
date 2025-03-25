package com.jay.sapapi.service;

import com.github.javafaker.Faker;
import com.jay.sapapi.domain.MemberRole;
import com.jay.sapapi.dto.MemberDTO;
import com.jay.sapapi.util.exception.CustomValidationException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;

@SpringBootTest
@Log4j2
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("MemberServiceTests")
public class MemberServiceTests {

    @Autowired
    private MemberService memberService;

    private final Faker faker = new Faker();

    private MemberDTO memberDTO;

    private Long userId;

    @BeforeAll
    public void setup() {
        Assertions.assertNotNull(memberService, "MemberService should not be null");
        log.info(memberService.getClass().getName());
    }

    @BeforeEach
    public void registerMember() {
        memberDTO = MemberDTO.builder()
                .email("sample@example.com")
                .password(faker.internet().password())
                .nickname("SampleUser")
                .role(MemberRole.USER)
                .build();

        try {
            userId = memberService.register(memberDTO);
        } catch (CustomValidationException e) {
            if ("emailAlreadyExists".equals(e.getMessage())) {
                memberDTO.setEmail(faker.internet().emailAddress());
                memberDTO.setNickname(faker.name().name());
                userId = memberService.register(memberDTO);
            }
        }
    }

    @AfterEach
    public void cleanup() {
        try {
            memberService.remove(userId);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
        }
    }

    @Nested
    @DisplayName("회원 조회 테스트")
    class ReadTests {

        @Test
        @DisplayName("단일 회원 조회")
        public void testGet() {
            MemberDTO dto = memberService.get(userId);
            Assertions.assertEquals(memberDTO.getEmail(), dto.getEmail());
            Assertions.assertEquals(memberDTO.getNickname(), dto.getNickname());
        }

        @Test
        @DisplayName("회원 조회 실패")
        public void testGetFail() {
            NoSuchElementException e = Assertions.assertThrows(NoSuchElementException.class, () -> memberService.get(0L));
            Assertions.assertEquals("userNotFound", e.getMessage());
        }

    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTests {

        @Test
        @DisplayName("이메일 중복")
        public void testRegisterDuplicateEmail() {
            CustomValidationException e = Assertions.assertThrows(CustomValidationException.class, () -> memberService.register(memberDTO));
            Assertions.assertEquals("emailAlreadyExists", e.getMessage());
        }

        @Test
        @DisplayName("닉네임 중복")
        public void testRegisterDuplicateNickname() {
            memberDTO.setEmail(faker.internet().emailAddress());
            CustomValidationException e = Assertions.assertThrows(CustomValidationException.class, () -> memberService.register(memberDTO));
            Assertions.assertEquals("nicknameAlreadyExists", e.getMessage());
        }

    }

    @Nested
    @DisplayName("회원 존재 여부 테스트")
    class ExistTests {

        @Test
        @DisplayName("이메일로 회원 존재 여부 확인")
        public void testExistsByEmail() {
            Assertions.assertTrue(memberService.existsByEmail(memberDTO.getEmail()));
        }

        @Test
        @DisplayName("닉네임으로 회원 존재 여부 확인")
        public void testExistsByNickname() {
            Assertions.assertTrue(memberService.existsByNickname(memberDTO.getNickname()));
        }

    }

    @Nested
    @DisplayName("비밀번호 확인 테스트")
    class PasswordTests {

        @Test
        @DisplayName("존재하지 않는 회원의 비밀번호 확인")
        public void testCheckPasswordInvalidUser() {
            NoSuchElementException e = Assertions.assertThrows(NoSuchElementException.class, () ->
                    memberService.checkPassword(0L, faker.internet().password()));
            Assertions.assertEquals("userNotFound", e.getMessage());
        }

        @Test
        @DisplayName("잘못된 비밀번호 확인")
        public void testCheckPasswordInvalidPassword() {
            CustomValidationException e = Assertions.assertThrows(CustomValidationException.class, () ->
                    memberService.checkPassword(userId, faker.internet().password()));
            Assertions.assertEquals("invalidPassword", e.getMessage());
        }

    }

    @Nested
    @DisplayName("회원 정보 수정 테스트")
    class ModifyTests {

        @Test
        @DisplayName("회원 정보 수정")
        public void testModify() {
            String email = "modified@example.com";
            String nickname = "ModifiedUser";
            MemberDTO updatedMember = MemberDTO.builder()
                    .id(userId)
                    .email(email)
                    .password(faker.internet().password())
                    .nickname(nickname)
                    .role(MemberRole.MANAGER)
                    .build();
            memberService.modify(updatedMember);

            MemberDTO result = memberService.get(userId);
            Assertions.assertNotEquals(memberDTO.getEmail(), result.getEmail());
            Assertions.assertEquals(email, result.getEmail());
            Assertions.assertNotEquals(memberDTO.getNickname(), result.getNickname());
            Assertions.assertEquals(nickname, result.getNickname());
        }

        @Test
        @DisplayName("이메일 중복 실패")
        public void testModifyDuplicateEmail() {
            Long testUserId = memberService.register(MemberDTO.builder()
                    .email(faker.internet().emailAddress())
                    .password(faker.internet().password())
                    .nickname(faker.name().name())
                    .role(MemberRole.USER)
                    .build());

            MemberDTO updatedMember = MemberDTO.builder()
                    .id(testUserId)
                    .email(memberDTO.getEmail())
                    .build();
            CustomValidationException e = Assertions.assertThrows(CustomValidationException.class, () -> memberService.modify(updatedMember));
            Assertions.assertEquals("emailAlreadyExists", e.getMessage());
        }

        @Test
        @DisplayName("닉네임 중복 실패")
        public void testModifyDuplicateNickname() {
            Long testUserId = memberService.register(MemberDTO.builder()
                    .email(faker.internet().emailAddress())
                    .password(faker.internet().password())
                    .nickname(faker.name().name())
                    .role(MemberRole.USER)
                    .build());

            MemberDTO updatedMember = MemberDTO.builder()
                    .id(testUserId)
                    .nickname(memberDTO.getNickname())
                    .build();
            CustomValidationException e = Assertions.assertThrows(CustomValidationException.class, () -> memberService.modify(updatedMember));
            Assertions.assertEquals("nicknameAlreadyExists", e.getMessage());
        }

    }

    @Nested
    @DisplayName("회원 삭제 테스트")
    class RemoveTests {

        @Test
        @DisplayName("회원 삭제")
        public void testRemove() {
            memberService.remove(userId);
            Assertions.assertThrows(NoSuchElementException.class, () -> memberService.get(userId));
        }

        @Test
        @DisplayName("존재하지 않는 회원 삭제")
        public void testRemoveInvalidUser() {
            NoSuchElementException e = Assertions.assertThrows(NoSuchElementException.class, () -> memberService.remove(0L));
            Assertions.assertEquals("userNotFound", e.getMessage());
        }

    }

}
