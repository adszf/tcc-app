package com.uninter.tcc.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.Data;
@Data
@Document(collection = "Custom_Credit_Score")
public class CustomCreditScoreEntity{
    @Id
    private String id;
    private long fakeIdCpf;
    private String nome;
    private int idade;
    private String estadoCivil;
    private String cidade;
    private String regiao;
    private int numContasBancarias;
    private int numCartaoCredito;
    private int numEmprestimo;
    private String profissao;
    private int salarioMensal;
    private String tipoEmprestimo;
    private String pagamentoValorMinimo;
    private int atrasoPartirDataVencimento;
    private int historicoCreditoIdade;
}