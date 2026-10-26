package io.github.alecredmond.networkscenario.templates;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.newnode.BinaryNode;
import io.github.alecredmond.export.node.newnode.DefaultBinaryNode;
import java.util.Arrays;

public interface BinaryNodeScenario {
  default BinaryNode binaryNode(String name, Node... parents) {
    return new DefaultBinaryNode(name, Arrays.asList(parents), getBinaryNodeOrder());
  }

  BinaryNode.StateOrder getBinaryNodeOrder();
}
