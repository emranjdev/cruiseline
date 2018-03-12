package com.cruiseline.repository;


import org.springframework.data.repository.CrudRepository;

import com.cruiseline.model.User;

public interface UserRepository extends CrudRepository<User, Integer> {

    User findByUsername(String username);

}
