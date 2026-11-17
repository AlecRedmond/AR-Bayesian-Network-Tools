package io.github.alecredmond.export.probabilitytables.tableentry;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.util.List;
import java.util.Set;

/**
 * Contains every {@link CptEntry} evaluated under a single combined parent condition permutation
 * within a {@link NetworkTable}.
 *
 * @param conditionStates a specific combination of conditioning {@link NodeState}s applicable to
 *     the entire row.
 * @param rowEntries all CPT entries subject to the conditioning states.
 * @param rowStartIndex the index of the first row entry in the probability {@code double[]} array
 *     of the associated {@link NetworkTable}.
 * @see CptEntry
 * @author Alec Redmond
 */
public record CptRow(
    Set<NodeState> conditionStates, List<CptEntry> rowEntries, int rowStartIndex) {}
