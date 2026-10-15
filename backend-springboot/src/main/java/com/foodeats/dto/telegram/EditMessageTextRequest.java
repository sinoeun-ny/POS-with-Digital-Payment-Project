package com.foodeats.dto.telegram;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditMessageTextRequest {

    @JsonProperty("chat_id")
    private Object chatId;

    @JsonProperty("message_id")
    private Long messageId;

    @JsonProperty("text")
    private String text;

    @JsonProperty("parse_mode")
    private String parseMode = "Markdown";

    @JsonProperty("reply_markup")
    private InlineKeyboardMarkup replyMarkup;

    public EditMessageTextRequest() {}

    public EditMessageTextRequest(Object chatId, Long messageId, String text, String parseMode, InlineKeyboardMarkup replyMarkup) {
        this.chatId = chatId;
        this.messageId = messageId;
        this.text = text;
        this.parseMode = parseMode;
        this.replyMarkup = replyMarkup;
    }

    public Object getChatId() { return chatId; }
    public void setChatId(Object chatId) { this.chatId = chatId; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getParseMode() { return parseMode; }
    public void setParseMode(String parseMode) { this.parseMode = parseMode; }

    public InlineKeyboardMarkup getReplyMarkup() { return replyMarkup; }
    public void setReplyMarkup(InlineKeyboardMarkup replyMarkup) { this.replyMarkup = replyMarkup; }
}
