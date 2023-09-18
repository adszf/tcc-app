package com.uninter.tcc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.uninter.tcc.model.ClassifierEntity;
@Repository
public interface ClassifierRepository extends MongoRepository<ClassifierEntity, String> {

    Page<ClassifierEntity> findByContext(String idContext, Pageable pageableClassifier);}