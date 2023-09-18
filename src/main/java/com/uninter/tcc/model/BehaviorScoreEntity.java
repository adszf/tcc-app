package com.uninter.tcc.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.Data;

@Data
@Document(collection = "Behavior_Score")
public class BehaviorScoreEntity {
    @Id
    public String id;
    public Integer idade;
    public String estadoCivil;
    public String profissao;
    public Integer salarioMensal;
    public Integer porcentagemDoSalarioUsada;
    public Integer grauEscolaridade;
    public String regiao;
    public Integer pontualidadePagamento;
    public Integer usoDeCredito;
    public Integer mediaGastosMensais;
    public Integer valorTotalEmprestimoSemJuros;
    public Integer valorTotalEmprestimoComJuros;
    public Integer totalTaxa;
    public Integer valorParcelas;
    public Integer quantidadeParcelasEmprestimoComJuros;
    public Integer quantidadeParcelasEmprestimoSemJuros;
    public Integer taxaJurosAnual;
    public String negociacao;
    public Integer historicoAtual;
    public String tipoEmprestimo;
}
