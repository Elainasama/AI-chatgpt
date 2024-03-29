package com.example.ai;

import lombok.Data;

import java.util.List;

@Data
public class Answer {
    private String id;
    private String object;
    private int created;
    private String model;
    private List<Choices> choices;
    private Usage usage;
    private String system_fingerprint;
}