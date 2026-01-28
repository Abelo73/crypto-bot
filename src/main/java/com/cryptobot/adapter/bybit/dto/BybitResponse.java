package com.cryptobot.adapter.bybit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Base Bybit API v5 response
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitResponse<T> {
    private int retCode;
    private String retMsg;
    private T result;
    private Object retExtInfo;
    private long time;
}
