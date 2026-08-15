package com.smartlogistics.client;

import com.smartlogistics.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class DocVerifyClient {

    private static final Logger log = LoggerFactory.getLogger(DocVerifyClient.class);
    private final RestClient restClient;

    public DocVerifyClient(@Value("${app.services.doc-verify-url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.png";
                }
            };
        } catch (IOException e) {
            throw new ApiException("Failed to read uploaded file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyDrivingLicenseCombined(MultipartFile frontFile, MultipartFile backFile) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("front_file", toResource(frontFile));
        body.add("back_file", toResource(backFile));

        try {
            return restClient.post()
                    .uri("/api/v1/documents/verify-combined")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("AI service call failed (/api/v1/documents/verify-combined): {}", e.getMessage());
            throw new ApiException("Document verification service unavailable: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyAadhaar(MultipartFile frontFile, MultipartFile backFile) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("front_file", toResource(frontFile));
        body.add("back_file", toResource(backFile));

        try {
            return restClient.post()
                    .uri("/api/v1/documents/verify-aadhaar")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("AI service call failed (/api/v1/documents/verify-aadhaar): {}", e.getMessage());
            throw new ApiException("Document verification service unavailable: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyRC(MultipartFile file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", toResource(file));

        try {
            return restClient.post()
                    .uri("/api/v1/documents/verify-rc")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("AI service call failed (/api/v1/documents/verify-rc): {}", e.getMessage());
            throw new ApiException("Document verification service unavailable: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyPUC(MultipartFile file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", toResource(file));

        try {
            return restClient.post()
                    .uri("/api/v1/documents/verify-puc")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("AI service call failed (/api/v1/documents/verify-puc): {}", e.getMessage());
            throw new ApiException("Document verification service unavailable: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyInsurance(MultipartFile file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", toResource(file));

        try {
            return restClient.post()
                    .uri("/api/v1/documents/verify-insurance")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("AI service call failed (/api/v1/documents/verify-insurance): {}", e.getMessage());
            throw new ApiException("Document verification service unavailable: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyPermit(MultipartFile file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", toResource(file));

        try {
            return restClient.post()
                    .uri("/api/v1/documents/verify-permit")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("AI service call failed (/api/v1/documents/verify-permit): {}", e.getMessage());
            throw new ApiException("Document verification service unavailable: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
