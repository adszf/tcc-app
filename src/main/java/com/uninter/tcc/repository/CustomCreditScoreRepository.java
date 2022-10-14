package com.uninter.tcc.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.uninter.tcc.domain.entity.CustomCreditScoreEntity;

@Repository
public interface CustomCreditScoreRepository extends MongoRepository<CustomCreditScoreEntity, String> {

   /*  List<CustomCreditScoreEntity> findByMonth(String Month); */

    List<CustomCreditScoreEntity> findByIdade(int age);

    CustomCreditScoreEntity findByFakeIdCpf(Long cpf);

    List<CustomCreditScoreEntity> findByRegiao(String regiao);

    /* List<CustomCreditScoreEntity> findByMonthAndAge(String month, int age); */
}
