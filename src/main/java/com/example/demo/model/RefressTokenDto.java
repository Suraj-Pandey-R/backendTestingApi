package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefressTokenDto {
        private String access_token ;
        private String expires_in ;
        private String token_type ;
        private String scope ;
        private String consent_valid_till ;
        private String refresh_token ;
        private String digilockerid ;
        private String name ;
        private String dob ;
        private String gender ;
        private String eaadhaar ;
        private String reference_key ;
}
