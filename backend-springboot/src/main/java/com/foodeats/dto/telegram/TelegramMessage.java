package com.foodeats.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramMessage {

    @JsonProperty("message_id")
    private Long messageId;

    @JsonProperty("from")
    private TelegramUser from;

    @JsonProperty("chat")
    private TelegramChat chat;

    @JsonProperty("date")
    private Long date;

    @JsonProperty("text")
    private String text;

    @JsonProperty("location")
    private TelegramLocation location;

    public TelegramMessage() {}

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public TelegramUser getFrom() { return from; }
    public void setFrom(TelegramUser from) { this.from = from; }

    public TelegramChat getChat() { return chat; }
    public void setChat(TelegramChat chat) { this.chat = chat; }

    public Long getDate() { return date; }
    public void setDate(Long date) { this.date = date; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public TelegramLocation getLocation() { return location; }
    public void setLocation(TelegramLocation location) { this.location = location; }

    public Long getChatId() {
        return chat != null ? chat.getId() : null;
    }
}
