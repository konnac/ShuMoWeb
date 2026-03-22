package com.konnac.service;

import com.konnac.User;
import org.springframework.stereotype.Service;

public interface LoginService {
    User login(User user);
}
