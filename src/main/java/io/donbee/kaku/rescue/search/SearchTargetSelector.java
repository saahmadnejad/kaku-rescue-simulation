package io.donbee.kaku.rescue.search;

import java.util.Collection;
import java.util.List;
import rescuecore2.worldmodel.EntityID;

/** Chooses the next search target from unsearched candidates. */
public interface SearchTargetSelector {

  /**
   * Selects the next search target, or null when nothing is reachable.
   *
   * @param candidates    entity IDs needing (re)search
   * @param currentPosition agent position
   * @return chosen target EntityID, or null
   */
  EntityID select(Collection<EntityID> candidates, EntityID currentPosition);
}
