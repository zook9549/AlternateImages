package com.joshzook.alternateimages.services;

import com.joshzook.alternateimages.models.Styles;

import java.awt.image.BufferedImage;
import java.util.List;

public interface ImageGenerationService {

    List<BufferedImage> getImage(String prompt, Styles style);
}
