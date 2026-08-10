package jp.sobue.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@RequiredArgsConstructor
public class DemoRunner implements ApplicationRunner {

  private final ApplicationContext context;

  @Override
  public void run(ApplicationArguments args) {
    Assert.notNull(context, "application context is null");
  }
}
