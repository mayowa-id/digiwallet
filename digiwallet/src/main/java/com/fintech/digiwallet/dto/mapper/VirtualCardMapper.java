package com.fintech.digiwallet.dto.mapper;


import com.fintech.digiwallet.domain.entity.VirtualCard;
import com.fintech.digiwallet.dto.response.VirtualCardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VirtualCardMapper {

    @Mapping(target = "maskedCardNumber", source = "cardNumberEncrypted", qualifiedByName = "maskCardNumber")
    @Mapping(target = "availableLimit", expression = "java(calculateAvailableLimit(card))")
    VirtualCardResponse toResponse(VirtualCard card);

    List<VirtualCardResponse> toResponseList(List<VirtualCard> cards);

    @Named("maskCardNumber")
    default String maskCardNumber(String encryptedCardNumber) {
        // In real implementation, decrypt first then mask
        // For now, just return masked placeholder
        return "**** **** **** 1234";
    }

    default BigDecimal calculateAvailableLimit(VirtualCard card) {
        if (card.getSpendingLimit() == null) {
            return null;
        }
        return card.getSpendingLimit().subtract(card.getSpentAmount());
    }
}