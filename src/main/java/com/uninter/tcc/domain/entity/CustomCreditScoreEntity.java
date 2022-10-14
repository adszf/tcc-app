package com.uninter.tcc.domain.entity;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
@Data
@Document(collection = "Custom_Credit_Score")
public class CustomCreditScoreEntity{
    @Id
    public String id;
    public long fakeIdCpf;
    public String nome;
    public int idade;
    public String estadoCivil;
    public String cidade;
    public String regiao;
    public int numContasBancarias;
    public int numCartaoCredito;
    public int numEmprestimo;
    public String profissao;
    public int salarioMensal;
    public String tipoEmprestimo;
    public String pagamentoValorMinimo;
    public int atrasoPartirDataVencimento;
    public int historicoCreditoIdade;
}