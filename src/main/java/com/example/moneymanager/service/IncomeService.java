package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDTO;
import com.example.moneymanager.dto.IncomeDTO;
import com.example.moneymanager.entity.CategoryEntity;

import com.example.moneymanager.entity.ExpenseEntity;
import com.example.moneymanager.entity.IncomeEntity;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.CategoryRepository;
import com.example.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private  final  ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO dto){
        ProfileEntity profile= profileService.getCurrentProfile();
        CategoryEntity category=categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(()->new RuntimeException("Category Not Found"));
        IncomeEntity newIncome= toEntity(dto,profile,category);
        newIncome= incomeRepository.save(newIncome);
        return  toDTO(newIncome);

    }

    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser(){
        ProfileEntity profile=profileService.getCurrentProfile();
        LocalDate now=LocalDate.now();

        LocalDate startDate=now.withDayOfMonth(now.lengthOfMonth());
        LocalDate endDate=now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> list=  incomeRepository.findByProfileIdAndDateBetween(profile.getId(),startDate,endDate);
        return  list.stream().map(this::toDTO).toList();
    }

    //delet expense by id for current user

    public void deleteIncomeById(Long incomeId){
        ProfileEntity profile= profileService.getCurrentProfile();
        IncomeEntity entity=incomeRepository.findById(incomeId)
                .orElseThrow(()->new RuntimeException("income not found"));
        if(!entity.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("unauthorized to delete this income");

        }
        incomeRepository.delete(entity);

    }

    //get latest 5 expenses for current user
    public List<IncomeDTO> getLatest5IncomesForCurrentUser(){
        ProfileEntity profile=profileService.getCurrentProfile();
        List<IncomeEntity> list=incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return  list.stream().map(this::toDTO).toList();
    }

    //get total expenses for current user

    public BigDecimal getTotalIncomeForCurrentUser(){
        ProfileEntity profile=profileService.getCurrentProfile();
        BigDecimal total= incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return  total !=null?total:BigDecimal.ZERO;

    }

    //filter incomes
    public List<IncomeDTO>filterIncomes(LocalDate startDate, LocalDate endDate, String keyBoard, Sort sort){
        ProfileEntity profile= profileService.getCurrentProfile();
        List<IncomeEntity> list =incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate,endDate,keyBoard,sort);
        return list.stream().map(this::toDTO).toList();
    }

    private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category ){

        return IncomeEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();

    }

    private IncomeDTO toDTO(IncomeEntity entity){
        return IncomeDTO.builder()
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


