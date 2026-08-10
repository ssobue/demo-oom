package jp.sobue.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DisplayName("Test ApplicationContext Loading")
class DemoApplicationTests {

  @Autowired
  private ApplicationContext context;

  @Test
  @DisplayName("Inject ApplicationContext")
  void test() {
    assertNotNull(context);
  }
}
