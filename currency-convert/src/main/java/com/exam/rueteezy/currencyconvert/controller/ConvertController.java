package com.exam.rueteezy.currencyconvert.controller;

import com.alibaba.fastjson.JSON;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ConvertController {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping(value = "/convert")
    Map<String,String> currencyConvert(@RequestParam("fromCurrency") String fromCurrency, @RequestParam("toCurrency") String toCurrency,
                                           @RequestParam("inputAmount") double inputAmount){

        String url = "http://api.exchangeratesapi.io/v1/latest?access_key=0c7565fa40b41bf16bdc773605a089c3&symbols="+fromCurrency+","+toCurrency+"&format=1";

        RestTemplate restTemplate = new RestTemplate();

        // get result from url
        String result = restTemplate.getForObject(url, String.class);

        Map<String,String> httpResponse = new HashMap();

        JSONObject jsonObject;
        try {
            // convert result to json
            jsonObject = new JSONObject(result);

            // get rates from json
            String rate = jsonObject.get("rates").toString();

            // get request rates
            Map<String,BigDecimal> text = JSON.parseObject(rate,Map.class);

            // compute the final value
            double finalValue = (inputAmount / text.get(fromCurrency.toUpperCase()).doubleValue())
                    * text.get(toCurrency.toUpperCase()).doubleValue() ;
            double convertedValue = finalValue/inputAmount;

            httpResponse.put(fromCurrency.toUpperCase(),doubleFormat(inputAmount));
            httpResponse.put(toCurrency.toUpperCase(),doubleFormat(finalValue));
            httpResponse.put("conversion","1 "+ fromCurrency+ " = " + doubleFormat(convertedValue)+ " "+toCurrency);
            httpResponse.put("selling",doubleFormat(getFinalValue(finalValue,"SELLING")));
            httpResponse.put("buying",doubleFormat(getFinalValue(finalValue,"BUYING")));

        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request Error");
        }
        return httpResponse;
    }

    private double getFinalValue(double value , String toDo){
        if (toDo.equals("SELLING")){
            return value * 1.02;
        }
        return  value * 0.98;
    }

    private String doubleFormat(double value){
        return String.format("%.2f",value);
    }
}
