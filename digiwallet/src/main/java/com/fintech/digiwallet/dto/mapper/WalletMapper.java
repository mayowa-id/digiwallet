package com.fintech.digiwallet.dto.mapper;


import com.fintech.digiwallet.domain.entity.Wallet;
import com.fintech.digiwallet.dto.response.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletMapper {
    WalletResponse toResponse(Wallet wallet);
    List<WalletResponse> toResponseList(List<Wallet> wallets);
}
