package com.uninter.tcc.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.uninter.tcc.model.BehaviorScoreEntity;

public interface BehaviorScoreRepository extends MongoRepository<BehaviorScoreEntity, String> {
    
}
