package com.foodeats.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramCallbackQuery {

    @JsonProperty("id")
    private String id;

    @JsonProperty("from")
    private TelegramUser from;

    @JsonProperty("message")
    private TelegramMessage message;

    @JsonProperty("data")
    private String data;

    public TelegramCallbackQuery() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public TelegramUser getFrom() { return from; }
    public void setFrom(TelegramUser from) { this.from = from; }

    public TelegramMessage getMessage() { return message; }
    public void setMessage(TelegramMessage message) { this.message = message; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
