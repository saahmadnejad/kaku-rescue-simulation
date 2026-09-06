package io.donbee.kaku.rescue.search;

import static rescuecore2.standard.entities.StandardEntityURN.AMBULANCE_CENTRE;
import static rescuecore2.standard.entities.StandardEntityURN.BUILDING;
import static rescuecore2.standard.entities.StandardEntityURN.GAS_STATION;
import static rescuecore2.standard.entities.StandardEntityURN.FIRE_STATION;
import static rescuecore2.standard.entities.StandardEntityURN.POLICE_OFFICE;
import static rescuecore2.standard.entities.StandardEntityURN.REFUGE;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.communication.standard.bundle.information.MessageBuilding;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.module.algorithm.Clustering;
import adf.core.component.module.complex.Search;
import adf.core.debug.DefaultLogger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * Kaku search module: cluster-partitioned building search with persistent
 * observation memory.
 *
 * <p>Improvements over the sample:
 * <ul>
 *   <li>{@link SearchState} keeps last-seen ticks instead of dropping
 *       observations, so covered ground stays covered until stale.</li>
 *   <li>Target selection is delegated to a {@link SearchTargetSelector}
 *       (currently {@link NearestFirstSelector} for parity).</li>
 * </ul>
 */
public class KakuSearch extends Search {

  /** Re-search a building when its last observation is older than this. */
  static final int MAX_AGE_TICKS = 100;

  /** Share at most this many observations per broadcast (channel budget). */
  static final int MAX_BROADCAST_ENTRIES = 20;

  private final Clustering clustering;
  private final SearchTargetSelector selector;
  private final SearchState state = new SearchState();

  private final Logger logger;
  private EntityID result;

  /** Entities observed since the last broadcast, tick-stamped. */
  private final Map<EntityID, Integer> pendingShare = new HashMap<>();

  public KakuSearch(AgentInfo ai, WorldInfo wi, ScenarioInfo si,
      ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);
    this.logger = DefaultLogger.getLogger(agentInfo.me());

    Router router = new Router(ai, wi, si, moduleManager, developData);
    this.selector = new NearestFirstSelector(router.asFunction());
    this.clustering = moduleManager.getModule(
        "KakuSearch.Clustering", "adf.impl.module.algorithm.KMeansClustering");

    registerModule(this.clustering);
    registerModule(router.getPathPlanning());
  }

  @Override
  public Search updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    int now = agentInfo.getTime();

    ensureMessageRegistered(messageManager);

    // own observations -> state + pending broadcast
    for (EntityID changed : worldInfo.getChanged().getChangedEntities()) {
      state.markSeen(changed, now);
      pendingShare.put(changed, now);
    }

    // teammate observations -> state (no re-broadcast, avoids loops)
    for (CommunicationMessage raw : messageManager
        .getReceivedMessageList(KakuSearchMessage.class)) {
      KakuSearchMessage received = (KakuSearchMessage) raw;
      List<EntityID> ids = received.getBuildingIDs();
      List<Integer> ticks = received.getTicks();
      for (int i = 0; i < ids.size(); i++) {
        state.markSeen(ids.get(i), ticks.get(i));
      }
    }

    // broadcast our freshest pending observations
    if (!pendingShare.isEmpty()) {
      List<EntityID> shareIds = new ArrayList<>(pendingShare.keySet());
      List<Integer> shareTicks = new ArrayList<>();
      for (EntityID id : shareIds) {
        shareTicks.add(pendingShare.get(id));
      }
      int limit = Math.min(shareIds.size(), MAX_BROADCAST_ENTRIES);
      messageManager.addMessage(new KakuSearchMessage(
          shareIds.subList(0, limit), shareTicks.subList(0, limit)));
      for (int i = 0; i < limit; i++) {
        pendingShare.remove(shareIds.get(i));
      }
    }
    return this;
  }

  @Override
  public Search calc() {
    this.result = null;

    Set<EntityID> candidates = state.unsearched(candidateBuildings(),
        agentInfo.getTime(), MAX_AGE_TICKS);
    if (candidates.isEmpty()) {
      return this;
    }

    this.result = selector.select(candidates, agentInfo.getPosition());
    logger.debug("KakuSearch chose: " + result);
    return this;
  }

  /** Registers the search message class once; picks a free index <= 31. */
  private void ensureMessageRegistered(MessageManager messageManager) {
    Class<? extends CommunicationMessage> messageClass = KakuSearchMessage.class
        .asSubclass(CommunicationMessage.class);
    for (int index = 0; index <= 31; index++) {
      if (messageClass.equals(messageManager.getMessageClass(index))) {
        return; // already registered (by us or a teammate module instance)
      }
    }
    for (int index = 0; index <= 31; index++) {
      if (messageManager.getMessageClass(index) == null) {
        messageManager.registerMessageClass(index, messageClass);
        return;
      }
    }
    logger.warn("KakuSearchMessage: no free message index; comms disabled");
  }

  /** Buildings of the agent's cluster; falls back to all searchables. */
  private Collection<EntityID> candidateBuildings() {
    Set<EntityID> candidates = new HashSet<>();
    int clusterIndex = clustering.getClusterIndex(agentInfo.getID());
    Collection<StandardEntity> clusterEntities =
        clustering.getClusterEntities(clusterIndex);
    if (clusterEntities != null && !clusterEntities.isEmpty()) {
      for (StandardEntity entity : clusterEntities) {
        if (entity instanceof Building && entity.getStandardURN() != REFUGE) {
          candidates.add(entity.getID());
        }
      }
    }
    if (candidates.isEmpty()) {
      candidates.addAll(worldInfo.getEntityIDsOfType(
          BUILDING, GAS_STATION, AMBULANCE_CENTRE, FIRE_STATION, POLICE_OFFICE));
    }
    return candidates;
  }

  @Override
  public EntityID getTarget() {
    return this.result;
  }
}
