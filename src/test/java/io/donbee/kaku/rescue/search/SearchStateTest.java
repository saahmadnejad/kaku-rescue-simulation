package io.donbee.kaku.rescue.search;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import rescuecore2.worldmodel.EntityID;

/** Unit tests for the persistent search memory. */
class SearchStateTest {

  private EntityID id(int value) {
    return new EntityID(value);
  }

  @Test
  @DisplayName("unseen entity needs search")
  void unseenEntityNeedsSearch() {
    // given
    SearchState state = new SearchState();

    // when
    boolean needed = state.needsSearch(id(1), 50, 100);

    // then
    assertTrue(needed);
  }

  @Test
  @DisplayName("freshly seen entity does not need search")
  void freshlySeenEntityIsCovered() {
    // given
    SearchState state = new SearchState();
    state.markSeen(id(1), 50);

    // when
    boolean needed = state.needsSearch(id(1), 60, 100);

    // then
    assertFalse(needed);
  }

  @Test
  @DisplayName("stale observation expires after maxAge ticks")
  void staleObservationExpires() {
    // given
    SearchState state = new SearchState();
    state.markSeen(id(1), 10);

    // when
    boolean needed = state.needsSearch(id(1), 111, 100);

    // then
    assertTrue(needed);
  }

  @Test
  @DisplayName("observation exactly maxAge old still counts as fresh")
  void boundaryAgeIsFresh() {
    // given
    SearchState state = new SearchState();
    state.markSeen(id(1), 10);

    // when
    boolean needed = state.needsSearch(id(1), 110, 100);

    // then
    assertFalse(needed);
  }

  @Test
  @DisplayName("markSeen keeps the most recent tick on re-observation")
  void markSeenKeepsMostRecent() {
    // given
    SearchState state = new SearchState();
    state.markSeen(id(1), 30);

    // when
    state.markSeen(id(1), 20);

    // then
    assertEquals(30, state.lastSeenTick(id(1)).getAsInt());
  }

  @Test
  @DisplayName("markSeen overwrites with newer tick")
  void markSeenOverwritesWithNewer() {
    // given
    SearchState state = new SearchState();
    state.markSeen(id(1), 20);

    // when
    state.markSeen(id(1), 40);

    // then
    assertEquals(40, state.lastSeenTick(id(1)).getAsInt());
  }

  @Test
  @DisplayName("merge keeps teammate's newer observations")
  void mergeKeepsNewerTeammateObservations() {
    // given
    SearchState state = new SearchState();
    state.markSeen(id(1), 10);
    state.markSeen(id(2), 30);

    // when
    state.merge(Map.of(id(1), 25, id(2), 5, id(3), 15));

    // then
    assertEquals(25, state.lastSeenTick(id(1)).getAsInt());
    assertEquals(30, state.lastSeenTick(id(2)).getAsInt());
    assertEquals(15, state.lastSeenTick(id(3)).getAsInt());
  }

  @Test
  @DisplayName("unsearched filters to entities needing search")
  void unsearchedFiltersCorrectly() {
    // given
    SearchState state = new SearchState();
    state.markSeen(id(1), 50);   // fresh
    state.markSeen(id(2), 1);    // stale
    // id(3) never seen

    // when
    var unsearched = state.unsearched(List.of(id(1), id(2), id(3)), 50, 10);

    // then
    assertEquals(2, unsearched.size());
    assertTrue(unsearched.contains(id(2)));
    assertTrue(unsearched.contains(id(3)));
  }

  @Test
  @DisplayName("snapshot is a defensive copy")
  void snapshotIsDefensive() {
    // given
    SearchState state = new SearchState();
    state.markSeen(id(1), 5);

    // when
    Map<EntityID, Integer> snap = state.snapshot();
    snap.put(id(2), 99);

    // then
    assertFalse(state.isSeen(id(2), 99));
  }

  @Test
  @DisplayName("isSeen is true only at the observed tick")
  void isSeenOnlyAtObservedTick() {
    // given
    SearchState state = new SearchState();
    state.markSeen(id(1), 42);

    // when / then
    assertTrue(state.isSeen(id(1), 42));
    assertFalse(state.isSeen(id(1), 43));
  }
}
