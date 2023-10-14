package com.joshzook.alternateimages.models;


import lombok.Getter;

@Getter
public enum Styles {
    analog_film, anime, artistic, cinematic, comic_book, craft_clay, cute, cyberpunk, digital_art, enhance, fantasy_art, futuristic, impressionism, isometric, line_art, logo, lowpoly, neonpunk, old_style, origami, photographic, photorealistic, pixel, pixel_art, pop_art, realism, texture, watercolor;

    Styles(String displayName) {
        this.displayName = displayName;
    }

    Styles() {
        this.displayName = this.name().replace('_', ' ');
    }

    private final String displayName;
    }
