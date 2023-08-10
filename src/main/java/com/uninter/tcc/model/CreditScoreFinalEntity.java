package com.uninter.tcc.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.Data;
@Data
@Document(collection = "Credit_Score_Final")
public class CreditScoreFinalEntity{
    @Id
    public String id;
    public Integer idade;
    public String estadoCivil;
    public Integer numContasBancarias;
    public Integer numCartaoCredito;
    public Integer numEmprestimoContratado;
    public String profissao;
    public Integer salarioMensal;
    public String pagamentoValorMinimo;
    public Integer atrasoPartirDataVencimentoDias;
    public Integer historicoCreditoAnos;
    public String tipoEmprestimoSolicitacao;
    public String pendencia;
    public Integer quantidadeParcelasSolicitada;
    public Integer grauEscolaridade;
    public Integer moradia;
    public Integer numDependentes;
    public String creditScore;
}