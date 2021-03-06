package com.concourse.repository;

import com.concourse.models.tokens.LoginToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends CrudRepository<LoginToken, String> {
}
