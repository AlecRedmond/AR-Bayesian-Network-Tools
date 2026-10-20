package io.github.alecredmond.internal.method.inference;

import static io.github.alecredmond.internal.method.utils.AppProperty.INFERENCE_ALGORITHM;

import io.github.alecredmond.exceptions.PropertiesLoaderException;
import io.github.alecredmond.export.inference.InferenceAlgorithm;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.internal.method.junctiontree.JunctionTreeAlgorithm;
import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public class InferenceEngineFactory {

  public InferenceEngine create(BayesianNetwork network) {
    return create(network, getInferenceAlgorithm());
  }

  public InferenceEngine create(BayesianNetwork network, InferenceAlgorithm inferenceAlgorithm) {
    BayesSolver solver = BayesSolver.create(network);
    if (!attemptSolve(network, solver)) {
      log.error("Could not build an inference engine!");
      return null;
    }
    return new InferenceEngineImpl(
        network,
        solver,
        JunctionTreeAlgorithm.buildForInference(network.getNetworkData(), inferenceAlgorithm),
        inferenceAlgorithm);
  }

  private InferenceAlgorithm getInferenceAlgorithm() {
    String inferenceTypeString =
        new PropertiesLoader().loadString(INFERENCE_ALGORITHM).toUpperCase().replaceAll("\\s+", "");
    try {
      return InferenceAlgorithm.valueOf(inferenceTypeString);
    } catch (IllegalArgumentException e) {
      throw new PropertiesLoaderException(
          "'%s' is not a valid inference type! Valid options are [%s]"
              .formatted(
                  inferenceTypeString,
                  Arrays.stream(InferenceAlgorithm.values())
                      .map(Enum::toString)
                      .collect(Collectors.joining(" "))));
    }
  }

  private boolean attemptSolve(BayesianNetwork network, BayesSolver solver) {
    if (network.isSolved()) return true;
    log.warn(
        "Attempted to create an Inference Engine on unsolved network '{}'. Will now attempt to solve...",
        network.getNetworkData().getNetworkName());
    return solver.forceSolve();
  }
}
