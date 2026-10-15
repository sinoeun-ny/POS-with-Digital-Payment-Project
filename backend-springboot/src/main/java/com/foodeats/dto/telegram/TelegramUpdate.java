package com.foodeats.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdate {

    @JsonProperty("update_id")
    private Long updateId;

    @JsonProperty("message")
    private TelegramMessage message;

    @JsonProperty("edited_message")
    private TelegramMessage editedMessage;

    @JsonProperty("callback_query")
    private TelegramCallbackQuery callbackQuery;

    public TelegramUpdate() {}

    public Long getUpdateId() { return updateId; }
    public void setUpdateId(Long updateId) { this.updateId = updateId; }

    public TelegramMessage getMessage() { return message; }
    public void setMessage(TelegramMessage message) { this.message = message; }

    public TelegramMessage getEditedMessage() { return editedMessage; }
    public void setEditedMessage(TelegramMessage editedMessage) { this.editedMessage = editedMessage; }

    public TelegramCallbackQuery getCallbackQuery() { return callbackQuery; }
    public void setCallbackQuery(TelegramCallbackQuery callbackQuery) { this.callbackQuery = callbackQuery; }
}
