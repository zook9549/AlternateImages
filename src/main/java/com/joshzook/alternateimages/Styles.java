package com.joshzook.alternateimages;


import lombok.Getter;

@Getter
public enum Styles {
    Cyberpunk, Artistic, Pixel, Impressionism, Cute, Anime, Old_Style, Pop_Art, Logo;

    Styles(String displayName) {
        this.displayName = displayName;
    }

    Styles() {
        this.displayName = this.name().replace('_', ' ');
    }
    private final String displayName;
}
