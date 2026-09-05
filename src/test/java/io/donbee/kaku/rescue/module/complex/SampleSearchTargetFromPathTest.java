package io.donbee.kaku.rescue.module.complex;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import rescuecore2.worldmodel.EntityID;

/**
 * Unit tests for the pure path-to-target selection logic of SampleSearch.
 */
class SampleSearchTargetFromPathTest {

  private List<EntityID> ids(Integer... values) {
    return Stream.of(values).map(v -> new EntityID(v))
        .collect(Collectors.toList());
  }

  @Test
  @DisplayName("long path returns node three steps before the end")
  void longPathReturnsThirdToLast() {
    // given
    List<EntityID> path = ids(1, 2, 3, 4, 5, 6, 7);

    // when
    EntityID target = SampleSearch.targetFromPath(path);

    // then
    assertEquals(new EntityID(5), target);
  }

  @Test
  @DisplayName("three-node path still counts as long: picks first node")
  void threeNodePathPicksFirstNode() {
    // given
    List<EntityID> path = ids(1, 2, 3);

    // when
    EntityID target = SampleSearch.targetFromPath(path);

    // then
    assertEquals(new EntityID(1), target);
  }

  @Test
  @DisplayName("two-node path returns destination node")
  void shortPathReturnsLast() {
    // given
    List<EntityID> path = ids(1, 2);

    // when
    EntityID target = SampleSearch.targetFromPath(path);

    // then
    assertEquals(new EntityID(2), target);
  }

  @Test
  @DisplayName("single-node path returns that node")
  void singleNodePathReturnsItself() {
    // given
    List<EntityID> path = ids(42);

    // when
    EntityID target = SampleSearch.targetFromPath(path);

    // then
    assertEquals(new EntityID(42), target);
  }

  @Test
  @DisplayName("null path yields null target")
  void nullPathYieldsNull() {
    // given
    List<EntityID> path = null;

    // when
    EntityID target = SampleSearch.targetFromPath(path);

    // then
    assertNull(target);
  }

  @Test
  @DisplayName("empty path yields null target")
  void emptyPathYieldsNull() {
    // given
    List<EntityID> path = Collections.emptyList();

    // when
    EntityID target = SampleSearch.targetFromPath(path);

    // then
    assertNull(target);
  }
}
