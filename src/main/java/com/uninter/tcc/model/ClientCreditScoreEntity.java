package com.uninter.tcc.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.Data;

@Data
@Document(collection = "Client")
public class ClientCreditScoreEntity {
     @Id
    private String id;
    private String cpf;
    private Integer idade;
    private String estadoCivil;
    private Integer numContasBancarias;
    private Integer numCartaoCredito;
    private Integer numEmprestimoContratado;
    private String profissao;
    private Integer salarioMensal;
    private String pagamentoValorMinimo;
    private Integer atrasoPartirDataVencimentoDias;
    private Integer historicoCreditoAnos;
    private String tipoEmprestimoSolicitacao;
    private String pendencia;
    private Integer quantidadeParcelasSolicitada;
    private Integer grauEscolaridade;
    private Integer moradia;
    private Integer numDependentes;
    private String creditScore;
    private String tipo;
}
