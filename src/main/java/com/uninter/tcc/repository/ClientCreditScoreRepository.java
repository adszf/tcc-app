package com.uninter.tcc.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.uninter.tcc.model.ClientBehaviorScoreEntity;
import com.uninter.tcc.model.ClientCreditScoreEntity;



public interface ClientCreditScoreRepository extends MongoRepository<ClientBehaviorScoreEntity, String> {

    ClientCreditScoreEntity findByCpfAndTipo(String cpf, String tipo);
    
}
