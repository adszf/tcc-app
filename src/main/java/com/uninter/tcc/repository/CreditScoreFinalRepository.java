package com.uninter.tcc.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninter.tcc.model.CreditScoreFinalEntity;

@Repository
public interface CreditScoreFinalRepository extends MongoRepository<CreditScoreFinalEntity, String> {

    @Query("{'creditScore': {$in: ?0}}")
    Page<CreditScoreFinalEntity> findAllCreditScoreFinalEntity(@Param("creditScores") List<String> creditScores, Pageable pageable);
}
