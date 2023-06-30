package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetails {
    private String digilockerid ;
    private String name ;
    private String dob ;
    private String gender ;
    private String eaadhaar ;
    private String reference_key ;
    private String mobile;
}
