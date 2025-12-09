package com.jobtracker.ats.dto;

import com.jobtracker.ats.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private Role role;
}
