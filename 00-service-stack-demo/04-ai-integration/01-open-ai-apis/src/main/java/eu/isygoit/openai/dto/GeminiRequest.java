package eu.isygoit.openai.dto;

import lombok.Data;

@Data
public class GeminiRequest {
    private Content[] contents;
    private SafetySetting[] safetySettings;
    private GenerationConfig generationConfig;

    @Data
    public static class Content {
        private Part[] parts;
    }

    @Data
    public static class Part {
        private String text;
    }

    @Data
    public static class SafetySetting {
        private String category;
        private String threshold;

        public SafetySetting(String category, String threshold) {
            this.category = category;
            this.threshold = threshold;
        }
    }

    @Data
    public static class GenerationConfig {
        private double temperature;
        private double topP;
        private int maxOutputTokens;

        public GenerationConfig(double temperature, double topP, int maxOutputTokens) {
            this.temperature = temperature;
            this.topP = topP;
            this.maxOutputTokens = maxOutputTokens;
        }
    }
}