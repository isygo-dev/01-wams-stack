package eu.isygoit.openai.dto;

import lombok.Data;

/**
 * The type Conversation request.
 */
@Data
public class ConversationRequest {
    private String message;
    private String[] conversationHistory;
}
