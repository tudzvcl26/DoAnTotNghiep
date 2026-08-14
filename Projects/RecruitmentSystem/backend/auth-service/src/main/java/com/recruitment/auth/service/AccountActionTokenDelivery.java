package com.recruitment.auth.service;

import com.recruitment.auth.entity.AccountActionPurpose;
import com.recruitment.auth.entity.User;

public interface AccountActionTokenDelivery {
    void deliver(User user, AccountActionPurpose purpose, String rawToken);
}
