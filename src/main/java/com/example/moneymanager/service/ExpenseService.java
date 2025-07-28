package com.example.moneymanager.service;


import com.example.moneymanager.dto.ExpenseDTO;
import com.example.moneymanager.entity.CategoryEntity;
import com.example.moneymanager.entity.ExpenseEntity;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.CategoryRepository;
import com.example.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor

public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    public ExpenseDTO  addExpense(ExpenseDTO dto){
        ProfileEntity profile= profileService.getCurrentProfile();
        CategoryEntity category=categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(()->new RuntimeException("Category Not Found"));
       ExpenseEntity newExpense= toEntity(dto,profile,category);
      newExpense= expenseRepository.save(newExpense);
      return  toDTO(newExpense);

    }


    //retrives all the expenses for current month/based on the start date and end date

    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser(){
        ProfileEntity profile=profileService.getCurrentProfile();
        LocalDate now=LocalDate.now();

        LocalDate startDate=now.withDayOfMonth(now.lengthOfMonth());
        LocalDate endDate=now.withDayOfMonth(now.lengthOfMonth());
      List<ExpenseEntity> list=  expenseRepository.findByProfileIdAndDateBetween(profile.getId(),startDate,endDate);
      return  list.stream().map(this::toDTO).toList();
    }

    //delet expense by id for current user

    public void deleteExpenseById(Long expenseId){
        ProfileEntity profile= profileService.getCurrentProfile();
        ExpenseEntity entity=expenseRepository.findById(expenseId)
                .orElseThrow(()->new RuntimeException("Expense not found"));
        if(!entity.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("unauthorized to delete this expense");

        }
        expenseRepository.delete(entity);

    }

    //get latest 5 expenses for current user
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser(){
        ProfileEntity profile=profileService.getCurrentProfile();
        List<ExpenseEntity> list=expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return  list.stream().map(this::toDTO).toList();
    }

    //get total expenses for current user

    public BigDecimal getTotalExpenseForCurrentUser(){
         ProfileEntity profile=profileService.getCurrentProfile();
        BigDecimal total= expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return  total !=null?total:BigDecimal.ZERO;

    }

    //filter expenses
    public List<ExpenseDTO>filterExpenses(LocalDate startDate, LocalDate endDate, String keyBoard, Sort sort){
       ProfileEntity profile= profileService.getCurrentProfile();
       List<ExpenseEntity> list =expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate,endDate,keyBoard,sort);
       return list.stream().map(this::toDTO).toList();
    }

    //notifications
    public List<ExpenseDTO> getExpensesForUserDate(Long profileId,LocalDate date){
        List<ExpenseEntity> list=expenseRepository.findByProfileIdAndDate(profileId, date);
       return list.stream().map(this::toDTO).toList();
    }


    private ExpenseEntity toEntity(ExpenseDTO dto, ProfileEntity profile, CategoryEntity category ){

        return ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();

    }

    private ExpenseDTO toDTO(ExpenseEntity entity){
       return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .categoryId(entity.getCategory()!=null ? entity.getCategory().getId():null)
                .categoryName(entity.getCategory()!=null?entity.getCategory().getName():"N/A")
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
