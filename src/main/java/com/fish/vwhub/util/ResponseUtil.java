package com.fish.vwhub.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtil {

    public static void responseFront(HttpServletResponse response, int code, String msg){
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(code);
        try {
            response.getWriter().print(msg);
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
