package io.github.alecredmond.internal.serialization;

import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.networkscenario.NetworkScenario;
import io.github.alecredmond.networkscenario.NetworkScenarioType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BayesianNetworkSerializerTest {
  static List<NetworkScenario> scenarios;
  BayesianNetworkSerializer test = new BayesianNetworkSerializer();

  static Stream<Arguments> provideNetworks() {
    return scenarios.stream().map(NetworkScenario::buildNetwork).map(Arguments::of);
  }

  @BeforeAll
  static void init() {
    scenarios = Arrays.stream(NetworkScenarioType.values()).map(NetworkScenarioType::get).toList();
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testSerializes_shouldNotThrow(BayesianNetwork network) {
    assertDoesNotThrow(
        () -> {
          test.serializeNetwork(network);
        });
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testDeSerializes_shouldNotThrow(BayesianNetwork network) {
    assertDoesNotThrow(
        () -> {
          SerializedBayesianNetwork sto = test.serializeNetwork(network);
          test.deSerializeNetwork(sto);
        });
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testNetworksAreEqual_shouldReturnTrue(BayesianNetwork before) {
    BayesianNetwork after = test.deSerializeNetwork(test.serializeNetwork(before));
    assertFalse(before.getConstraints().isEmpty());
    assertFalse(after.getConstraints().isEmpty());
    assertEquals(before, after);
  }
}
