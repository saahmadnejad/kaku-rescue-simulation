package io.donbee.kaku.rescue.search;

import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.develop.DevelopData;
import adf.core.component.module.algorithm.PathPlanning;
import java.util.Collection;
import java.util.List;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

/**
 * Creates and caches the PathPlanning instance appropriate for the agent
 * type, keyed by module name, so search code does not repeat the sample's
 * if/else plumbing.
 */
public final class Router {

  private final PathPlanning pathPlanning;

  public Router(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
      ModuleManager moduleManager, DevelopData developData) {
    StandardEntityURN agentURN = agentInfo.me().getStandardURN();
    String defaultImpl = "adf.impl.module.algorithm.DijkstraPathPlanning";
    this.pathPlanning = moduleManager.getModule(
        "KakuSearch.PathPlanning." + agentURN.name(), defaultImpl);
  }

  /** Plans a path from the agent position to the nearest of the destinations. */
  public List<EntityID> route(EntityID from, Collection<EntityID> destinations) {
    pathPlanning.setFrom(from);
    pathPlanning.setDestination(destinations);
    return pathPlanning.calc().getResult();
  }

  /** Routing function view of this router, suitable for injection in tests. */
  public RoutingFunction asFunction() {
    return this::route;
  }

  /** Decouples selectors from the ADF router for unit testing. */
  @FunctionalInterface
  public interface RoutingFunction {
    List<EntityID> route(EntityID from, Collection<EntityID> destinations);
  }

  public PathPlanning getPathPlanning() {
    return pathPlanning;
  }
}
