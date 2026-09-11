package eu.isygoit.openai.dto;

import lombok.Data;

/**
 * The type Gemini response.
 */
@Data
public class GeminiResponse {
    private boolean success;
    private String generatedText;
    private String errorMessage;

    /**
     * Success gemini response.
     *
     * @param generatedText the generated text
     * @return the gemini response
     */
    public static GeminiResponse success(String generatedText) {
        GeminiResponse response = new GeminiResponse();
        response.setSuccess(true);
        response.setGeneratedText(generatedText);
        return response;
    }

    /**
     * Error gemini response.
     *
     * @param errorMessage the error message
     * @return the gemini response
     */
    public static GeminiResponse error(String errorMessage) {
        GeminiResponse response = new GeminiResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}