package com.foodeats.dto.telegram;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnswerCallbackQueryRequest {

    @JsonProperty("callback_query_id")
    private String callbackQueryId;

    @JsonProperty("text")
    private String text;

    @JsonProperty("show_alert")
    private Boolean showAlert;

    public AnswerCallbackQueryRequest() {}

    public AnswerCallbackQueryRequest(String callbackQueryId, String text, Boolean showAlert) {
        this.callbackQueryId = callbackQueryId;
        this.text = text;
        this.showAlert = showAlert;
    }

    public String getCallbackQueryId() { return callbackQueryId; }
    public void setCallbackQueryId(String callbackQueryId) { this.callbackQueryId = callbackQueryId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Boolean getShowAlert() { return showAlert; }
    public void setShowAlert(Boolean showAlert) { this.showAlert = showAlert; }
}
