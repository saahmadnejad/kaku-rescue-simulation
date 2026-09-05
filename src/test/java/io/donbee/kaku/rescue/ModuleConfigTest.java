package io.donbee.kaku.rescue;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies config/classpath consistency: every module class referenced in
 * config/module.cfg must exist and be loadable. Catches package renames that
 * break runtime module resolution.
 */
class ModuleConfigTest {

  /** Lines of module.cfg excluding comments and blanks; " : " is the separator. */
  private List<String> cfgEntries() throws IOException {
    return Files.readAllLines(Path.of("config/module.cfg")).stream()
        .map(String::trim)
        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
        .toList();
  }

  private String classOf(String entry) {
    String value = entry.split(":", 2)[1].trim();
    return value;
  }

  /** True when the value looks like a fully qualified class name. */
  private boolean isClassName(String value) {
    return value.matches("([a-zA-Z_$][a-zA-Z0-9_$]*\\.)+[a-zA-Z_$][a-zA-Z0-9_$]*");
  }

  @Test
  @DisplayName("every module.cfg entry references a loadable class")
  void moduleCfgEntriesAreLoadableClasses() throws IOException {
    // given
    List<String> entries = cfgEntries();
    assertFalse(entries.isEmpty(), "module.cfg must have entries");

    // when / then (AAA: Act + Assert per entry)
    for (String entry : entries) {
      String fqn = classOf(entry);
      if (!isClassName(fqn)) {
        continue; // plain config value, not a class reference
      }
      assertDoesNotThrow(
          () -> Class.forName(fqn),
          "module.cfg references class that cannot be loaded: " + fqn);
    }
  }

  @Test
  @DisplayName("every kaku class in module.cfg lives under io.donbee.kaku.rescue")
  void kakuModulesUseTeamPackage() throws IOException {
    // given
    List<String> entries = cfgEntries();

    // when / then
    for (String entry : entries) {
      String fqn = classOf(entry);
      if (!isClassName(fqn)) {
        continue; // plain config value, not a class reference
      }
      if (fqn.startsWith("io.donbee.kaku.")) {
        assertTrue(fqn.startsWith("io.donbee.kaku.rescue."),
            "kaku class outside team package: " + fqn);
      }
    }
  }

  @Test
  @DisplayName("no module.cfg entry still references old sample_team package")
  void noStaleSampleTeamReferences() throws IOException {
    // given
    List<String> entries = cfgEntries();

    // when / then
    for (String entry : entries) {
      assertFalse(entry.contains("sample_team"),
          "stale sample_team reference: " + entry);
    }
  }
}
