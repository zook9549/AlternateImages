package com.joshzook.alternateimages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class AlternateImagesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlternateImagesApplication.class, args);
    }

    @RequestMapping(value = "/image", produces = "image/png")
    public byte[] getImage(String prompt) throws IOException {
        log.debug("Asking {}", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + chatgptApiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("response_format", "b64_json");
        requestBody.put("size", "256x256");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(chatGptUrl, HttpMethod.POST, entity, Map.class);

        Map<String, List> responseBody = response.getBody();
        String img = ((Map<String, String>) (responseBody.get("data")).get(0)).get("b64_json");
        pushImage(img);
        return Base64.getDecoder().decode(img);
    }

    private void pushImage(String img) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tidbytApiKey);
        headers.set("Content-Type", "application/json");

        byte[] resizedBytes = resizeBase64Image(img, 64, 32);

        // If you want to convert the resized bytes back to Base64:
        String resizedBase64Image = Base64.getEncoder().encodeToString(resizedBytes);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("image", resizedBase64Image);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(tidbytUrl, HttpMethod.POST, entity, Map.class);
    }

    public static byte[] resizeBase64Image(String base64Image, int targetWidth, int targetHeight) {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bis);

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
            Graphics2D graphics = resizedImage.createGraphics();
            graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", bos);
            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Value("${chatgpt.api.key}")
    private String chatgptApiKey;
    @Value("${chatgpt.api.url}")
    private String chatGptUrl;
    @Value("${tidbyt.api.key}")
    private String tidbytApiKey;
    @Value("${tidbyt.api.url}")
    private String tidbytUrl;


    private static final Logger log = LoggerFactory.getLogger(AlternateImagesApplication.class);
}
