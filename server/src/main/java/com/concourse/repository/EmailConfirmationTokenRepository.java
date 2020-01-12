package com.concourse.repository;

import com.concourse.models.tokens.ConfirmationToken;
import org.springframework.data.repository.CrudRepository;

public interface EmailConfirmationTokenRepository extends CrudRepository<ConfirmationToken, String> {

    ConfirmationToken findConfirmationTokenByEmail(String email);
}
