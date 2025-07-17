package eu.isygoit.openai.dto;

import lombok.Data;

/**
 * The type Gemini request.
 */
@Data
public class GeminiRequest {
    private Content[] contents;
    private SafetySetting[] safetySettings;
    private GenerationConfig generationConfig;

    /**
     * The type Content.
     */
    @Data
    public static class Content {
        private Part[] parts;
    }

    /**
     * The type Part.
     */
    @Data
    public static class Part {
        private String text;
    }

    /**
     * The type Safety setting.
     */
    @Data
    public static class SafetySetting {
        private String category;
        private String threshold;

        /**
         * Instantiates a new Safety setting.
         *
         * @param category  the category
         * @param threshold the threshold
         */
        public SafetySetting(String category, String threshold) {
            this.category = category;
            this.threshold = threshold;
        }
    }

    /**
     * The type Generation config.
     */
    @Data
    public static class GenerationConfig {
        private double temperature;
        private double topP;
        private int maxOutputTokens;

        /**
         * Instantiates a new Generation config.
         *
         * @param temperature     the temperature
         * @param topP            the top p
         * @param maxOutputTokens the max output tokens
         */
        public GenerationConfig(double temperature, double topP, int maxOutputTokens) {
            this.temperature = temperature;
            this.topP = topP;
            this.maxOutputTokens = maxOutputTokens;
        }
    }
}