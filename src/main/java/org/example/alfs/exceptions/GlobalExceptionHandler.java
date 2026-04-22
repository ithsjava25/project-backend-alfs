package org.example.alfs.exceptions;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(
            ResponseStatusException ex,
            Model model,
            HttpServletResponse response
    ) {

        int status = ex.getStatusCode().value();
        response.setStatus(status);

        model.addAttribute("status", status);
        model.addAttribute("error", ex.getReason());

        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(
            Exception ex,
            Model model,
            HttpServletResponse response
    ) {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        model.addAttribute("status", 500);
        model.addAttribute("error", "Something went wrong");

        return "error";
    }
}