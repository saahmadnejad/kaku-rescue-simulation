package io.donbee.kaku.rescue;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies launch.cfg consistency: team name set and agent count values
 * are valid (-1 = all, 0..n = exact count).
 */
class LaunchConfigTest {

  private Properties loadLaunchCfg() throws IOException {
    Properties p = new Properties();
    try (FileInputStream in = new FileInputStream("config/launch.cfg")) {
      p.load(in);
    }
    return p;
  }

  @Test
  @DisplayName("team name is configured")
  void teamNameIsConfigured() throws IOException {
    // given
    Properties cfg = loadLaunchCfg();

    // when
    String teamName = cfg.getProperty("team.name", "");

    // then
    assertFalse(teamName.isBlank(), "team.name must not be empty");
  }

  @Test
  @DisplayName("agent counts are valid integers (-1 or >= 0)")
  void agentCountsAreValid() throws IOException {
    // given
    Properties cfg = loadLaunchCfg();

    // when / then
    cfg.stringPropertyNames().stream()
        .filter(key -> key.endsWith(".count"))
        .forEach(key -> {
          int value = Integer.parseInt(cfg.getProperty(key));
          assertTrue(value == -1 || value >= 0,
              "invalid count in " + key + ": " + value);
        });
  }
}
