package com.uninter.tcc.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.uninter.tcc.domain.entity.IssueEntity;
@Repository
public interface IssueRepository extends MongoRepository<IssueEntity, String> {}