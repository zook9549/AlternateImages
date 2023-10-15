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
        log.debug("Asking Monster API {}", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + key);
        headers.set("Content-Type", "multipart/form-data");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("aspect_ratio", "landscape");
        body.add("samples", 2);
        if (style != null) {
            String mappedStyle = mapStyle(style);
            if(mappedStyle != null) {
                body.add("style", mappedStyle);
                prompt = "Create in the style of " + style.getDisplayName() + ". " + prompt;
            }
        }
        body.add("prompt", prompt);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
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
    private String mapStyle(Styles style) {
        switch (style) {
            case analog_film:
                return "analog-film";
            case anime:
                return "anime";
            case cinematic:
                return "expressionism";
            case comic_book:
                return "comic-book";
            case digital_art:
                return "digital-art";
            case enhance:
                return "enhance";
            case fantasy_art:
                return "fantasy-art";
            case isometric:
                return "isometric";
            case line_art:
                return "line-art";
            case lowpoly:
                return "lowpoly";
            case neonpunk, cyberpunk:
                return "neonpunk";
            case origami:
                return "origami";
            case photographic:
                return "photographic";
            case watercolor:
                return "watercolor";
            case pixel:
                return "pixel-art";
            default:
                return null;
        }
    }

    @Value("${monster.api.key}")
    private String key;
    @Value("${monster.api.url}")
    private String url;

    private static final Logger log = LoggerFactory.getLogger(DeepAIService.class);
}
