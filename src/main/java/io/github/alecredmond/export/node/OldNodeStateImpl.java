package io.github.alecredmond.export.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.io.Serializable;

@SuppressWarnings({"LombokGetterMayBeUsed"})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OldNodeStateImpl implements NodeState {
    /** The unique identifier for this state. */
    @EqualsAndHashCode.Include private final Serializable id;

    /** The parent {@link Node} that exhibits this state. */
    private final Node node;

    private String name;

    private int position;

    /**
     * Constructs a new {@code NodeState} with a unique identifier and its associated {@link Node}.
     *
     * @param id the {@link Serializable} unique identifier for this state
     * @param node the {@link Node} that this state belongs to
     * @param <T> the type of the state identifier
     * @throws NullPointerException if either the id or the node is {@code null}
     */
    public <T extends Serializable> OldNodeStateImpl(@NonNull T id, @NonNull Node node) {
        this.id = id;
        this.node = node;
    }

    /**
     * Returns a string representation of this state, which corresponds to its identifier.
     *
     * @return the string representation of the state identifier
     */
    @Override
    public String toString() {
        return id.toString();
    }

    /**
     * Returns the unique identifier for this {@code NodeState}.
     *
     * @return the {@link Serializable} identifier of this state
     */
    public Serializable getId() {
        return this.id;
    }

    /**
     * Returns the parent {@link Node} associated with this state.
     *
     * @return the {@link Node} instance that exhibits this state
     */
    public Node getNode() {
        return this.node;
    }

    /**
     * Returns the index of this {@code NodeState} in its parent {@link Node}'s state list.
     *
     * @return the position of this {@code NodeState}
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Package-private setter for the index position of this {@code NodeState} in its parent {@link
     * Node}'s state list.
     *
     * @param position the index of this state.
     */
    void setPosition(int position) {
        this.position = position;
    }
}
