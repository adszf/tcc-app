package com.uninter.tcc.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.uninter.tcc.model.CreditScoreEntity;

@Repository
public interface CreditScoreRepository extends MongoRepository<CreditScoreEntity, String> {

    List<CreditScoreEntity> findByMonth(String Month);

    List<CreditScoreEntity> findByAge(int age);

    CreditScoreEntity findByFakeIdCpf(Long i);

    List<CreditScoreEntity> findByMonthAndAge(String month, int age);
}
