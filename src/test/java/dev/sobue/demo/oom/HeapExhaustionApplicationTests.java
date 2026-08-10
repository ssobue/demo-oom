package dev.sobue.demo.oom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that the heap exhaustion application context can start without OOM mode.
 */
@SpringBootTest
@DisplayName("Test HeapExhaustionApplication context loading")
class HeapExhaustionApplicationTests {

  @Autowired
  private ApplicationContext context;

  @Test
  @DisplayName("Context injection: creates the application context without starting heap exhaustion")
  void test() {
    assertNotNull(context);
  }
}
