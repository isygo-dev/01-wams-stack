package eu.isygoit.openai.dto;

import lombok.Data;

@Data
public class GeminiResponse {
    private boolean success;
    private String generatedText;
    private String errorMessage;

    public static GeminiResponse success(String generatedText) {
        GeminiResponse response = new GeminiResponse();
        response.setSuccess(true);
        response.setGeneratedText(generatedText);
        return response;
    }

    public static GeminiResponse error(String errorMessage) {
        GeminiResponse response = new GeminiResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}