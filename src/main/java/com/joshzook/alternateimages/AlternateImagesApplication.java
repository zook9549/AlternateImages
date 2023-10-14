package com.joshzook.alternateimages;

import com.joshzook.alternateimages.models.Styles;
import com.joshzook.alternateimages.services.AnswerService;
import com.joshzook.alternateimages.services.DisplayService;
import com.joshzook.alternateimages.services.ImageGenerationService;
import com.joshzook.alternateimages.utilties.ImageUtilties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
@EnableScheduling
public class AlternateImagesApplication {

    public AlternateImagesApplication(AnswerService answerService, List<ImageGenerationService> imageGenerationServices, DisplayService displayService) {
        this.answerService = answerService;
        this.imageGenerationServices = imageGenerationServices;
        this.displayService = displayService;
    }

    public static void main(String[] args) {
        SpringApplication.run(AlternateImagesApplication.class, args);
    }

    @RequestMapping(value = "/ask", produces = "application/json")
    public Map<String, Object> answer(@RequestParam(name = "prompt") String question, @RequestParam(required = false) Styles style) throws Exception {
        String answer;
        if (answerService.isQuestion(question)) {
            answer = answerService.getAnswer(question);
        } else {
            answer = question;
        }
        Map<String, Object> response = new HashMap<>();
        Map<String, List<BufferedImage>> imgs = getImage(answer, style);
        Map<String, List<byte[]>> byteMap = convertToByteMap(imgs);
        response.put("images", byteMap);
        response.put("device", displayService.getDetails());
        response.put("prompt", answer);
        bufferedImages.clear();
        List<BufferedImage> allImages = imgs.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        bufferedImages.addAll(allImages);
        return response;
    }

    @RequestMapping(value = "/image", produces = "image/png")
    public Map<String, List<BufferedImage>> getImage(String prompt, @RequestParam(required = false) Styles style) {
        Map<String, List<BufferedImage>> response = new HashMap<>();
        for (ImageGenerationService imageGenerationService : imageGenerationServices) {
            List<BufferedImage> parsedImages = new ArrayList<>();
            try {
                parsedImages.addAll(imageGenerationService.getImage(prompt, style));
                response.put(imageGenerationService.getClass().getSimpleName(), parsedImages);
            } catch (Exception e) {
                log.error("Error getting image from service", e);
            }
        }

        return response;
    }

    @RequestMapping(value = "/styles")
    public Map<Styles, String> getStyles() {
        Map<Styles, String> stylesMap = new HashMap<>();
        for (Styles style : Styles.values()) {
            stylesMap.put(style, style.getDisplayName());
        }
        return stylesMap;
    }

    @Scheduled(fixedRate = 2000)
    public void pushImages() throws Exception {
        List<BufferedImage> images = new ArrayList<>(bufferedImages);
        if (!images.isEmpty()) {
            for (BufferedImage bufferedImage : images) {
                displayService.display(bufferedImage);
                Thread.sleep(displayPushDelay);
            }
        }
    }

    private Map<String, List<byte[]>> convertToByteMap(Map<String, List<BufferedImage>> imageMap) {
        return imageMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> convertImageListToByteList(entry.getValue())
                ));
    }

    private List<byte[]> convertImageListToByteList(List<BufferedImage> images) {
        return images.stream()
                .map(ImageUtilties::getByteArray)
                .collect(Collectors.toList());
    }

    private final AnswerService answerService;
    private final List<ImageGenerationService> imageGenerationServices;
    private final DisplayService displayService;
    private final List<BufferedImage> bufferedImages = new ArrayList<>();

    @Value("${display.push.delay}")
    private int displayPushDelay;


    private static final Logger log = LoggerFactory.getLogger(AlternateImagesApplication.class);
}
