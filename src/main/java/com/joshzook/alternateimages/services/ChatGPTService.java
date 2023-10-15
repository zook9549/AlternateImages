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
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatGPTService implements AnswerService, ImageGenerationService {

    public String getAnswer(String question) {
        log.debug("Asking {}", question);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + chatgptApiKey);
        headers.set("Content-Type", "application/json");

        List<Map<String, String>> conversation = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        String engineeredPrompt = "Answer without stating that you cannot predict the future. Provide the answer as a description that can be used as an image prompt that can best describe the answer as a visual.";
        log.debug("Fetching answer with prompt: {}", engineeredPrompt);
        systemMessage.put("content", engineeredPrompt);
        conversation.add(systemMessage);
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        conversation.add(userMessage);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.8);
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", conversation);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(chatGptChatUrl, HttpMethod.POST, entity, Map.class);

        Map responseBody = response.getBody();
        String answer = ((Map<?, ?>) ((Map<?, ?>) ((List<?>) responseBody.get("choices")).get(0)).get("message")).get("content").toString();
        log.debug("Completed getting image prompt: {}", answer);
        return answer;
    }

    public boolean isQuestion(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        text = text.trim().toLowerCase();

        if (text.endsWith("?")) {
            return true;
        }
        String[] questionWords = {"who", "what", "when", "where", "why", "how"};
        for (String word : questionWords) {
            if (text.startsWith(word)) {
                return true;
            }
        }
        return false;
    }


    public List<BufferedImage> getImage(String prompt, Styles style) {
        if (style != null) {
            prompt = "Create in the style of " + style.getDisplayName() + "." + prompt;
        }
        log.debug("Asking ChatGPT {}", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + chatgptApiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("response_format", "b64_json");
        requestBody.put("size", "256x256");
        requestBody.put("n", 4);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(chatGptImageUrl, HttpMethod.POST, entity, Map.class);

        List<Map<String, String>> imageList = (List<Map<String, String>>) response.getBody().get("data");
        List<BufferedImage> parsedImages = imageList.stream()
                .map(img -> Base64.getDecoder().decode(img.get("b64_json")))
                .map(ByteArrayInputStream::new)
                .map(bytesStream -> {
                    try {
                        return ImageIO.read(bytesStream);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .collect(Collectors.toList());
        return parsedImages;
    }
    @Value("${chatgpt.api.key}")
    private String chatgptApiKey;
    @Value("${chatgpt.api.image.url}")
    private String chatGptImageUrl;
    @Value("${chatgpt.api.chat.url}")
    private String chatGptChatUrl;


    private static final Logger log = LoggerFactory.getLogger(ChatGPTService.class);
}
