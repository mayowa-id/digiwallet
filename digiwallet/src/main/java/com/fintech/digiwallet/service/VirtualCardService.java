package com.fintech.digiwallet.service;

import com.fintech.digiwallet.domain.entity.User;
import com.fintech.digiwallet.domain.entity.VirtualCard;
import com.fintech.digiwallet.domain.entity.Wallet;
import com.fintech.digiwallet.domain.enums.CardStatus;
import com.fintech.digiwallet.domain.repository.VirtualCardRepository;
import com.fintech.digiwallet.dto.mapper.VirtualCardMapper;
import com.fintech.digiwallet.dto.request.VirtualCardRequest;
import com.fintech.digiwallet.dto.response.VirtualCardResponse;
import com.fintech.digiwallet.exception.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VirtualCardService {

    private final VirtualCardRepository virtualCardRepository;
    private final WalletService walletService;
    private final VirtualCardMapper virtualCardMapper;

    @Transactional
    public VirtualCardResponse createCard(VirtualCardRequest request) {
        log.info("Creating virtual card for wallet: {}", request.getWalletNumber());

        Wallet wallet = walletService.findWalletByNumber(request.getWalletNumber());

        // Generate card details (in production, use actual card issuer API)
        String cardNumber = generateCardNumber();
        String cvv = generateCVV();
        LocalDate expiryDate = LocalDate.now().plusYears(3);

        VirtualCard card = VirtualCard.builder()
                .user(wallet.getUser())
                .wallet(wallet)
                .cardNumberEncrypted(encryptCardNumber(cardNumber))
                .cardHolderName(request.getCardHolderName())
                .cvvEncrypted(encryptCVV(cvv))
                .expiryDate(expiryDate)
                .cardStatus(CardStatus.ACTIVE)
                .currency(request.getCurrency())
                .spendingLimit(request.getSpendingLimit())
                .spentAmount(BigDecimal.ZERO)
                .cardBrand("VISA")
                .build();

        card = virtualCardRepository.save(card);
        log.info("Virtual card created successfully: {}", card.getId());

        return virtualCardMapper.toResponse(card);
    }

    @Transactional(readOnly = true)
    public List<VirtualCardResponse> getUserCards(UUID userId) {
        List<VirtualCard> cards = virtualCardRepository.findByUserId(userId);
        return virtualCardMapper.toResponseList(cards);
    }

    @Transactional(readOnly = true)
    public VirtualCardResponse getCard(UUID cardId) {
        VirtualCard card = findCardById(cardId);
        return virtualCardMapper.toResponse(card);
    }

    @Transactional
    public VirtualCardResponse blockCard(UUID cardId) {
        VirtualCard card = findCardById(cardId);
        card.setCardStatus(CardStatus.BLOCKED);
        card = virtualCardRepository.save(card);

        log.info("Card blocked: {}", cardId);
        return virtualCardMapper.toResponse(card);
    }

    @Transactional
    public VirtualCardResponse unblockCard(UUID cardId) {
        VirtualCard card = findCardById(cardId);

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot unblock expired card");
        }

        card.setCardStatus(CardStatus.ACTIVE);
        card = virtualCardRepository.save(card);

        log.info("Card unblocked: {}", cardId);
        return virtualCardMapper.toResponse(card);
    }

    @Transactional
    public void deleteCard(UUID cardId) {
        VirtualCard card = findCardById(cardId);
        virtualCardRepository.delete(card);
        log.info("Card deleted: {}", cardId);
    }

    // Helper methods
    private VirtualCard findCardById(UUID cardId) {
        return virtualCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));
    }

    private String generateCardNumber() {
        // In production, use actual card issuer API
        return "4111" + String.format("%012d", (long)(Math.random() * 1000000000000L));
    }

    private String generateCVV() {
        return String.format("%03d", (int)(Math.random() * 1000));
    }

    private String encryptCardNumber(String cardNumber) {
        // In production, use proper encryption (AES-256)
        return "ENCRYPTED_" + cardNumber;
    }

    private String encryptCVV(String cvv) {
        // In production, use proper encryption (AES-256)
        return "ENCRYPTED_" + cvv;
    }
}