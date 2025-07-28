package com.example.moneymanager.controller;


import com.example.moneymanager.dto.ExpenseDTO;
import com.example.moneymanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
 @RequiredArgsConstructor
 @RequestMapping("/expenses")
 public class ExpenseController {
     private final ExpenseService expenseService;

     @PostMapping
     public ResponseEntity<ExpenseDTO> addExpense(@RequestBody ExpenseDTO dto){

         ExpenseDTO saved=expenseService.addExpense(dto);
         return ResponseEntity.status(HttpStatus.CREATED).body(saved);


    }


    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses(){
         List<ExpenseDTO> expenses=expenseService.getCurrentMonthExpensesForCurrentUser();
         return ResponseEntity.ok(expenses);

    }
@DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id){
        expenseService.deleteExpenseById(id);
        return  ResponseEntity.noContent().build();
    }
}
