package com.joshzook.alternateimages.services;

import com.joshzook.alternateimages.models.Display;

import java.awt.image.BufferedImage;

public interface DisplayService {

    void display(BufferedImage image);

    Display getDetails();
}
