package com.example.ai;

import lombok.Data;

import java.util.List;

@Data
public class Answer {
    private String id;
    private String object;
    private int created;
    private String result;
    private boolean is_truncated;
    private boolean need_clear_history;
    private String finish_reason;
    private Usage usage;
}