package io.donbee.kaku.rescue.search;

import java.util.Collection;
import java.util.List;
import rescuecore2.worldmodel.EntityID;

/**
 * Baseline selector: nearest unsearched candidate via the injected router.
 * Behaviour parity with the sample's Dijkstra-nearest approach; exists to be
 * replaced by smarter selectors in later phases.
 */
public final class NearestFirstSelector implements SearchTargetSelector {

  private final Router.RoutingFunction routing;

  public NearestFirstSelector(Router.RoutingFunction routing) {
    this.routing = routing;
  }

  @Override
  public EntityID select(Collection<EntityID> candidates, EntityID currentPosition) {
    if (candidates == null || candidates.isEmpty()) {
      return null;
    }
    List<EntityID> path = routing.route(currentPosition, candidates);
    return targetFromPath(path);
  }

  /**
   * Chooses the search target from a planned path: the node three steps
   * before the end when the path is long, otherwise the last reachable node.
   * Mirrors the sample's observation-at-distance behaviour.
   */
  static EntityID targetFromPath(List<EntityID> path) {
    if (path == null || path.isEmpty()) {
      return null;
    }
    if (path.size() > 2) {
      return path.get(path.size() - 3);
    }
    return path.get(path.size() - 1);
  }
}
