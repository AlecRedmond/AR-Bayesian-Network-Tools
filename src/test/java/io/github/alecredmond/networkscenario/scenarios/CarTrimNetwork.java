package io.github.alecredmond.networkscenario.scenarios;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.networkscenario.templates.AbstractNetworkScenario;
import java.util.List;

public class CarTrimNetwork extends AbstractNetworkScenario {
  //          TRIM LEVEL---------
  //         /     |    \        \
  // ENGINE TYPE   |    INTERIOR  COLOUR
  //    | |      / |     |
  //    | GEARBOX  |     |
  //     \  \      |    /
  //      ----  PRICE
  public final Node trim;
  public final Node engine;
  public final Node interior;
  public final Node colour;
  public final Node gearbox;
  public final Node price;

  public CarTrimNetwork() {
    trim = defaultNode("TRIM", List.of("STANDARD", "SPORT", "GT", "GTR", "HOMOLOGATION"));
    engine = defaultNode("ENGINE", List.of("1L ECO", "1.6 TURBO", "2.0 DIESEL", "3.0 V6"), trim);
    interior = defaultNode("INTERIOR", List.of("BASIC", "MID", "LUX", "LIGHT"), trim);
    colour = defaultNode("COLOUR", List.of("WHITE", "BLACK", "SILVER", "RED", "BLUE"), trim);
    gearbox =
        defaultNode(
            "GEARBOX", List.of("5x MANUAL", "6x MANUAL", "3x AUTO", "6x DCT"), engine, trim);
    price =
        defaultNode(
            "PRICE", List.of("£20k", "£40k", "£80k", "£250k"), engine, gearbox, interior, trim);

    // CONSTRAINTS
    addConstraint(
        List.of(
            state(engine, "3.0 V6"),
            state(gearbox, "6x DCT"),
            state(interior, "LIGHT"),
            state(price, "£250k")),
        List.of(state(trim, "HOMOLOGATION")),
        1.0);
    addConstraint(
        List.of(
            state(engine, "3.0 V6"),
            state(gearbox, "6x MANUAL"),
            state(interior, "LUX"),
            state(price, "£80k")),
        List.of(state(trim, "GTR")),
        1.0);
    addConstraint(state(engine, "1L ECO"), List.of(state(price, "£20k")), 1.0);
    addConstraint(state(interior, "BASIC"), List.of(state(price, "£20k")), 1.0);
    addConstraint(
        List.of(state(gearbox, "5x MANUAL"), state(gearbox, "3x AUTO")),
        List.of(state(price, "£20k"), state(engine, "1L ECO")),
        1.0);
    addConstraint(state(trim, "STANDARD"), List.of(state(price, "£20k")), 1.0);
    addConstraint(
        List.of(state(gearbox, "6x MANUAL"), state(gearbox, "3x AUTO")),
        List.of(state(trim, "GT")),
        1.0);
    addConstraint(
        List.of(state(gearbox, "6x MANUAL"), state(gearbox, "3x AUTO"), state(gearbox, "6x DCT")),
        List.of(state(engine, "2.0 DIESEL"), state(engine, "3.0 V6")),
        1.0);
    addConstraint(
        List.of(
            state(gearbox, "6x MANUAL"), state(gearbox, "5x MANUAL"), state(engine, "1.6 TURBO")),
        List.of(state(trim, "SPORT")),
        1.0);
    addConstraint(state(price, "£40k"), List.of(state(trim, "SPORT")), 1.0);
    addConstraint(state(price, "£80k"), List.of(state(trim, "GT")), 0.6);
    addConstraint(
        List.of(state(price, "£20k"), state(price, "£40k")), List.of(state(engine, "3.0 V6")), 0.0);
    addConstraint(state(colour, "BLACK"), List.of(state(trim, "GTR")), 0.5);
    addConstraint(state(colour, "BLUE"), List.of(state(trim, "HOMOLOGATION")), 0.85);
    addConstraint(state(colour, "BLUE"), List.of(state(trim, "SPORT")), 0.65);
    addConstraint(state(trim, "HOMOLOGATION"), List.of(state(price, "£250k")), 1.0);

    // MARGINALS
    // PRICE
    addConstraint(state(price, "£80k"), 0.075);
    addConstraint(state(price, "£250k"), 1e-5);
    // ENGINES
    addConstraint(state(engine, "1L ECO"), 0.65);
    addConstraint(state(engine, "1.6 TURBO"), 0.1);
    addConstraint(state(engine, "2.0 DIESEL"), 0.2);
    // COLOUR
    addConstraint(state(colour, "BLACK"), 0.25);
    addConstraint(state(colour, "BLUE"), 0.35);
    addConstraint(state(colour, "WHITE"), 0.1);
  }

  @Override
  protected String getNetworkName() {
    return "CAR MODEL VARIANTS";
  }

  @Override
  protected Node[] definedNodes() {
    return new Node[] {trim, engine, interior, colour, gearbox, price};
  }
}
