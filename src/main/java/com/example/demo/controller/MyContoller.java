package com.example.demo.controller;
import com.example.demo.model.TakeData;
import com.example.demo.service.Service1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@Slf4j
public class MyContoller {
    private final Service1 service;
    public MyContoller(Service1 service) {
        this.service = service;
    }
    @GetMapping
    public String hello(){
        log.info("-----------------------------some one is pinging hi -----------------------------------");
        return "Hi ping";
    }

    @GetMapping("/start")
    public String getCodeAndState(@RequestParam("code_challenge") String code_challenge , @RequestParam("code") String code){
        log.info("-------------------------------------inside the code");
        return service.getAccessTokenApi(code, code_challenge);
    }
    @GetMapping("/accesstoken/{token}")
    public String refressToken(@PathVariable("token") String token){
        log.info("---------------------- calling refress token api");
        return service.refressAccessToken(token);
    }

    @GetMapping("/takedata")
    public TakeData getAccesDataAndToken(){
        return service.tempdata();
    }
}
