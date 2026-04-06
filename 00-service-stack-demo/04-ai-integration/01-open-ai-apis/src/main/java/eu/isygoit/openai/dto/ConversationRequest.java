package eu.isygoit.openai.dto;

import lombok.Getter;

/**
 * The type Conversation request.
 */
@Getter
public class ConversationRequest {
    private String message;
    private String[] conversationHistory;
}
