package com.joshzook.alternateimages.services;

import com.joshzook.alternateimages.models.Styles;
import com.joshzook.alternateimages.utilties.ImageUtilties;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class DeepAIService implements ImageGenerationService {
    @Override
    public List<BufferedImage> getImage(String prompt, Styles style) {
        if (style != null) {
            prompt = "Create in the style of " + style.getDisplayName() + "." + prompt;
        }
        log.debug("Asking DeepAI {}", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", deepApiKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("text", prompt);
        body.add("width", "512");
        body.add("height", "256");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(deepApiUrl + '/' + mapStyle(style), HttpMethod.POST, requestEntity, Map.class);
        String result = response.getBody().get("output_url").toString();
        log.debug("Completed getting image response from DeepAI: {}", result);
        BufferedImage originalImage;
        try {
            byte[] imageBytes = ImageUtilties.downloadFileBytes(new URL(result));
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            originalImage = ImageIO.read(bis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage[] subImages = new BufferedImage[4];
        int count = 0;
        for (int y = 0; y < height; y += height / 2) {
            for (int x = 0; x < width; x += width / 2) {
                subImages[count] = originalImage.getSubimage(x, y, width / 2, height / 2);
                count++;
            }
        }
        return Arrays.asList(subImages);
    }

    private String mapStyle(Styles style) {
        if(style == null) {
            return "text2img";
        }
        switch (style) {
            case cute:
                return "cute-creature-generator";
            case fantasy_art:
                return "fantasy-portrait-generator";
            case anime:
                return "anime-portrait-generator";
            case impressionism:
                return "impressionism-painting-generator";
            case artistic:
                return "renaissance-painting-generator";
            case old_style:
                return "old-style-generator";
            case cyberpunk, neonpunk:
                return "cyberpunk-portrait-generator";
            case logo:
                return "logo-generator";
            case watercolor:
                return "watercolor-painting-generator";
            case pop_art:
                return "pop-art-generator";
            case origami:
                return "origami-3d-generator";
            case pixel:
                return "pixel-art-generator";
            case photorealistic:
                return "photorealistic-portrait-generator";
            default:
                return "text2img";
        }
    }

    @Value("${deepai.api.key}")
    private String deepApiKey;
    @Value("${deepai.api.url}")
    private String deepApiUrl;

    private static final Logger log = LoggerFactory.getLogger(DeepAIService.class);
}
