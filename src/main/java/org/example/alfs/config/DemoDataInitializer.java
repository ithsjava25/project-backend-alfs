package org.example.alfs.config;

import lombok.RequiredArgsConstructor;
import org.example.alfs.services.DemoDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

  private final DemoDataService demoDataService;

  @Override
  public void run(String... args) {
    demoDataService.seedDemoData();
  }
}
