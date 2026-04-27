package org.example.alfs.config;

import org.example.alfs.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {
  private static final Logger log = LoggerFactory.getLogger(GlobalModelAttributes.class);
  private final SecurityUtils securityUtils;

  public GlobalModelAttributes(SecurityUtils securityUtils) {
    this.securityUtils = securityUtils;
  }

  @ModelAttribute
  public void addGlobalAttributes(Model model) {

    boolean isLoggedIn = false;
    String username = null;
    String role = null;
    try {
      var user = securityUtils.getCurrentUser();
      isLoggedIn = true;
      username = user.getUsername();
      role = user.getRole().name();
    } catch (RuntimeException ex) {
      log.debug("Could not resolve current user for global model attributes", ex);
    }

    model.addAttribute("isLoggedIn", isLoggedIn);
    model.addAttribute("username", username);
    model.addAttribute("role", role);
  }
}
