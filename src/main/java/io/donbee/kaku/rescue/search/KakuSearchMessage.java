package io.donbee.kaku.rescue.search;

import adf.core.agent.communication.standard.bundle.StandardMessage;
import adf.core.agent.communication.standard.bundle.StandardMessagePriority;
import adf.core.component.communication.util.BitOutputStream;
import adf.core.component.communication.util.BitStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import rescuecore2.worldmodel.EntityID;

/**
 * Radio message carrying a compact explored-buildings delta: pairs of
 * (building id, last-seen tick) observed by the sender.
 *
 * <p>Wire format after the StandardMessage header: [count][id tick]...
 * with ids and ticks as 32-bit values.
 */
public class KakuSearchMessage extends StandardMessage {

  private static final int ID_BITS = 32;
  private static final int TICK_BITS = 32;

  private final List<EntityID> buildingIDs;
  private final List<Integer> ticks;

  /** Builds an outgoing message from a slice of the observation map. */
  public KakuSearchMessage(Collection<EntityID> buildingIDs, List<Integer> ticks) {
    super(false, StandardMessagePriority.NORMAL);
    if (buildingIDs.size() != ticks.size()) {
      throw new IllegalArgumentException("ids and ticks must have equal size");
    }
    this.buildingIDs = new ArrayList<>(buildingIDs);
    this.ticks = new ArrayList<>(ticks);
  }

  /** Rebuilds an incoming message from the wire. */
  public KakuSearchMessage(boolean isRadio, int ttl, BitStreamReader reader) {
    super(isRadio, ttl, StandardMessagePriority.NORMAL.ordinal(), reader);
    int count = reader.getBits(ID_BITS);
    this.buildingIDs = new ArrayList<>(count);
    this.ticks = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      int rawId = reader.getBits(ID_BITS);
      int tick = reader.getBits(TICK_BITS);
      this.buildingIDs.add(new EntityID(rawId));
      this.ticks.add(tick);
    }
  }

  public List<EntityID> getBuildingIDs() {
    return buildingIDs;
  }

  public List<Integer> getTicks() {
    return ticks;
  }

  @Override
  public int getByteArraySize() {
    return Integer.BYTES + (buildingIDs.size() * (Integer.BYTES * 2));
  }

  @Override
  public byte[] toByteArray() {
    BitOutputStream stream = toBitOutputStream();
    return stream.toByteArray();
  }

  @Override
  public BitOutputStream toBitOutputStream() {
    BitOutputStream stream = new BitOutputStream();
    stream.writeBits(buildingIDs.size(), ID_BITS);
    for (int i = 0; i < buildingIDs.size(); i++) {
      stream.writeBits(buildingIDs.get(i).getValue(), ID_BITS);
      stream.writeBits(ticks.get(i), TICK_BITS);
    }
    return stream;
  }

  @Override
  public String getCheckKey() {
    return "KakuSearchMessage";
  }
}
