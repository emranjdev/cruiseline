package com.cruiseline.service;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.cruiseline.model.User;
import com.cruiseline.model.UserFactory;
import com.cruiseline.repository.UserRepository;
import com.cruiseline.security.JwtService;


@Component
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ShaPasswordEncoder shaPasswordEncoder;
    @Autowired
    UserFactory userFactory;
    @Autowired
    JwtService jwtService;


    public void create(String username, String password, String role) {
        String salt = UUID.randomUUID().toString();
        User u = userFactory.create(username, shaPasswordEncoder.encodePassword(password, salt), salt, role);
        userRepository.save(u);
    }

    public User isLoginValid(String username, String pass)  {
        if(!StringUtils.hasText(username) || !StringUtils.hasText(pass)) {
            return null;
        }
        String password = new String(Base64.decodeBase64(pass.getBytes()));
        User u = userRepository.findByUsername(username);
        if(u == null) {
            return null;
        }
        if(!u.getPassword().equals(shaPasswordEncoder.encodePassword(password, u.getSalt()))) {
            return null;
        }
        return u;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUserToken(String username, String secret) {
    	Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_WEEK, 1);
        Date expirationDate = c.getTime();
        String token = jwtService.createToken(username, secret, expirationDate);
        User u = userRepository.findByUsername(username);
        u.setToken(token);
        return userRepository.save(u);
    }

    public User validateUser(String token, String secret) {
        String username = jwtService.getUsername(token, secret);
        if (username != null ) {
            User user = userRepository.findByUsername(username);
            if (user != null && token.equals(user.getToken()) && jwtService.isValid(token, secret)) {
                return user;
            }
        }
        return null;
    }
}
