package com.joshzook.alternateimages.services;

import com.joshzook.alternateimages.models.Styles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MonsterAPIService implements ImageGenerationService {
    @Override
    public List<BufferedImage> getImage(String prompt, Styles style) {
        if (style != null) {
            prompt = "Create in the style of " + style.getDisplayName() + "." + prompt;
        }
        log.debug("Asking Monster API {}", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + key);
        headers.set("Content-Type", "multipart/form-data");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("prompt", prompt);
        body.add("aspect_ratio", "landscape");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url + "/generate/txt2img", HttpMethod.POST, requestEntity, Map.class);
        String processId = (String) response.getBody().get("process_id");
        List<BufferedImage> results = fetchResults(processId);
        log.debug("Completed getting image response from Monster API");
        return results;
    }

    private List<BufferedImage> fetchResults(String processId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + key);
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url + "/status/" + processId, HttpMethod.GET, entity, Map.class);
        String status = (String) response.getBody().get("status");
        if (status.equalsIgnoreCase("failed")) {
            throw new RuntimeException("Failed to generate image " + processId);
        } else if (status.equalsIgnoreCase("completed")) {
            List<String> results = ((Map<String, List>) response.getBody().get("result")).get("output");
            List<BufferedImage> images = new ArrayList<>();
            for (String result : results) {
                try {
                    log.debug("Downloading MonsterAPI image from {}", result);
                    URL url = new URL(result);
                    images.add(ImageIO.read(url));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return images;
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return fetchResults(processId);
        }
    }

    @Value("${monster.api.key}")
    private String key;
    @Value("${monster.api.url}")
    private String url;

    private static final Logger log = LoggerFactory.getLogger(DeepAIService.class);
}
