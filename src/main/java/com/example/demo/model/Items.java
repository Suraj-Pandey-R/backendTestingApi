package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Items{
    private String name ;
    private String type ;
    private String size ;
    private String date ;
    private String parent ;
    private List<String> mime ;
    private String uri ;
    private String doctype ;
    private String description ;
    private String issuerid ;
    private String issuer ;
}
