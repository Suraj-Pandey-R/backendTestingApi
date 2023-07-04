package com.example.demo.service;

import com.example.demo.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class Service1 {
    List<String> l = new ArrayList<String>();
    private HttpClient httpClient = HttpClient.newHttpClient();

    private TakeData takeData = new TakeData();
    private TakeData panData = new TakeData();


    public String getAccessTokenApi(String code, String code_challenge) {
        String requestBody = String.format("grant_type=%s&code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&code_verifier=%s",
                URLEncoder.encode("authorization_code", StandardCharsets.UTF_8),
                URLEncoder.encode(code, StandardCharsets.UTF_8),
                URLEncoder.encode("IAE3E4C164", StandardCharsets.UTF_8),
                URLEncoder.encode("eb25a081b28750063826", StandardCharsets.UTF_8),
                URLEncoder.encode("https://first.d1ds8gytdtrzs9.amplifyapp.com/call", StandardCharsets.UTF_8),
                URLEncoder.encode(code_challenge, StandardCharsets.UTF_8));
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
        log.info("-----------------------------file{}-------------", response.body());
        AccessTokenDto accessTokenDto =  convertJsonStringToObjectType(response.body(), AccessTokenDto.class);
        log.info("-------------------------------- inserting data to take data format");
        takeData.setBearer(accessTokenDto.getAccess_token());
        log.info("----------------------- succesful access token details converted value {}", accessTokenDto);
        log.info("------------------------- call to get userDetailsService");
        log.info("------------------------------------------- filling the information of getUser details service");
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
        log.info("----------------------getaadhar in xml");
        gete_AadhaarDataInXML(accessToken);
        ListOfIssuedDocs items = convertJsonStringToObjectType(response.body(), ListOfIssuedDocs.class);
        log.info("-----------------------------list of json format {}", items);
        log.info("----------------------------- calling each to check");
        log.info("------------------------------acccesstoken, file uri {}, {}" , accessToken, items.getItems().get(0).getUri());
        log.info("---------------------------inserting data to take data set uri");
        takeData.setUri(items.getItems().get(0).getUri());

        for(int i= 0 ; i < items.getItems().size(); i++){
            if(items.getItems().get(i).getDoctype().equals("PANCR")){
                log.info("----------------------calling from inside");
                getDatainXMLformat(accessToken, items.getItems().get(i).getUri());
                getFileFromURI(accessToken, items.getItems().get(i).getUri());
            }
        }
        if(items.getItems().size() == 2){
            getFileFromURI3(accessToken, items.getItems().get(2).getUri());
        }
        getFileFromURI(accessToken, items.getItems().get(0).getUri());
        log.info("----------------------------------------- old api to get data");
        getFileFromURI2(accessToken, items.getItems().get(0).getUri());
        log.info("----------------------------------------------- get file uri deployment");
        log.info("----------------------------------------------get data in xml format");
        getFileFromURI3(accessToken, items.getItems().get(0).getUri());
        savePdfToDesktop(accessToken, items.getItems().get(0).getUri());
        savePdfToDesktop2(accessToken, items.getItems().get(0).getUri());
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
    public void getFileFromURI3(String accessToken, String fileUrl) {
        log.info("---------------------------inside get file from URI");
        log.info("---------------------------content loaded for frontend");
        log.info("---------------------------content loaded for frontend");
        log.info("---------------------------content loaded for frontend");
        log.info("---------------------------content loaded for frontend");
        String url = "https://digilocker.meripehchaan.gov.in/public/oauth2/1/file/" + fileUrl + ".pdf";
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/pdf")
                .header("Authorization", "Bearer " + accessToken);
        HttpRequest request = requestBuilder.GET().build();
        HttpResponse<String> httpResponse = null;
        try{
            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e){
            log.info("----------------------------in catch exception is here");
        }
        log.info("--------------------------{}", httpResponse);
        log.info("---------------------------{}", httpResponse.body());
        log.info("---------------------------{}", httpResponse.headers());
    }

    //    Get File from URI
    public void getFileFromURI(String accessToken, String fileUrl){
        log.info("---------------------------inside get file from URI");
        log.info("---------------------------content loaded for frontend");
        log.info("---------------------------content loaded for frontend");
        log.info("---------------------------content loaded for frontend");
        log.info("---------------------------content loaded for frontend");
        String url = "https://digilocker.meripehchaan.gov.in/public/oauth2/1/file/"+ fileUrl ;
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/pdf")
                .header("Authorization", "Bearer " + accessToken);

        HttpRequest request = requestBuilder.GET().build();
        HttpResponse<String> httpResponse = null ;
        HttpResponse<InputStream> httpm = null;
        log.info("---------------------------------calling to another file");
        getFileFromURI2(accessToken, fileUrl);
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

    public void getFileFromURI2(String accessToken, String fileUrl){
        log.info("---------------------------second part of inside get file from URI ");
        String url = "https://api.digitallocker.gov.in/public/oauth2/1/file/"+ fileUrl;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/pdf")
                .header("Authorization", "Bearer " + accessToken)
                .GET().build();
        HttpResponse<InputStream> httpm = null;
        try{
            httpm = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        }
        catch (Exception e){
            log.info("------------------------------exception in input stream");
        }
        log.info("----------==///////////////========-first data ");
        log.info("---------------------------{}", httpm);
        log.info("-----------------------------{}", httpm.headers());
        log.info("------------------------------{}", httpm.body());
    }

//    pull document api
//    public void pullDocumentApi(String accessToken){
//        log.info("---------------------------------- inside the get aadhaar in xml");
//        String requestBody = String.format("orgid=%s&doctype=%s&consent=%s&panno=%s&PANFullName=%s",
//                URLEncoder.encode("001891", StandardCharsets.UTF_8),
//                URLEncoder.encode("PANCR" , StandardCharsets.UTF_8),
//                URLEncoder.encode("Y", StandardCharsets.UTF_8),
//                URLEncoder.encode("DRGPK1162K", StandardCharsets.UTF_8),
//                URLEncoder.encode("", StandardCharsets.UTF_8));
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("https://digilocker.meripehchaan.gov.in/public/oauth2/1/pull/pulldocument"))
//                .header("Accept", "application/json")
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                .build();
//        HttpResponse<String> httpResponse = null ;
//        try{
//            httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//        }
//        catch (Exception e){
//            log.info("----------------------------in catch exception is here");
//        }
//        log.info("------------------------{}", httpResponse);
//        log.info("------------------------{}", httpResponse.headers());
//        log.info("-------------------------{}", httpResponse.body());
//    }

//    Get Certificate Data in XML Format from URI
    public void getDatainXMLformat(String accessToken, String uri){
        String url = "https://digilocker.meripehchaan.gov.in/public/oauth2/1/xml/"+ uri ;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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
    public TakeData tempdata(){
        log.info("----------------------data getting {}", takeData);
        return takeData ;
    }
    public TakeData getPanData(){
        log.info("------------------------call pan data {}", panData);
        return  panData ;
    }
    public void savePdfToDesktop2(String acessToken, String uri) {
        log.info("----------------------save pdf 2");
        log.info("----------------------save pdf 2");
        log.info("----------------------save pdf 2");
        log.info("----------------------save pdf 2");
        log.info("----------------------save pdf 2");
        log.info("----------------------save pdf 2");
        log.info("----------------------save pdf 2");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_PDF));
        headers.set("Authorization", "Bearer <your-token>");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    "https://digilocker.meripehchaan.gov.in/public/oauth2/1/file/" + uri,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            byte[] responseBody = response.getBody();
            if (responseBody != null) {
                log.info("--------------------------------inside pdf code {}", new String(responseBody));
            } else {
                log.info("--------------------------------inside pdf code: Response body is null");
            }

            if (response.getStatusCode() == HttpStatus.OK) {
                byte[] pdfData = response.getBody();

                // Generate a unique file name based on the current timestamp
                String fileName = "/home/ubuntu/pdf_file_data/file_" + System.currentTimeMillis() + ".pdf";
                // Save the received PDF file to the default directory
                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                    fos.write(pdfData);
                    System.out.println("PDF file saved successfully");
                } catch (IOException e) {
                    System.out.println("Failed to save PDF file");
                    e.printStackTrace();
                }
            } else {
                System.out.println("Failed to receive PDF file");
            }
        }
        catch (Exception e){
            log.info("----------------------exception is therer");
        }

    }
    public void savePdfToDesktop(String acessToken, String uri) {
        log.info("----------------------------------inside save pdf to desktop");
        log.info("----------------------------------inside save pdf to desktop");
        log.info("----------------------------------inside save pdf to desktop");
        log.info("----------------------------------inside save pdf to desktop");
        log.info("----------------------------------inside save pdf to desktop");
        log.info("----------------------------------inside save pdf to desktop");
        log.info("----------------------------------inside save pdf to desktop");
        log.info("----------------------------------inside save pdf to desktop");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_PDF));
        headers.set("Authorization", "Bearer <your-token>");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    "https://digilocker.meripehchaan.gov.in/public/oauth2/1/file/" + uri + ".pdf",
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );
            byte[] responseBody = response.getBody();
            if (responseBody != null) {
                log.info("--------------------------------inside pdf code {}", new String(responseBody));
            } else {
                log.info("--------------------------------inside pdf code: Response body is null");
            }
            if (response.getStatusCode() == HttpStatus.OK) {
                byte[] pdfData = response.getBody();
                // Generate a unique file name based on the current timestamp
                String fileName = "/home/ubuntu/pdf_file_data/file_" + System.currentTimeMillis() + ".pdf";

                // Save the received PDF file to the default directory
                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                    fos.write(pdfData);
                    System.out.println("PDF file saved successfully");
                } catch (IOException e) {
                    System.out.println("Failed to save PDF file");
                    e.printStackTrace();
                }
            } else {
                System.out.println("Failed to receive PDF file");
            }
        }
        catch (Exception e){
            log.info("----------------------exception is there");
        }
    }


}
