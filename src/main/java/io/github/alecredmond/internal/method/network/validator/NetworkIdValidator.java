package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import lombok.Data;

@Data
public class NetworkIdValidator implements NetworkValidator {
  private static final String ID = "an identifier";
  private static final String NAME = "a name";

  @Override
  public void validateData(BayesianNetworkData networkData) {
    Collection<Node> nodes = networkData.getNodes();
    Set<Serializable> checkedIds = new HashSet<>();
    validateNoNullsOrDupes(nodes, Node::getId, checkedIds, ID);
    validateNoNullsOrDupes(NodeUtils.getAllStates(nodes), NodeState::getId, checkedIds, ID);
    Set<String> checkedNames = new HashSet<>();
    validateNoNullsOrDupes(nodes, Node::getName, checkedNames, NAME);
  }

  private <T, U> void validateNoNullsOrDupes(
      Collection<T> input, Function<T, U> checkGet, Set<U> checked, String typeChecked) {
    for (T t : input) {
      U u = checkGet.apply(t);
      if (u == null) {
        throwNullValueException(t, typeChecked);
      }
      if (!checked.add(u)) {
        throwDuplicateException(t, u, typeChecked);
      }
    }
  }

  private <T> void throwNullValueException(T t, String typeChecked) {
    throw new BayesNetIDException("Input %s contained %s that was null".formatted(t, typeChecked));
  }

  private <T, U> void throwDuplicateException(T t, U u, String typeChecked) {
    throw new BayesNetIDException("Duplicated %s found in %s : %s".formatted(typeChecked, t, u));
  }
}
