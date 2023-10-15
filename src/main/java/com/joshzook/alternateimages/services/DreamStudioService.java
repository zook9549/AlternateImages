package com.joshzook.alternateimages.services;

import com.joshzook.alternateimages.models.Styles;
import com.joshzook.alternateimages.utilties.ImageUtilties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DreamStudioService implements ImageGenerationService {
    @Override
    public List<BufferedImage> getImage(String prompt, Styles style) {
        log.debug("Asking Dream Studio API {}", prompt);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + key);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("height", 512);
        requestBody.put("width", 1024);
        requestBody.put("samples", 2);
        if (style != null) {
            String mappedStyle = mapStyle(style);
            if(mappedStyle != null) {
                requestBody.put("style_preset", mappedStyle);
            } else {
                prompt = "Create in the style of " + style.getDisplayName() + ". " + prompt;
            }
        }

        Map<String, String> textPrompts = new HashMap<>();
        textPrompts.put("text", prompt);
        requestBody.put("text_prompts", Collections.singletonList(textPrompts));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        List<Map<String, String>> imageList = (List<Map<String, String>>) response.getBody().get("artifacts");
        List<BufferedImage> parsedImages = imageList.stream()
                .map(img -> Base64.getDecoder().decode(img.get("base64")))
                .map(ByteArrayInputStream::new)
                .map(bytesStream -> {
                    try {
                        return ImageIO.read(bytesStream);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .collect(Collectors.toList());
        log.debug("Completed getting image response from Dream Studio API");
        return parsedImages;
    }

    private String mapStyle(Styles style) {
        if(style == null) {
            return null;
        }
        switch (style) {
            case analog_film:
                return "analog-film";
            case anime:
                return "anime";
            case cinematic:
                return "cinematic";
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
                return "low-poly";
            case neonpunk, cyberpunk:
                return "neon-punk";
            case origami:
                return "origami";
            case photographic:
                return "photographic";
            case pixel:
                return "pixel-art";
            default:
                return null;
        }
    }

    @Value("${dreamstudio.api.key}")
    private String key;
    @Value("${dreamstudio.api.url}")
    private String url;

    private static final Logger log = LoggerFactory.getLogger(DeepAIService.class);
}
