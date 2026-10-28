package io.github.alecredmond.networkscenario.templates;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.implementation.BinaryNode;
import io.github.alecredmond.export.node.implementation.DefaultBinaryNode;
import java.util.Arrays;

public interface BinaryNodeScenario {
  default BinaryNode binaryNode(String name, Node... parents) {
    return new DefaultBinaryNode(name, Arrays.asList(parents), getBinaryNodeOrder());
  }

  BinaryNode.StateOrder getBinaryNodeOrder();
}
