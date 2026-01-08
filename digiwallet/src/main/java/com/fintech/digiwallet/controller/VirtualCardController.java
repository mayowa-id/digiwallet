package com.fintech.digiwallet.controller;

import com.fintech.digiwallet.domain.entity.User;
import com.fintech.digiwallet.domain.repository.UserRepository;
import com.fintech.digiwallet.dto.request.VirtualCardRequest;
import com.fintech.digiwallet.dto.response.ApiResponse;
import com.fintech.digiwallet.dto.response.VirtualCardResponse;
import com.fintech.digiwallet.exception.UserNotFoundException;
import com.fintech.digiwallet.service.VirtualCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
public class VirtualCardController {

    private final VirtualCardService virtualCardService;
    private final UserRepository userRepository;
    private VirtualCardService cardService;


    @PostMapping
    public ResponseEntity<ApiResponse<VirtualCardResponse>> createCard(
            @Valid @RequestBody VirtualCardRequest request) {
        log.info("Creating virtual card for wallet: {}", request.getWalletNumber());

        VirtualCardResponse card = virtualCardService.createCard(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(card, "Virtual card created successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<VirtualCardResponse>>> getUserCards(
            @PathVariable UUID userId) {
        log.info("Fetching cards for user: {}", userId);

        List<VirtualCardResponse> cards = virtualCardService.getUserCards(userId);

        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<ApiResponse<VirtualCardResponse>> getCard(
            @PathVariable UUID cardId) {
        log.info("Fetching card: {}", cardId);

        VirtualCardResponse card = virtualCardService.getCard(cardId);

        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @PutMapping("/{cardId}/block")
    public ResponseEntity<ApiResponse<VirtualCardResponse>> blockCard(
            @PathVariable UUID cardId) {
        log.info("Blocking card: {}", cardId);

        VirtualCardResponse card = virtualCardService.blockCard(cardId);

        return ResponseEntity.ok(
                ApiResponse.success(card, "Card blocked successfully"));
    }

    @PutMapping("/{cardId}/unblock")
    public ResponseEntity<ApiResponse<VirtualCardResponse>> unblockCard(
            @PathVariable UUID cardId) {
        log.info("Unblocking card: {}", cardId);

        VirtualCardResponse card = virtualCardService.unblockCard(cardId);

        return ResponseEntity.ok(
                ApiResponse.success(card, "Card unblocked successfully"));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(
            @PathVariable UUID cardId) {
        log.info("Deleting card: {}", cardId);

        virtualCardService.deleteCard(cardId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Card deleted successfully"));
    }

    @GetMapping("/cards/my-cards")
    public ResponseEntity<ApiResponse<List<VirtualCardResponse>>> getMyCards(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<VirtualCardResponse> cards = cardService.getUserCards(user.getId());
        return ResponseEntity.ok(ApiResponse.success(cards));
    }
}