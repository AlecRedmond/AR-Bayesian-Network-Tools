package io.github.alecredmond.internal.application.inference.indirect;

import io.github.alecredmond.export.node.NodeState;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;

@Data
public class SampleData {
  private final NodeState[] rawStateArray;
  private final Set<NodeState> rawStateSet;
  private NodeState[] exportStateArray;
  private int count;

  public SampleData(Set<NodeState> states) {
    this.rawStateArray = states.toArray(NodeState[]::new);
    this.rawStateSet = Collections.unmodifiableSet(states);
    this.exportStateArray = Arrays.copyOf(rawStateArray, rawStateArray.length);
    this.count = 0;
  }

  public SampleData(NodeState[] rawStateArray) {
    this.rawStateArray = rawStateArray;
    this.rawStateSet = new LinkedHashSet<>(Arrays.asList(rawStateArray));
    this.exportStateArray = Arrays.copyOf(rawStateArray, rawStateArray.length);
    this.count = 0;
  }
}
