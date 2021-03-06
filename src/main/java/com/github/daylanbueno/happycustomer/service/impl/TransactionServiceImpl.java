package com.github.daylanbueno.happycustomer.service.impl;

import com.github.daylanbueno.happycustomer.converters.CustomerConverter;
import com.github.daylanbueno.happycustomer.converters.ItemConverter;
import com.github.daylanbueno.happycustomer.converters.TransactionConverter;
import com.github.daylanbueno.happycustomer.domain.dto.CustomerDto;
import com.github.daylanbueno.happycustomer.domain.dto.GroupDto;
import com.github.daylanbueno.happycustomer.domain.dto.TransactionDto;
import com.github.daylanbueno.happycustomer.domain.dto.TransactionGroupDto;
import com.github.daylanbueno.happycustomer.domain.entity.Customer;
import com.github.daylanbueno.happycustomer.domain.entity.Item;
import com.github.daylanbueno.happycustomer.domain.entity.Transaction;
import com.github.daylanbueno.happycustomer.repository.TransactionRepository;
import com.github.daylanbueno.happycustomer.service.CustomerService;
import com.github.daylanbueno.happycustomer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private final ItemConverter itemConverter;
    private final CustomerConverter customerConverter;
    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;
    private final TransactionConverter transactionConverter;

    private static BigDecimal calculateTotal(Item item) {
        return item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    @Override
    public TransactionDto registerTransaction(TransactionDto transactionDto) {

        CustomerDto customerDto = customerService.findById(transactionDto.getIdCustomer());

        Transaction transaction = Transaction.builder()
                .date(LocalDate.now())
                .customer(customerConverter.converterToEntity(customerDto))
                .items(itemConverter.converterDtoItensToEntity(transactionDto.getItems()))
                .build();

        calculateTotalTransaction(transaction);

        calculatePointsTransaction(transaction);

        Transaction newTransaction = transactionRepository.save(transaction);

        return transactionConverter.conveterToDTo(newTransaction);
    }

    @Override
    public List<TransactionDto> findTranscationsByFilterDate(LocalDate startDate, LocalDate endDate) {

        List<Transaction> transactions = transactionRepository.findByDateBetween(startDate, endDate);

        return transactions.stream()
                .map(entity -> transactionConverter.conveterToDTo(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionGroupDto> findTransactionGroupTheLastThreeMonth() {
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = LocalDate.now();
        List<TransactionGroupDto> transactionGroupDtos = new ArrayList<>();
        List<Transaction> transactionByFilter = transactionRepository.findByDateBetween(startDate, endDate);

        List<TransactionDto> transactionDtos = transactionByFilter.stream().
                map(entity -> transactionConverter.conveterToDTo(entity))
                .collect(Collectors.toList());

        Collection<Long> idsCustomers = new HashSet<>();

        includIdCustomers(transactionDtos, idsCustomers);

        List<LocalDate> dates = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth().getValue() - 2, 01).datesUntil(LocalDate.now())
                .filter(date -> date.getDayOfMonth() == 1)
                .collect(Collectors.toList());


        for (Long idCustomer: idsCustomers) {
            for (LocalDate currentDate: dates) {
                filterTransactionByCustomerAndDateMonth(idCustomer,currentDate,transactionDtos, transactionGroupDtos);
            }
        }

        return transactionGroupDtos;
    }

    @Override
    public List<TransactionDto> findTranscationsByFilterDateAndCustomer(LocalDate startDate, LocalDate endDate, Long id) {

        CustomerDto customerDto = customerService.findById(id);

        if (customerDto == null) {
            return Arrays.asList();
        }

        Customer customer = customerConverter.converterToEntity(customerDto);


        List<Transaction> transactions = transactionRepository.findByDateBetweenAndCustomer(startDate, endDate, customer);

        return transactions.stream()
                .map(entity -> transactionConverter.conveterToDTo(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> findAll() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        return allTransactions.stream()
                .map(entity -> transactionConverter.conveterToDTo(entity))
                .collect(Collectors.toList());
    }

    private void filterTransactionByCustomerAndDateMonth(Long idCustomer,
                                                         LocalDate dateMonth,
                                                         List<TransactionDto> transactionDtos,
                                                         List<TransactionGroupDto> transactionGroupDtos ) {

        List<TransactionDto> result = transactionDtos.stream().filter(transaction -> transaction.getIdCustomer() == idCustomer
                   && transaction.getDateTransaction().getMonth().name().equals(dateMonth.getMonth().name())
        ).collect(Collectors.toList());


        CustomerDto currentCustomer = customerService.findById(idCustomer);

        TransactionGroupDto group = TransactionGroupDto.builder()
                .nameCustomer(currentCustomer.getName())
                .moth(dateMonth.getMonth().name())
                .details(new ArrayList<>())
                .build();


        List<GroupDto> groupDetails = result.stream().map(transactionDto -> GroupDto.builder()
                .dateTransaction(transactionDto.getDateTransaction())
                .total(transactionDto.getTotal())
                .totalPoint(transactionDto.getTotalPoint()).build()
        ).collect(Collectors.toList());

        group.getDetails().addAll(groupDetails);

        Integer totalPointByMonth = group.getDetails().stream().map(transaction -> transaction.getTotalPoint())
                .reduce(0, (currentValue, nextValue) -> currentValue + nextValue);

        group.setTotalPointMonth(totalPointByMonth);

        transactionGroupDtos.add(group);
    }

    private void includIdCustomers(List<TransactionDto> transactionByFilter, Collection<Long> idsCustomers) {
        List<Long> idCustomerRepeted = transactionByFilter.stream()
                .map(transaction -> transaction.getIdCustomer())
                .collect(Collectors.toList());
        idsCustomers.addAll(idCustomerRepeted);
    }

    private void calculatePointsTransaction(Transaction transaction) {
        int totalTransaction = transaction.getTotal().intValue();
        Integer totalPoint;

        if (totalTransaction <= 50) return;

        if(totalTransaction > 50 && totalTransaction < 100) {
            totalPoint = (totalTransaction - 50);
            transaction.setTotalPoint(totalPoint);
            return;
        }

        totalPoint = (totalTransaction - 50) * 1 + (totalTransaction - 100) * 1;
        transaction.setTotalPoint(totalPoint);
    }

    private void calculateTotalTransaction(Transaction transaction) {
        BigDecimal total = transaction
                .getItems()
                .stream()
                .map(TransactionServiceImpl::calculateTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        transaction.setTotal(total);
    }

}
