package com.SafeCryptoStocks.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SafeCryptoStocks.model.Budget;
import com.SafeCryptoStocks.model.DummyStocksList;
import com.SafeCryptoStocks.model.Expense;
import com.SafeCryptoStocks.model.Portfolio;
import com.SafeCryptoStocks.model.Stock;
import com.SafeCryptoStocks.services.BudgetService;
import com.SafeCryptoStocks.services.ExpenseService;
import com.SafeCryptoStocks.services.PortfolioService;
import com.SafeCryptoStocks.services.StockService;

@RestController
	@RequestMapping("/stock")
	public class StockController {

	

	private Portfolio portfolio;
	
	
	@Autowired
	    private StockService stockService;

	    @Autowired
	    private PortfolioService portfolioService;

	    @Autowired
	    private BudgetService budgetService;
	    
	    @Autowired
	    private ExpenseService expenseService;
	    
	   
	    @PostMapping("/{portfolioId}")
	    public ResponseEntity<String> addStockToPortfolio(
	            @PathVariable Long portfolioId,
	            @RequestBody Stock stock,
	            @RequestParam double stockPrice,
	            @RequestParam Long budgetId) {

	        // Add stock to portfolio
	        stockService.addStockToPortfolio(portfolioId, stock, stockPrice);

	        // Add stock price as an expense to the budget
	        try {
	            Budget budget = budgetService.getBudgetById(budgetId);

	            double totalExpenses = budget.getExpenses() + stockPrice;
	            if (totalExpenses > budget.getAmount()) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                        .body("Out of Budget: Total expenses exceed the budget limit of ₹" + budget.getAmount());
	            }

	            Expense expense = new Expense();
	            expense.setAmount(stockPrice);
	            expense.setDescription(stock.getStockName());
	            expense.setBudget(budget);
	            expense.setTimestamp(LocalDateTime.now());
	            expenseService.createExpense(expense);
	            
	            
	            

	            budget.setExpenses(totalExpenses);
	            budgetService.updateBudget(budget);

	            return ResponseEntity.ok("Stock added and budget updated successfully.");
	        } catch (RuntimeException ex) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	        }
	    }

	    
	    
	  
	   

	    
	    
//	    
//	    @PostMapping("/{portfolioId}")
//	    public ResponseEntity<Stock> addStock(@PathVariable Long portfolioId, @RequestBody Stock stock) {
//	        Stock createdStock = stockService.addStockToPortfolio(portfolioId, stock);
//	        return ResponseEntity.status(HttpStatus.CREATED).body(createdStock);
//	    }
	    

	    @GetMapping("/{portfolioId}")
	    public ResponseEntity<List<Stock>> getStocksByPortfolio(@PathVariable Long portfolioId) {
	        List<Stock> stocks = stockService.getStocksByPortfolio(portfolioId);
	        return ResponseEntity.ok(stocks);
	    }
	    
	    // Bulk insert stocks into a portfolio
//	    @PostMapping("/bulk-insert/{portfolioId}")
//	    public ResponseEntity<List<Stock>> addMultipleStocks(@PathVariable Long portfolioId, @RequestBody List<Stock> stocks) {
//	        List<Stock> createdStocks = stockService.addMultipleStocksToPortfolio(portfolioId, stocks);
//	      
//	    
//	        
//	        return ResponseEntity.status(HttpStatus.CREATED).body(createdStocks);
//	    }

    
	    @PostMapping("/bulk-insert/{portfolioId}")
	    public ResponseEntity<String> addMultipleStocksAndExpenses(
	            @PathVariable Long portfolioId,
	            @RequestBody List<Stock> stocks) {
	        try {
	            // Fetch portfolio by ID and validate its existence
	            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
	            if (portfolio == null) {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Portfolio not found.");
	            }

	            // Fetch and validate the associated budgets (one-to-many relation)
	            List<Budget> budgets = portfolio.getBudgets();
	            if (budgets.isEmpty()) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No budgets associated with the given portfolio.");
	            }

	            // Assuming you are working with the first budget (or you can handle specific logic to choose the budget)
	            Budget budget = budgets.get(0);  // You can adjust this logic if you want to use a specific budget
	            
	            // Calculate the total cost of stocks (stock purchase cost)
	            double totalStockExpense = stocks.stream()
	                    .mapToDouble(stock -> stock.getAvgBuyPrice() * stock.getHoldings()) // Price * Holdings
	                    .sum();

	            // Validate if total expenses exceed the budget limit
	            double newTotalExpenses = budget.getExpenses() + totalStockExpense;
	            if (newTotalExpenses > budget.getAmount()) {
	                String errorMessage = String.format(
	                        "Out of Budget: Total expenses (₹%.2f) exceed the budget limit of ₹%.2f.",
	                        newTotalExpenses, budget.getAmount()
	                );
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
	            }

	            // Add stocks to portfolio
	            List<Stock> createdStocks = stockService.addMultipleStocksToPortfolio(portfolioId, stocks);

	            // Create expenses for each stock and associate them with the budget
	            List<Expense> expenses = createdStocks.stream()
	                    .map(stock -> {
	                        Expense expense = new Expense();
	                        expense.setDescription("Stock Purchase: " + stock.getStockName());
	                        expense.setAmount(stock.getAvgBuyPrice() * stock.getHoldings()); // Stock cost
	                        expense.setBudget(budget); // Link the expense to the budget
	                        return expense;
	                    })
	                    .toList();

	            // Bulk save the expenses
	            expenseService.addBulkExpenses(expenses);

	            // Update the budget with new total expenses
	            budget.setExpenses(newTotalExpenses);
	            budgetService.updateBudget(budget);

	            return ResponseEntity.status(HttpStatus.CREATED)
	                    .body("Stocks and expenses added successfully.");
	        } catch (RuntimeException ex) {
	            // Handle unexpected errors
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("An error occurred: " + ex.getMessage());
	        }
	    }

    
	    
	    // Add stocks in portfolio
	    @PostMapping
	    public Stock addStock(@RequestBody Stock stock) {
	        return stockService.buyStock(stock);
	    }
	    
	    
	    
	    
	}

	