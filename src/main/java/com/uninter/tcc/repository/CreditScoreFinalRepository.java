package com.uninter.tcc.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.uninter.tcc.model.CreditScoreFinalEntity;

@Repository
public interface CreditScoreFinalRepository extends MongoRepository<CreditScoreFinalEntity, String> {
}
