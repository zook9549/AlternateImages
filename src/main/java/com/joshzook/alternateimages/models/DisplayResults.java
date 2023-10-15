package com.joshzook.alternateimages.models;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DisplayResults {
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Map<String, List<BufferedImage>> images = new HashMap<>();
    String prompt;
    String question;
    Styles style;
}
