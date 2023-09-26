package com.uninter.tcc.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.Data;

@Data
@Document(collection = "Behavior_Score")
public class BehaviorScoreEntity {
    @Id
    private String id;
    private Integer idade;
    private String estadoCivil;
    private String profissao;
    private Integer salarioMensal;
    private Integer porcentagemDoSalarioUsada;
    private Integer grauEscolaridade;
    private String regiao;
    private Integer pontualidadePagamento;
    private Integer usoDeCredito;
    private Integer mediaGastosMensais;
    private Integer valorTotalEmprestimoSemJuros;
    private Integer valorTotalEmprestimoComJuros;
    private Integer totalTaxa;
    private Integer valorParcelas;
    private Integer quantidadeParcelasEmprestimoComJuros;
    private Integer quantidadeParcelasEmprestimoSemJuros;
    private Integer taxaJurosAnual;
    private String negociacao;
    private Integer historicoAtual;
    private String tipoEmprestimo;
}
