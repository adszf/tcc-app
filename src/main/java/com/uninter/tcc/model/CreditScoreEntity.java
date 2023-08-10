package com.uninter.tcc.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.Data;

@Data
@Document(collection = "Credit_Score")
public class CreditScoreEntity {
   @Id
    public String id;
    public Long fakeIdCpf;
    public String customerId;
    public String month;
    public int age;
    public String ssn;
    public String occupation;
    public double annualIncome;
    public double monthlyInhandSalary;
    public int numBankAccounts;
    public int numCreditCard;
    public int interestRate;
    public int numOfLoan;
    public String typeOfLoan;
    public int delayFromDueDate;
    public double changedCreditLimit;
    public int numCreditInquiries;
    public String creditMix;
    public double outstandingDebt;
    public double creditUtilizationRatio;
    public String creditHistoryAge;
    public String paymentOfMinAmount;
    public double totalEmiPerMonth;
    public double amountInvestedMonthly;
    public String paymentbehaviour;
    public double monthlyBalance;
    public String creditScore;
}
