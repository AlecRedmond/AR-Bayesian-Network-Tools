package io.github.alecredmond.internal.serialization;

import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.scenarios.NetworkScenario;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BayesianNetworkSerializerTest {
  static List<Supplier<BayesianNetwork>> networkSuppliers;
  BayesianNetworkSerializer test = new BayesianNetworkSerializer();

  static Stream<Arguments> provideNetworks() {
    return networkSuppliers.stream()
        .flatMap(BayesianNetworkSerializerTest::solvedAndUnsolved)
        .map(Arguments::of);
  }

  private static Stream<BayesianNetwork> solvedAndUnsolved(
      Supplier<BayesianNetwork> bayesianNetworkSupplier) {
    return Stream.of(bayesianNetworkSupplier.get(), bayesianNetworkSupplier.get().solveNetwork());
  }

  @BeforeAll
  static void init() {
    networkSuppliers =
        Arrays.stream(NetworkScenario.values())
            .map(NetworkScenario::getSupplier)
            .toList();
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testSerializes_shouldNotThrow(BayesianNetwork network) {
    assertDoesNotThrow(
        () -> {
          test.serialize(network);
        });
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testDeSerializes_shouldNotThrow(BayesianNetwork network) {
    assertDoesNotThrow(
        () -> {
          SerializedBayesianNetwork sto = test.serialize(network);
          test.deSerialize(sto);
        });
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void testNetworksAreEqual_shouldReturnTrue(BayesianNetwork network) {
    BayesianNetworkData before = network.getNetworkData();
    BayesianNetworkData after = test.deSerialize(test.serialize(network)).getNetworkData();
    assertFalse(before.getConstraints().isEmpty());
    assertFalse(after.getConstraints().isEmpty());
    assertEquals(before, after);
  }
}
