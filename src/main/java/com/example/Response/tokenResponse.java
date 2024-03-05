package com.example.Response;

import lombok.Data;
import org.springframework.web.bind.annotation.ResponseBody;

@Data
@ResponseBody
public class tokenResponse {
    private String refresh_token;
    private String expires_in;
    private String session_key;
    private String access_token;
    private String scope;
}
