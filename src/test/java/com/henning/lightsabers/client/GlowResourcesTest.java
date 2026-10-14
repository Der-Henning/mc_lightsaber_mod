package com.henning.lightsabers.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GlowResourcesTest {
    private static final List<String> SABERS = List.of(
        "lightsaber", "blue_lightsaber", "green_lightsaber", "red_lightsaber", "purple_lightsaber", "yellow_lightsaber"
    );

    @Test
    void allAurasHaveASmoothLoopAndTransparentEdges() throws Exception {
        for (String saber : SABERS) {
            try (var stream = resource("textures/item/" + saber + "_aura.png")) {
                BufferedImage image = ImageIO.read(stream);
                assertEquals(64, image.getWidth());
                assertEquals(128 * 40, image.getHeight());
                int minimum = 255;
                int maximum = 0;
                for (int frame = 0; frame < 40; frame++) {
                    int center = alpha(image, 32, frame * 128 + 64);
                    minimum = Math.min(minimum, center);
                    maximum = Math.max(maximum, center);
                    assertEquals(0, alpha(image, 0, frame * 128 + 64));
                    assertEquals(0, alpha(image, 63, frame * 128 + 64));
                    assertEquals(0, alpha(image, 32, frame * 128));
                    assertEquals(0, alpha(image, 32, frame * 128 + 127));
                    int next = alpha(image, 32, ((frame + 1) % 40) * 128 + 64);
                    assertTrue(Math.abs(center - next) < 10, "No flash at the animation loop seam");
                }
                assertTrue(maximum - minimum > 40, "The aura must visibly pulse");
                assertTrue(minimum > 0 && maximum < 180, "The aura stays softly translucent");
            }
            JsonObject animation = json("textures/item/" + saber + "_aura.png.mcmeta").getAsJsonObject("animation");
            assertEquals(1, animation.get("frametime").getAsInt());
            assertTrue(animation.get("interpolate").getAsBoolean());
        }
    }

    @Test
    void aurasFollowTheOriginalModelTransformsWithoutCoveringTheHilt() throws Exception {
        for (String saber : SABERS) {
            JsonObject core = json("models/item/" + saber + ".json");
            JsonObject aura = json("models/item/" + saber + "_aura.json");
            assertEquals(core.get("display"), aura.get("display"));
            assertEquals(4, aura.getAsJsonArray("elements").size());
            for (var value : aura.getAsJsonArray("elements")) {
                JsonObject element = value.getAsJsonObject();
                assertEquals(15, element.get("light_emission").getAsInt());
                assertFalse(element.get("shade").getAsBoolean());
                assertTrue(element.getAsJsonArray("from").get(1).getAsDouble() >= 6.8D);
            }
            JsonObject item = json("items/" + saber + ".json").getAsJsonObject("model");
            assertEquals("minecraft:composite", item.get("type").getAsString());
            assertEquals(2, item.getAsJsonArray("models").size());
        }
    }

    private static int alpha(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) >>> 24;
    }

    private static java.io.InputStream resource(String name) {
        var stream = GlowResourcesTest.class.getResourceAsStream("/assets/lightsabers/" + name);
        assertNotNull(stream, name);
        return stream;
    }

    private static JsonObject json(String name) throws Exception {
        try (var reader = new InputStreamReader(resource(name), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
