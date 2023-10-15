package com.joshzook.alternateimages.utilties;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BufferedImageSerializer extends StdSerializer<BufferedImage> {

    public BufferedImageSerializer() {
        super(BufferedImage.class);
    }

    @Override
    public void serialize(BufferedImage value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(value, "png", baos);
        byte[] bytes = baos.toByteArray();
        gen.writeBinary(bytes);
    }
}
