package org.example.alfs.config;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.example.alfs.security.SecurityUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final SecurityUtils securityUtils;

    public GlobalModelAttributes(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {

        boolean isLoggedIn = false;

        try {
            securityUtils.getCurrentUser();
            isLoggedIn = true;
        } catch (Exception ignored) {}

        model.addAttribute("isLoggedIn", isLoggedIn);
    }
}