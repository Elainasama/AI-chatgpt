package com.example.ai;

import lombok.Data;


@Data
public class Choices {
    private Integer index;
    private Message message;
    private String logprobs;
    private String finish_reason;
}
