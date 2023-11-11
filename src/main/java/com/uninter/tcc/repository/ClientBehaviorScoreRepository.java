package com.uninter.tcc.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.uninter.tcc.model.ClientBehaviorScoreEntity;

public interface ClientBehaviorScoreRepository extends MongoRepository<ClientBehaviorScoreEntity, String> {

    ClientBehaviorScoreEntity findByCpfAndTipo(String cpf, String tipo);

}
