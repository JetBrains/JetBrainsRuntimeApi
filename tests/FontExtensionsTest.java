/*
 * Copyright 2000-2024 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
  @test
  @summary checking visualisation of drawing text with custom OpenType's features
  @run main/othervm -Djava2d.font.subpixelResolution=1x1 FontExtensionsTest
*/

import com.jetbrains.FontExtensions;
import com.jetbrains.JBR;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class FontExtensionsTest {
    @Retention(RetentionPolicy.RUNTIME)
    private @interface JBRTest {}

    private static final int IMG_WIDTH = 500;
    private static final int IMG_HEIGHT = 50;
    private static final String LIGATURES_STRING = "== != -> <>";
    private static final String FRACTION_STRING = "1/2";
    private static final String ZERO_STRING = "0";
    private static final String TEST_STRING = "hello abc 012345 " + FRACTION_STRING + LIGATURES_STRING;
    private static final Font BASE_FONT = new Font("JetBrains Mono", Font.PLAIN, 20);

    private static BufferedImage getImageWithString(Font font, String str) {
        BufferedImage img = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);

        g.setColor(Color.black);
        g.setFont(font);
        g.drawString(str, 20, 20);
        g.dispose();

        return img;
    }

    private static Boolean isImageEquals(BufferedImage first, BufferedImage second) {
        for (int y = 0; y < first.getHeight(); y++) {
            for (int x = 0; x < first.getWidth(); x++) {
                if (first.getRGB(x, y) != second.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Font fontWithFeatures(Font font, String... features) {
        return JBR.getFontExtensions().deriveFontWithFeatures(font, features);
    }

    private static Font fontWithFeatures(String... features) {
        return fontWithFeatures(BASE_FONT, features);
    }

    private static Boolean textDrawingEquals(Font font1, Font font2, String text) {
        BufferedImage image1 = getImageWithString(font1, text);
        BufferedImage image2 = getImageWithString(font2, text);
        return isImageEquals(image1, image2);
    }

    @JBRTest
    private static Boolean testFeaturesToString() {
        String[] features = {"zero", "salt=123", "frac=0"};
        Font font = JBR.getFontExtensions().deriveFontWithFeatures(BASE_FONT, features);
        return Arrays.equals(JBR.getFontExtensions().getEnabledFeatures(font), features);
    }

    @JBRTest
    private static Boolean testDisablingLigatureByDefault() {
        return textDrawingEquals(BASE_FONT, fontWithFeatures(FontExtensions.FeatureTag.ZERO), LIGATURES_STRING);
    }

    @JBRTest
    private static Boolean testSettingValuesToFeatures() {
        Font font = JBR.getFontExtensions().deriveFontWithFeatures(BASE_FONT, "zero", "salt=123", "frac=9999");
        return textDrawingEquals(fontWithFeatures(FontExtensions.FeatureTag.FRAC, FontExtensions.FeatureTag.ZERO,
                FontExtensions.FeatureTag.SALT), font, TEST_STRING);
    }

    @JBRTest
    private static Boolean testLigature() {
        Font font = BASE_FONT.deriveFont(Map.of(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON));
        return !textDrawingEquals(BASE_FONT, font, LIGATURES_STRING);
    }

    // TODO include this test when will be found suitable example
    // @JBRTest
    private static Boolean testKerning() {
        Font font = BASE_FONT.deriveFont(Map.of(TextAttribute.KERNING, TextAttribute.KERNING_ON));
        return !textDrawingEquals(BASE_FONT, font, TEST_STRING);
    }

    @JBRTest
    private static Boolean testTracking() {
        Font font = BASE_FONT.deriveFont(Map.of(TextAttribute.TRACKING, 0.3f));
        return !textDrawingEquals(BASE_FONT, font, TEST_STRING);
    }

    @JBRTest
    private static Boolean testWeight() {
        Font font = BASE_FONT.deriveFont(Map.of(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD));
        return !textDrawingEquals(BASE_FONT, font, TEST_STRING);
    }

    @JBRTest
    private static Boolean testDisablingFeatures() {
        Font font = JBR.getFontExtensions().deriveFontWithFeatures(BASE_FONT, "zero=0", "frac=0");
        return textDrawingEquals(BASE_FONT, font, TEST_STRING);
    }

    @JBRTest
    private static Boolean testFeaturesZero() {
        return !textDrawingEquals(BASE_FONT, fontWithFeatures(FontExtensions.FeatureTag.ZERO), ZERO_STRING);
    }

    @JBRTest
    private static Boolean testFeaturesFrac() {
        return !textDrawingEquals(BASE_FONT, fontWithFeatures(FontExtensions.FeatureTag.FRAC), FRACTION_STRING);
    }

    @JBRTest
    private static Boolean testFeaturesDerive1() {
        Font fontFZ1 = fontWithFeatures(FontExtensions.FeatureTag.FRAC, FontExtensions.FeatureTag.ZERO).
                deriveFont(Map.of(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON));
        Font fontFZ2 = fontWithFeatures(BASE_FONT.deriveFont(Map.of(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON)),
                FontExtensions.FeatureTag.FRAC, FontExtensions.FeatureTag.ZERO);
        return  textDrawingEquals(fontFZ1, fontFZ2, TEST_STRING);
    }

    @JBRTest
    private static Boolean testFeaturesDerive2() {
        Font fontFZ1 = fontWithFeatures(FontExtensions.FeatureTag.FRAC, FontExtensions.FeatureTag.ZERO);
        Font fontFZ2 = fontWithFeatures(BASE_FONT.deriveFont(Map.of(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON)),
                FontExtensions.FeatureTag.FRAC, FontExtensions.FeatureTag.ZERO);
        fontFZ2 = fontFZ2.deriveFont(Map.of(TextAttribute.LIGATURES, false));
        return  textDrawingEquals(fontFZ1, fontFZ2, TEST_STRING);
    }

    @JBRTest
    private static Boolean testTrackingWithFeatures() {
        Font font = BASE_FONT.deriveFont(Map.of(TextAttribute.TRACKING, 0.3f));
        return !textDrawingEquals(font, fontWithFeatures(font, FontExtensions.FeatureTag.ZERO), TEST_STRING);
    }

    @JBRTest
    private static Boolean testFeaturesEmpty() {
        return textDrawingEquals(BASE_FONT, fontWithFeatures(), TEST_STRING);
    }

    public static void main(final String[] args) {
        if (!JBR.isFontExtensionsSupported()) {
            throw new RuntimeException("JBR FontExtension API is not available");
        }

        String error = "";
        try {
            for (final Method method : FontExtensionsTest.class.getDeclaredMethods()) {
                if (method.isAnnotationPresent(JBRTest.class)) {
                    System.out.print("Testing " + method.getName() + "...");
                    boolean statusOk = (boolean) method.invoke(null);
                    if (!statusOk) {
                        error += System.lineSeparator() + "Test failed: " + method.getName();
                    }
                    System.out.println(statusOk ? "passed" : "failed");
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("JBR: internal error during testing", e);
        }

        if (!error.isEmpty()) {
            throw new RuntimeException(error);
        }
    }
}
