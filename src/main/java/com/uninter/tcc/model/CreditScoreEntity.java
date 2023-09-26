package com.uninter.tcc.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.Data;

@Data
@Document(collection = "Credit_Score")
public class CreditScoreEntity {
   @Id
    private String id;
    private Long fakeIdCpf;
    private String customerId;
    private String month;
    private int age;
    private String ssn;
    private String occupation;
    private double annualIncome;
    private double monthlyInhandSalary;
    private int numBankAccounts;
    private int numCreditCard;
    private int interestRate;
    private int numOfLoan;
    private String typeOfLoan;
    private int delayFromDueDate;
    private double changedCreditLimit;
    private int numCreditInquiries;
    private String creditMix;
    private double outstandingDebt;
    private double creditUtilizationRatio;
    private String creditHistoryAge;
    private String paymentOfMinAmount;
    private double totalEmiPerMonth;
    private double amountInvestedMonthly;
    private String paymentbehaviour;
    private double monthlyBalance;
    private String creditScore;
}
