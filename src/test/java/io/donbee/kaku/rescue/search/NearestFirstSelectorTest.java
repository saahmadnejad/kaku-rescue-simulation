package io.donbee.kaku.rescue.search;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import rescuecore2.worldmodel.EntityID;

/** Unit tests for the nearest-first selector logic. */
class NearestFirstSelectorTest {

  private EntityID id(int value) {
    return new EntityID(value);
  }

  private NearestFirstSelector selectorWith(List<EntityID> cannedPath) {
    return new NearestFirstSelector((from, destinations) -> cannedPath);
  }

  @Test
  @DisplayName("empty candidates yield null target")
  void emptyCandidatesYieldNull() {
    // given
    NearestFirstSelector selector = selectorWith(null);

    // when
    EntityID target = selector.select(Collections.emptyList(), id(0));

    // then
    assertNull(target);
  }

  @Test
  @DisplayName("null candidates yield null target")
  void nullCandidatesYieldNull() {
    // given
    NearestFirstSelector selector = selectorWith(null);

    // when
    EntityID target = selector.select(null, id(0));

    // then
    assertNull(target);
  }

  @Test
  @DisplayName("long path returns node three steps before the end")
  void longPathReturnsThirdToLast() {
    // given
    List<EntityID> path = Arrays.asList(id(1), id(2), id(3), id(4), id(5), id(6), id(7));
    NearestFirstSelector selector = selectorWith(path);

    // when
    EntityID target = selector.select(List.of(id(7)), id(1));

    // then
    assertEquals(id(5), target);
  }

  @Test
  @DisplayName("three-node path picks first node as observation point")
  void threeNodePathPicksFirstNode() {
    // given
    List<EntityID> path = Arrays.asList(id(1), id(2), id(3));
    NearestFirstSelector selector = selectorWith(path);

    // when
    EntityID target = selector.select(List.of(id(3)), id(1));

    // then
    assertEquals(id(1), target);
  }

  @Test
  @DisplayName("two-node path returns destination node")
  void shortPathReturnsLast() {
    // given
    List<EntityID> path = Arrays.asList(id(1), id(2));
    NearestFirstSelector selector = selectorWith(path);

    // when
    EntityID target = selector.select(List.of(id(2)), id(1));

    // then
    assertEquals(id(2), target);
  }

  @Test
  @DisplayName("null path from router yields null target")
  void nullPathYieldsNull() {
    // given
    NearestFirstSelector selector = selectorWith(null);

    // when
    EntityID target = selector.select(List.of(id(9)), id(1));

    // then
    assertNull(target);
  }

  @Test
  @DisplayName("empty path from router yields null target")
  void emptyPathYieldsNull() {
    // given
    NearestFirstSelector selector = selectorWith(Collections.emptyList());

    // when
    EntityID target = selector.select(List.of(id(9)), id(1));

    // then
    assertNull(target);
  }
}
