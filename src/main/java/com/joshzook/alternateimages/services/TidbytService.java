package com.joshzook.alternateimages.services;

import com.joshzook.alternateimages.models.Display;
import com.joshzook.alternateimages.utilties.ImageUtilties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class TidbytService implements DisplayService {

    @Override
    public void display(BufferedImage image) {
        try {
            BufferedImage scaledImage = ImageUtilties.scaleImage(image, display.getWidth(), display.getHeight());
            byte[] resizedImage = ImageUtilties.getByteArray(scaledImage);
            pushImage(resizedImage);
            log.trace("Pushed image to Tidbyt {}", tidbytDeviceId);
        } catch (Exception e) {
            log.error("Error pushing image to Tidbyt", e);
        }
    }

    @Override
    public Display getDetails() {
        return display;
    }

    private void pushImage(byte[] img) {
        String encodedImg = Base64.getEncoder().encodeToString(img);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tidbytApiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("image", encodedImg);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(tidbytUrl, HttpMethod.POST, entity, Map.class);
    }

    @Value("${tidbyt.api.key}")
    private String tidbytApiKey;
    @Value("${tidbyt.api.url}")
    private String tidbytUrl;
    @Value("${tidbyt.device.id}")
    private String tidbytDeviceId;
    private final Display display = new Display();

    {
        display.setId(tidbytDeviceId);
        display.setName("Tidbyt");
        display.setHeight(32);
        display.setWidth(64);
    }

    private static final Logger log = LoggerFactory.getLogger(TidbytService.class);

}
