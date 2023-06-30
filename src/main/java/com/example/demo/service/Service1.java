package com.example.demo.service;

import com.example.demo.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class Service1 {

    private HttpClient httpClient = HttpClient.newHttpClient();


    public String getAccessTokenApi(String code, String code_challenge) {
        String requestBody = String.format("grant_type=%s&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&code_verifier=%s",
                URLEncoder.encode("authorization_code", StandardCharsets.UTF_8),
                URLEncoder.encode(code, StandardCharsets.UTF_8),
                URLEncoder.encode("IAE3E4C164", StandardCharsets.UTF_8),
                URLEncoder.encode("eb25a081b28750063826", StandardCharsets.UTF_8),
                URLEncoder.encode("https://first.d1ds8gytdtrzs9.amplifyapp.com/call", StandardCharsets.UTF_8),
                URLEncoder.encode(code_challenge, StandardCharsets.UTF_8));
        log.info("------------------------------------------{}---", requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://digilocker.meripehchaan.gov.in/public/oauth2/2/token"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        log.info("-----------------after making http req");
        HttpResponse<String> response;
        try {
            log.info("-------------------------------------in try block making req");
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            log.info("------------------------Io exception is here");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.info("--------------------------------interruptedExceptin is herer");
            throw new RuntimeException(e);
        }
        log.info("-----------------------------at last i am here before ----------------------{} ", response);
        log.info("------------------------empty-------------------------");
        log.info("-----------------------------file{}-------------", response.body());
        log.info("--------------------------------------1{}-----------------------------", response.request());
        log.info("--------------------------------------2{}-----------------------------", response.statusCode());
        log.info("--------------------------------------3{}-----------------------------", response.uri());
        log.info("--------------------------------------4{}-----------------------------", response.sslSession());
        log.info("-----------------------------to string {}--------------------------------", response.toString());
        AccessTokenDto accessTokenDto =  convertJsonStringToObjectType(response.body(), AccessTokenDto.class);
        log.info("----------------------- succesful access token details converted value {}", accessTokenDto);
        log.info("------------------------- call to get userDetailsService");
        getUserDetails(accessTokenDto.getAccess_token());
        log.info("--------------------going to issued docs");
        getListOfissuedDocuments(accessTokenDto.getAccess_token());
        return "successfull";
    }
    public <T> T convertJsonStringToObjectType(String jsonString, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.readValue(jsonString, clazz);
        }
        catch (JsonMappingException e) {
            log.info("-----------------------------jsonmapping exception");
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            log.info("------------------------------json processing exception");
            throw new RuntimeException(e);
        }
    }
    public <T> List<T> convertJsonArrayToObjectType(String jsonArray, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(jsonArray, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void getUserDetails(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://digilocker.meripehchaan.gov.in/public/oauth2/1/user"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .GET().build();
        log.info("-------------------------------inside the get digilocker request --------");
        HttpResponse<String> response = null;
        try {
            log.info("--------------------------in try block of--");
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.info("----------------------catch block of access token method");
            e.printStackTrace();
        }
        log.info("--------------------body of user details services------- {}", response.body());
        UserDetails userDetails = convertJsonStringToObjectType(response.body(), UserDetails.class);
        log.info("--------------------succesfull fetch the user details services");
    }


    public void  getListOfissuedDocuments(String accessToken){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://digilocker.meripehchaan.gov.in/public/oauth2/2/files/issued"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .GET().build();
        log.info("-------------------------------inside the get List of Documents request --------");
        HttpResponse<String> response = null;
        try {
            log.info("--------------------------in try block of get list --");
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.info("----------------------catch block of list of isssued docs");
            e.printStackTrace();
        }
        log.info("-----------------------------list{}", response);
        log.info("-----------------------------code", response.statusCode());
        log.info("-----------------------------in list of docs of user -- {} ", response.body());
        log.info("-----------------------------------------------------------------------------");
        log.info("-------------------getaadhar in xml");
        gete_AadhaarDataInXML(accessToken);
        ListOfIssuedDocs items = convertJsonStringToObjectType(response.body(), ListOfIssuedDocs.class);
        log.info("-----------------------------list of json format {}", items);
        log.info("----------------------------- calling each to check");
        List<Items> ls = items.getItems();
        log.info("---------------------------calling the file from uri");
        getFileFromURI(accessToken, items.getItems().get(0).getUri());
    }

    public String refressAccessToken(String accessToken){
        String credentials = "IAE3E4C164" + "eb25a081b28750063826" ;
        String requestBody = String.format("refresh_token=%s&grant_type=%s",
                URLEncoder.encode(accessToken, StandardCharsets.UTF_8),
                URLEncoder.encode("refresh_token" + StandardCharsets.UTF_8));

        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://digilocker.meripehchaan.gov.in/public/oauth2/1/token"))
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = null;
        try {
            log.info("--------------------------in try block of get list --");
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.info("----------------------catch block of list of isssued docs");
            e.printStackTrace();
        }
        log.info("----------------------------{}", response);
        log.info("---------------------------{}", response.body());

        RefressTokenDto refressTokenDto = convertJsonStringToObjectType(response.body(), RefressTokenDto.class);
        log.info("----------------calling to another service {}" , refressTokenDto );
        log.info("calling to another service");
        getListOfissuedDocuments(refressTokenDto.getAccess_token());
        return refressTokenDto.getAccess_token() ;
    }

    // Get e-Aadhaar Data in XML Format
    public void gete_AadhaarDataInXML(String accessToken){
        log.info("---------------------------------- inside the get aadhaar in xml");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://digilocker.meripehchaan.gov.in/public/oauth2/3/xml/eaadhaar"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .GET().build();
        HttpResponse<String> httpResponse = null ;
        try{
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e){
            log.info("----------------------------in catch exception is here");
        }
        log.info("------------------------{}", httpResponse);
        log.info("------------------------{}", httpResponse.headers());
        log.info("-------------------------{}", httpResponse.body());
    }

    //    Get File from URI
    public void getFileFromURI(String accessToken, String fileUrl){
        log.info("---------------------------inside get file from URI");
        String url = "https://digilocker.meripehchaan.gov.in/public/oauth2/1/file/"+ fileUrl;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/pdf")
                .header("Authorization", "Bearer " + accessToken)
                .GET().build();
        HttpResponse<String> httpResponse = null ;
        HttpResponse<InputStream> httpm = null;
        try{
            httpm = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        }
        catch (Exception e){
            log.info("------------------------------exception in input stream");
        }
        try{
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e){
            log.info("----------------------------in catch exception is here");
        }
        log.info("----------==///////////////========-first data ");
        log.info("---------------------------{}", httpm);
        log.info("-----------------------------{}", httpm.headers());
        log.info("------------------------------{}", httpm.body());
        log.info("--------------------------------------------second data-------------------------------");
        log.info("--------------------------{}", httpResponse);
        log.info("---------------------------{}", httpResponse.body());
        log.info("---------------------------{}", httpResponse.headers());
    }
}
