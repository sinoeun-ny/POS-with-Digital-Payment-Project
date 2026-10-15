package com.foodeats.dto.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramLocation {

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("heading")
    private Integer heading;

    @JsonProperty("horizontal_accuracy")
    private Double horizontalAccuracy;

    @JsonProperty("live_period")
    private Integer livePeriod;

    public TelegramLocation() {}

    public TelegramLocation(Double latitude, Double longitude, Integer heading, Double horizontalAccuracy, Integer livePeriod) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.heading = heading;
        this.horizontalAccuracy = horizontalAccuracy;
        this.livePeriod = livePeriod;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getHeading() { return heading; }
    public void setHeading(Integer heading) { this.heading = heading; }

    public Double getHorizontalAccuracy() { return horizontalAccuracy; }
    public void setHorizontalAccuracy(Double horizontalAccuracy) { this.horizontalAccuracy = horizontalAccuracy; }

    public Integer getLivePeriod() { return livePeriod; }
    public void setLivePeriod(Integer livePeriod) { this.livePeriod = livePeriod; }
}
