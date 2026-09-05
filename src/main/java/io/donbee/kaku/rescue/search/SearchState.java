package io.donbee.kaku.rescue.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import rescuecore2.worldmodel.EntityID;

/**
 * Persistent search memory: last tick each building was observed.
 *
 * <p>Replaces the sample's leaky HashSet reset with a timestamped map, so
 * previously explored ground stays covered until entries grow stale, and
 * teammate observations can be merged (Phase 2 hook).
 */
public final class SearchState {

  private final Map<EntityID, Integer> lastSeen = new HashMap<>();

  /**
   * Marks the given entities as observed at the given tick, overwriting any
   * older observation.
   */
  public void markSeen(Collection<EntityID> entities, int tick) {
    for (EntityID id : entities) {
      lastSeen.merge(id, tick, Math::max);
    }
  }

  /** Marks a single entity as observed at the given tick. */
  public void markSeen(EntityID entity, int tick) {
    lastSeen.merge(entity, tick, Math::max);
  }

  /**
   * Merges a teammate's observation map; for each entity the most recent
   * observation wins.
   */
  public void merge(Map<EntityID, Integer> other) {
    for (Map.Entry<EntityID, Integer> entry : other.entrySet()) {
      lastSeen.merge(entry.getKey(), entry.getValue(), Math::max);
    }
  }

  /** Snapshot of the observation map (defensive copy). */
  public Map<EntityID, Integer> snapshot() {
    return new HashMap<>(lastSeen);
  }

  /** Last-seen tick for the entity, or empty if never observed. */
  public java.util.OptionalInt lastSeenTick(EntityID entity) {
    Integer tick = lastSeen.get(entity);
    return tick == null ? java.util.OptionalInt.empty() : java.util.OptionalInt.of(tick);
  }

  /** True when the entity was observed at the given tick. */
  public boolean isSeen(EntityID entity, int tick) {
    Integer seen = lastSeen.get(entity);
    return seen != null && seen == tick;
  }

  /**
   * True when the entity needs (re)search: never seen, or its last
   * observation is older than maxAge ticks.
   */
  public boolean needsSearch(EntityID entity, int now, int maxAge) {
    Integer seen = lastSeen.get(entity);
    return seen == null || now - seen > maxAge;
  }

  /** Filters the candidates down to those needing (re)search. */
  public Set<EntityID> unsearched(Collection<EntityID> candidates, int now, int maxAge) {
    return candidates.stream()
        .filter(id -> needsSearch(id, now, maxAge))
        .collect(Collectors.toCollection(HashSet::new));
  }
}
