package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessTokenDto {
    private String access_token ;
    private String expires_in ;
    private String token_type ;
    private String scope ;
    private String id_token ;
}
