package com.example.ale.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
public class AleRestService {

    @Value("${ale.host}")
    private String aleHost;

    private static HttpHeaders getHeaders ()
    {
        String adminuserCredentials = "admin:welcome123";
        String encodedCredentials =
                new String(Base64.encodeBase64(adminuserCredentials.getBytes()));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Basic " + encodedCredentials);
        httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return httpHeaders;
    }

//    public Map<String, Object> getSomething() {
//        final String uri = "https://" + aleHost + "/api/v1/geo_fence";
//
//        HttpHeaders httpHeaders = getHeaders();
//
//        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
//
//        RestTemplate restTemplate = new RestTemplate();
//        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<Map<String, Object>>(){};
//
//        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, responseType);
//        //String result = restTemplate.getForObject(uri, String.class);
//        //log.info("QQQQQQQQQQQQQQQQQQQQQQQQQ geofence rest api result :{}", result);
//        return responseEntity.getBody();
//    }

    public String getSomething2() {

        String clientId = "admin";
        String clientSecret = "welcome123";

        final String uri = "https://" + aleHost + "/api/v1/geo_fence";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        String token = new String(Base64.encodeBase64((clientId + ":" + clientSecret).getBytes()));
        headers.add("Authorization", "Basic " + token);
        HttpEntity<?> request = new HttpEntity<>(headers);
        log.info(request.toString());
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);
        log.info("URI -" + builder.toUriString());
        ResponseEntity<String> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, request, String.class);
        log.info("Response" + response.getBody());

        return response.getBody();
    }

    public String getSomething() {
        final String uri = "https://" + aleHost + "/api/v1/geo_fence";

        RestTemplate restTemplate = new RestTemplate();

        String result = restTemplate.getForObject(uri, String.class);
        log.info("QQQQQQQQQQQQQQQQQQQQQQQQQ geofence rest api result :{}", result);
        return result;
    }
}
