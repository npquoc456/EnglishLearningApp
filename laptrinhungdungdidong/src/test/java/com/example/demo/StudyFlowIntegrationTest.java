package com.example.demo;

import com.example.demo.constant.DeckVisibility;
import com.example.demo.constant.StudyModeType;
import com.example.demo.controller.UserAvatarController;
import com.example.demo.dto.request.*;
import com.example.demo.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The transaction rolls back all temporary data. No persistent QA accounts are left on RDS. */
@SpringBootTest(properties = "APP_JWT_SECRET=TEST_ONLY_NEVER_DEPLOY_0123456789_0123456789")
@Transactional
class StudyFlowIntegrationTest {
    @Autowired com.example.demo.security.OwnershipGuard owner;
    @Autowired UserService userService;
    @Autowired DeckService deckService;
    @Autowired CardService cardService;
    @Autowired CardProgressService progressService;
    @Autowired StudySessionService sessionService;
    @Autowired UserAvatarController avatarController;
    @Autowired com.example.demo.controller.UserController userController;
    @Autowired com.example.demo.controller.DeckController deckController;
    @Autowired com.example.demo.controller.CardController cardController;
    @Autowired com.example.demo.controller.CardProgressController progressController;
    @Autowired com.example.demo.controller.StudySessionController sessionsController;
    @Autowired com.example.demo.security.JwtIssuer issuer;
    @Autowired org.springframework.security.oauth2.jwt.JwtDecoder decoder;

    @Test
    void reviewHistoryAndAvatarWorkTogether() throws Exception {
        UserRegisterRequest register = new UserRegisterRequest();
        register.setFullName("QA Test");
        register.setEmail("qa-" + UUID.randomUUID() + "@example.invalid");
        register.setPassword("qa-temp-password-123");
        var user = userService.register(register);
        assertNotNull(user.getId());

        UserLoginRequest login = new UserLoginRequest();
        login.setEmail(register.getEmail());
        login.setPassword(register.getPassword());
        assertEquals(user.getId(), userService.login(login).getId());

        DeckCreateRequest deckRequest = new DeckCreateRequest();
        deckRequest.setUserId(user.getId());
        deckRequest.setTitle("QA Vocabulary");
        deckRequest.setVisibility(DeckVisibility.PRIVATE);
        var deck = deckService.createDeck(deckRequest);

        CardCreateRequest cardRequest = new CardCreateRequest();
        cardRequest.setDeckId(deck.getId());
        cardRequest.setTerm("hello");
        cardRequest.setDefinition("xin chào");
        var card = cardService.createCard(cardRequest);
        assertEquals(1L, progressService.countDue(user.getId()));
        assertEquals(1L, progressService.summary(user.getId()).newCards());

        var easy = progressService.review(
                new CardReviewRequest(user.getId(), card.getId(), CardReviewRequest.Rating.EASY));
        assertEquals("MASTERED", easy.status());
        assertTrue(easy.nextReviewAt().isAfter(LocalDateTime.now().plusDays(3)));
        assertEquals(0L, progressService.countDue(user.getId()));
        assertEquals(1, progressService.byDeck(user.getId(), deck.getId()).size());

        StudySessionCreateRequest session = new StudySessionCreateRequest();
        session.setUserId(user.getId());
        session.setDeckId(deck.getId());
        session.setStudyMode(StudyModeType.REVIEW);
        session.setTotalCards(1);
        session.setCorrectCards(1);
        session.setDurationSeconds(45);
        sessionService.createStudySession(session);
        var history = sessionService.getSessionsByUserId(user.getId());
        assertEquals(1, history.size());
        assertEquals("QA Vocabulary", history.get(0).getDeckTitle());

        var jwt = Jwt.withTokenValue("test-only-token")
                .header("alg", "HS256").subject(user.getId().toString())
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(600)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, java.util.List.of()));
        var accessToken = issuer.issue(user.getId());
        assertEquals(user.getId().toString(), decoder.decode(accessToken).getSubject());
        assertEquals(user.getId().toString(), SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated(), "JWT Authentication must be authenticated");
        assertTrue(owner.isSelf(user.getId()), "Owner guard must recognize valid JWT subject");
        assertEquals(user.getId(), userController.getUserById(user.getId()).getBody().getId());
        assertEquals(deck.getId(), deckController.getDeckById(deck.getId()).getBody().getId());
        assertEquals(1, cardController.getCardsByDeckId(deck.getId()).getBody().size());
        assertEquals(1, sessionsController.getSessionsByUserId(user.getId()).getBody().size());
        assertThrows(AccessDeniedException.class, () -> userController.getUserById(user.getId() + 1000));
        assertThrows(AccessDeniedException.class, () -> userController.getAllUsers());
        assertThrows(AccessDeniedException.class, () -> progressController.summary(user.getId() + 1000));
        assertThrows(AccessDeniedException.class, () -> sessionsController.getSessionsByUserId(user.getId() + 1000));
        assertThrows(AccessDeniedException.class, () -> progressController.review(
                new CardReviewRequest(user.getId() + 1000, card.getId(), CardReviewRequest.Rating.GOOD)));
        try {
        byte[] exampleImage = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xd9};
        var file = new MockMultipartFile("image", "avatar.jpg", "image/jpeg", exampleImage);
        assertEquals(204, avatarController.upload(user.getId(), file).getStatusCode().value());
        assertArrayEquals(exampleImage, avatarController.download(user.getId()).getBody());
        assertThrows(AccessDeniedException.class, () -> avatarController.download(user.getId() + 1000));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
