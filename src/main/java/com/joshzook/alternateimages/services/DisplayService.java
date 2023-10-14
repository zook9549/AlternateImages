package com.joshzook.alternateimages.services;

import com.joshzook.alternateimages.models.Display;

import java.awt.image.BufferedImage;

public interface DisplayService {

    public void display(BufferedImage image);

    public Display getDetails();
}
