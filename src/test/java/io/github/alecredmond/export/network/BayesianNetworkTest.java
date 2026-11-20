package io.github.alecredmond.export.network;

import static io.github.alecredmond.TestConfigs.PRINT_TABLES;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.networkscenario.NetworkScenario;
import io.github.alecredmond.networkscenario.NetworkScenarioType;
import io.github.alecredmond.networkscenario.scenarios.RainNetwork;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BayesianNetworkTest {

  @Nested
  class PrinterTests {
    @Test
    void printAllNetworks() {
      if (!PRINT_TABLES) return;
      assertDoesNotThrow(
          () ->
              Arrays.stream(NetworkScenarioType.values())
                  .map(NetworkScenarioType::get)
                  .map(NetworkScenario::buildNetwork)
                  .forEach(BayesianNetwork::printNetwork));
    }
  }

  @Nested
  class RainNetworkTests {
    RainNetwork rainNetwork;
    BayesianNetwork net;
    InferenceEngine test;

    @BeforeEach
    void init() {
      rainNetwork = new RainNetwork();
      net = rainNetwork.buildNetwork();
      test = net.buildInferenceEngine();
    }

    @Test
    void getNetworkTable_shouldReturnTable() {
      ProbabilityTable rainTable = net.getNetworkTable(rainNetwork.rain);
      assertNotNull(rainTable);
      assertEquals(1, rainTable.getNodes().size());

      ProbabilityTable grassTable = net.getNetworkTable(rainNetwork.wetGrass);
      assertNotNull(grassTable);
      assertEquals(3, grassTable.getNodes().size());
    }

    @Test
    void getNetworkTable_nonExistentNode_shouldReturnNull() {
      assertNull(net.getNetworkTableByNodeId("CLOUDY"));
    }
  }
}
