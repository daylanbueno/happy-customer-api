package com.github.daylanbueno.happycustomer.converters;

import com.github.daylanbueno.happycustomer.domain.dto.TransactionDto;
import com.github.daylanbueno.happycustomer.domain.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionConverter {

    private final ItemConverter itemConverter;

    public TransactionDto conveterToDTo(Transaction newTransaction) {
        return TransactionDto.builder()
                .id(newTransaction.getId())
                .idCustomer(newTransaction.getCustomer().getId())
                .customerName(newTransaction.getCustomer().getName())
                .dateTransaction(newTransaction.getDate())
                .total(newTransaction.getTotal())
                .totalPoint(newTransaction.getTotalPoint())
                .items(itemConverter.converterDtoItensToDto(newTransaction.getItems()))
                .build();
    }
}
