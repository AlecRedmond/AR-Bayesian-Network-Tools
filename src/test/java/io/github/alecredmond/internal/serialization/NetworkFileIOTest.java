package io.github.alecredmond.internal.serialization;

import static io.github.alecredmond.TestConfigs.*;
import static io.github.alecredmond.internal.method.utils.AppProperty.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import io.github.alecredmond.networkscenario.NetworkScenario;
import io.github.alecredmond.networkscenario.NetworkScenarioType;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class NetworkFileIOTest {
  static final PropertiesLoader LOADER = new PropertiesLoader();
  static final String DIRECTORY = LOADER.loadDirectory(DIRECTORY_ROOT, DIRECTORY_SAVE);
  static final String EXTENSION = LOADER.loadString(EXTENSION_FILE_TYPE);
  static List<BayesianNetwork> networks;
  NetworkFileIO test = new NetworkFileIO();

  static Stream<Arguments> provideNetworks() {
    return networks.stream().map(Arguments::of);
  }

  @BeforeAll
  static void init() {
    networks =
        Arrays.stream(NetworkScenarioType.values())
            .map(NetworkScenarioType::get)
            .map(NetworkScenario::buildNetwork)
            .toList();
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void saveNetwork(BayesianNetwork network) {
    if (!SAVE_BINARIES) {
      assertTrue(true);
      return;
    }
    String netName = network.getNetworkName();
    String totalPath = DIRECTORY + netName;
    SerializedBayesianNetwork sbn = network.serializeNetwork();
    assertTrue(test.saveNetwork(sbn, totalPath));
    assertTrue(test.saveNetwork(sbn, new File(totalPath)));
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void loadNetwork(BayesianNetwork network) {
    if (!SAVE_BINARIES) {
      assertTrue(true);
      return;
    }
    String netName = network.getNetworkName();
    String totalPath = DIRECTORY + netName + EXTENSION;
    SerializedBayesianNetwork sbn = test.loadNetwork(totalPath);
    BayesianNetwork loaded = BayesianNetwork.loadNetwork(sbn);
    assertEquals(network, loaded);
  }
}
