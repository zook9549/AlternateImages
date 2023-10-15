package com.joshzook.alternateimages;

import com.joshzook.alternateimages.models.DisplayResults;
import com.joshzook.alternateimages.models.Styles;
import com.joshzook.alternateimages.services.AnswerService;
import com.joshzook.alternateimages.services.DisplayService;
import com.joshzook.alternateimages.services.ImageGenerationService;
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
import java.util.*;

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
    public DisplayResults answer(@RequestParam(name = "prompt") String question, @RequestParam(required = false) Styles style) {
        String answer;
        if (answerService.isQuestion(question)) {
            answer = answerService.getAnswer(question);
        } else {
            answer = question;
        }
        Map<String, List<BufferedImage>> imgs = getImage(answer, style);
        synchronized (displayResults) {
            displayResults.setQuestion(question);
            displayResults.setPrompt(answer);
            displayResults.setStyle(style);
            displayResults.setImages(imgs);
        }
        return displayResults;
    }

    @RequestMapping(value = "/current", produces = "application/json")
    public DisplayResults getCurrentDisplayResults() {
        return displayResults;
    }

    @RequestMapping(value = "/clear", produces = "application/json")
    public boolean clearDisplayResults() {
        displayResults.setQuestion(null);
        displayResults.setPrompt(null);
        displayResults.setStyle(null);
        displayResults.setImages(null);
        return true;
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
        if (displayResults.getImages() != null && !displayResults.getImages().isEmpty()) {
            List<BufferedImage> images = displayResults.getImages().values().stream().flatMap(List::stream).toList();
            for (BufferedImage bufferedImage : images) {
                displayService.display(bufferedImage);
                Thread.sleep(displayPushDelay);
            }
        }
    }

    private Map<String, List<BufferedImage>> getImage(String prompt, @RequestParam(required = false) Styles style) {
        Map<String, List<BufferedImage>> response = new HashMap<>();
        for (ImageGenerationService imageGenerationService : imageGenerationServices) {
            try {
                List<BufferedImage> parsedImages = new ArrayList<>(imageGenerationService.getImage(prompt, style));
                response.put(imageGenerationService.getClass().getSimpleName(), parsedImages);
            } catch (Exception e) {
                log.error("Error getting image from service", e);
            }
        }
        return response;
    }

    private final AnswerService answerService;
    private final List<ImageGenerationService> imageGenerationServices;
    private final DisplayService displayService;
    private final DisplayResults displayResults = new DisplayResults();

    @Value("${display.push.delay}")
    private int displayPushDelay;


    private static final Logger log = LoggerFactory.getLogger(AlternateImagesApplication.class);
}
