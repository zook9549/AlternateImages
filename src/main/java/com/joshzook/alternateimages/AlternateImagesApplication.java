package com.joshzook.alternateimages;

import com.joshzook.alternateimages.models.Display;
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

    @RequestMapping(value = "/ask", produces = "image/png")
    public byte[] answer(@RequestParam(name = "prompt") String question, @RequestParam(required = false) Styles style) throws Exception {
        String answer;
        bufferedImages.clear();
        if (answerService.isQuestion(question)) {
            answer = answerService.getAnswer(question);
        } else {
            answer = question;
        }
        List<BufferedImage> imgs = getImage(answer, style);
        BufferedImage combined = ImageUtilties.appendVertically(imgs);
        bufferedImages.addAll(imgs);
        return ImageUtilties.getByteArray(combined);
    }

    @RequestMapping(value = "/image", produces = "image/png")
    public List<BufferedImage> getImage(String prompt, @RequestParam(required = false) Styles style) {
        List<BufferedImage> parsedImages = new ArrayList<>();
        for (ImageGenerationService imageGenerationService : imageGenerationServices) {
            try {
                parsedImages.addAll(imageGenerationService.getImage(prompt, style));
            } catch (Exception e) {
                log.error("Error getting image from service", e);
            }
        }

        return parsedImages;
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


    private final AnswerService answerService;
    private final List<ImageGenerationService> imageGenerationServices;
    private final DisplayService displayService;
    private final List<BufferedImage> bufferedImages = new ArrayList<>();

    @Value("${display.push.delay}")
    private int displayPushDelay;


    private static final Logger log = LoggerFactory.getLogger(AlternateImagesApplication.class);
}
