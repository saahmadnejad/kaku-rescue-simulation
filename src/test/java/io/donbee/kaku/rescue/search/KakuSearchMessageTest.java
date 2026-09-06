package io.donbee.kaku.rescue.search;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import rescuecore2.worldmodel.EntityID;

/** Wire roundtrip tests for the explored-delta search message. */
class KakuSearchMessageTest {

  private KakuSearchMessage roundtrip(KakuSearchMessage original) {
    byte[] wire = original.toByteArray();
    adf.core.component.communication.util.BitStreamReader reader =
        new adf.core.component.communication.util.BitStreamReader(wire);
    return new KakuSearchMessage(true, original.getTTL(), reader);
  }

  @Test
  @DisplayName("message survives serialize/deserialize roundtrip")
  void roundtripPreservesObservations() {
    // given
    List<EntityID> ids = Arrays.asList(new EntityID(11), new EntityID(42), new EntityID(307));
    List<Integer> ticks = Arrays.asList(1, 22, 333);

    // when
    KakuSearchMessage decoded = roundtrip(new KakuSearchMessage(ids, ticks));

    // then
    assertEquals(ids, decoded.getBuildingIDs());
    assertEquals(ticks, decoded.getTicks());
  }

  @Test
  @DisplayName("empty delta roundtrips to empty lists")
  void emptyDeltaRoundtrips() {
    // given
    KakuSearchMessage message = new KakuSearchMessage(List.of(), List.of());

    // when
    KakuSearchMessage decoded = roundtrip(message);

    // then
    assertTrue(decoded.getBuildingIDs().isEmpty());
    assertTrue(decoded.getTicks().isEmpty());
  }

  @Test
  @DisplayName("mismatched ids/ticks sizes are rejected")
  void mismatchedSizesRejected() {
    // given
    List<EntityID> ids = List.of(new EntityID(1));
    List<Integer> ticks = List.of(1, 2);

    // when / then
    assertThrows(IllegalArgumentException.class,
        () -> new KakuSearchMessage(ids, ticks));
  }

  @Test
  @DisplayName("byte size matches wire format (header + 8 bytes per entry)")
  void byteArraySizeMatchesWireFormat() {
    // given
    List<EntityID> ids = Arrays.asList(new EntityID(1), new EntityID(2));
    List<Integer> ticks = Arrays.asList(10, 20);
    KakuSearchMessage message = new KakuSearchMessage(ids, ticks);

    // when
    int size = message.getByteArraySize();

    // then
    assertEquals(4 + 2 * 8, size);
  }
}
