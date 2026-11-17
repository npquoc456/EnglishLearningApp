package com.example.demo.security;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
@Component("owner")
@RequiredArgsConstructor
public class OwnershipGuard {
    private final DeckRepository decks;
    private final CardRepository cards;
    private final StudySessionRepository sessions;

    private Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (!(a instanceof JwtAuthenticationToken token) || !token.isAuthenticated()) return null;
        try { return Long.valueOf(token.getName()); } catch(NumberFormatException e) { return null; }
    }
    public boolean isSelf(Long userId) {
        Long me = currentUserId();
        return me != null && me.equals(userId);
    }
    public boolean ownsDeck(Long id) {
        Long me = currentUserId();
        return me != null && id != null && decks.existsByIdAndUserId(id, me);
    }
    public boolean ownsCard(Long id) {
        Long me = currentUserId();
        return me != null && id != null && cards.existsByIdAndDeck_User_Id(id, me);
    }
    public boolean ownsSession(Long id) {
        Long me = currentUserId();
        return me != null && id != null && sessions.existsByIdAndUserId(id, me);
    }
    public boolean selfAndDeck(Long uid, Long deckId) {
        return isSelf(uid) && (deckId == null || ownsDeck(deckId));
    }
    public boolean sessionAndCard(Long sessionId, Long cardId) {
        return ownsSession(sessionId) && ownsCard(cardId);
    }
}
