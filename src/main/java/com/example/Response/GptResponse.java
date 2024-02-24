package com.example.Response;

import lombok.Data;
import org.springframework.web.bind.annotation.ResponseBody;

@Data
@ResponseBody
public class GptResponse {
    int code;
    String message;

    public GptResponse(int i, String ans) {
        this.code = i;
        this.message = ans;
    }
}
