package com.fintech.digiwallet.dto.mapper;

import com.fintech.digiwallet.domain.entity.Transaction;
import com.fintech.digiwallet.dto.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    @Mapping(source = "sourceWallet.walletNumber", target = "sourceWalletNumber")
    @Mapping(source = "destinationWallet.walletNumber", target = "destinationWalletNumber")
    TransactionResponse toResponse(Transaction transaction);

    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}
