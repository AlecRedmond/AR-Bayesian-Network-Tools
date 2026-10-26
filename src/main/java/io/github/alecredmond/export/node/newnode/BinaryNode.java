package io.github.alecredmond.export.node.newnode;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

public interface BinaryNode extends Node {
  default NodeState getState(boolean binaryState){
      return binaryState ? trueState() : falseState();
  }

  NodeState trueState();

  NodeState falseState();

  enum StateOrder {
    FALSE_FIRST,
    TRUE_FIRST
  }
}
