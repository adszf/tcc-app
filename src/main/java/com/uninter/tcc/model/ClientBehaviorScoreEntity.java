package com.uninter.tcc.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.Data;

@Data
@Document(collection = "Client")
public class ClientBehaviorScoreEntity {
    @Id
    private String id;
    private String cpf;
    private Integer idade;
    private String estadoCivil;
    private String genero;
    private String relacionamento;
    private String usoCredito;
    private String profissao;
    private Integer salarioMensal;
    private Integer porcentagemDoSalarioUsada;
    private Integer grauEscolaridade;
    private String regiao;
    private Integer pontualidadePagamento;
    private Integer mediaGastosMensais;
    private Integer valorTotalEmprestimoComJuros;
    private Integer totalTaxa;
    private Integer valorParcelas;
    private Integer quantidadeParcelasEmprestimoComJuros;
    private Integer taxaJurosAnual;
    private String negociacao;
    private Integer historicoPagamento;
    private String eventos;
    private String quantidadeDividas;
    private String tipoEmprestimo;
    private String tipo;
}
