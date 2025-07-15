package eu.isygoit.openai.dto;

import lombok.Data;

@Data
public class ConversationRequest {
    private String message;
    private String[] conversationHistory;
}
