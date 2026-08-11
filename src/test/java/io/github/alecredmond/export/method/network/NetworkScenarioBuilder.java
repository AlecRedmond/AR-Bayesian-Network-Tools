package io.github.alecredmond.export.method.network;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilder;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkScenarioBuilder {

  public static Supplier<BayesianNetwork> buildSimpleLinearGraph() {
    return () ->
        BayesianNetwork.newNetwork("SIMPLE_LINEAR_GRAPH")
            .addNewNode("A", List.of("A+", "A-"))
            .addNewNode("B", List.of("B+", "B-"))
            .addNewNode("C", List.of("C+", "C-"))
            .addNewNode("D", List.of("D+", "D-"))
            .addParents("B", "A")
            .addParents("C", "B")
            .addParents("D", "C")
            .addConstraint("A+", 0.4)
            .addConstraint("B+", List.of("A+"), 0.8)
            .addConstraint("B+", List.of("A-"), 0.5)
            .addConstraint("C+", List.of("B+"), 0.9)
            .addConstraint("C+", List.of("B-"), 0.25)
            .addConstraint("D+", List.of("C+"), 0.6)
            .addConstraint("D+", List.of("C-"), 0.3);
  }

  public static Supplier<BayesianNetwork> buildFantasyGraph() {
    return () ->
        BayesianNetwork.newNetwork("FANTASY_ELECTION")
            .addNewNode(
                "DISTRICT_TYPE",
                List.of(
                    "DISTRICT_TYPE:URBAN",
                    "DISTRICT_TYPE:SUBURBAN",
                    "DISTRICT_TYPE:RURAL",
                    "DISTRICT_TYPE:FRONTIER"))
            .addNewNode(
                "DISTRICT",
                List.of(
                    "DISTRICT:CAPITAL_CITY",
                    "DISTRICT:CITY_SUBURBS",
                    "DISTRICT:FARM_TOWN",
                    "DISTRICT:MINING_OUTPOST",
                    "DISTRICT:OTHER"))
            .addNewNode(
                "RACE",
                List.of(
                    "RACE:HUMAN", "RACE:ANK", "RACE:ORC", "RACE:GOBLIN", "RACE:DWARF", "RACE:ELF"))
            .addNewNode(
                "AGE", List.of("AGE:CHILD", "AGE:YOUNG_ADULT", "AGE:MIDDLE_AGE", "AGE:ELDERLY"))
            .addNewNode(
                "WEALTH",
                List.of(
                    "WEALTH:MARGINAL",
                    "WEALTH:LOW",
                    "WEALTH:MIDDLE",
                    "WEALTH:HIGH",
                    "WEALTH:ULTRA"))
            .addNewNode(
                "OUTLOOK",
                List.of(
                    "OUTLOOK:REVOLUTIONARY",
                    "OUTLOOK:PROGRESSIVE",
                    "OUTLOOK:MODERATE",
                    "OUTLOOK:CONSERVATIVE",
                    "OUTLOOK:REACTIONARY",
                    "OUTLOOK:APATHY"))
            .addNewNode(
                "VOTE",
                List.of(
                    "VOTE:SDP",
                    "VOTE:VNG",
                    "VOTE:CPK",
                    "VOTE:UNF",
                    "VOTE:SND",
                    "VOTE:FPK",
                    "VOTE:KNC",
                    "VOTE:CSD",
                    "VOTE:OTH",
                    "VOTE:NONE"))
            .addParents("DISTRICT_TYPE", List.of())
            .addParents("DISTRICT", List.of("DISTRICT_TYPE"))
            .addParents("RACE", List.of("DISTRICT"))
            .addParents("AGE", List.of("RACE", "DISTRICT"))
            .addParents("WEALTH", List.of("RACE", "DISTRICT"))
            .addParents("OUTLOOK", List.of("WEALTH", "AGE", "DISTRICT"))
            .addParents("VOTE", List.of("RACE", "AGE", "DISTRICT", "OUTLOOK"))
            // Known marginals
            // DISTRICT
            .addConstraint("DISTRICT:CAPITAL_CITY", 600E3 / 320E6)
            .addConstraint("DISTRICT:CITY_SUBURBS", 600E3 / 320E6)
            .addConstraint("DISTRICT:FARM_TOWN", 600E3 / 320E6)
            .addConstraint("DISTRICT:MINING_OUTPOST", 600E3 / 320E6)
            // RACE
            .addConstraint("RACE:HUMAN", 0.65)
            .addConstraint("RACE:ANK", 0.14)
            .addConstraint("RACE:ORC", 0.07)
            .addConstraint("RACE:GOBLIN", 0.06)
            .addConstraint("RACE:DWARF", 0.05)
            .addConstraint("RACE:ELF", 0.03)
            // VOTE
            .addConstraint("VOTE:SDP", 0.1090)
            .addConstraint("VOTE:VNG", 0.0941)
            .addConstraint("VOTE:CPK", 0.0834)
            .addConstraint("VOTE:UNF", 0.0685)
            .addConstraint("VOTE:SND", 0.0214)
            .addConstraint("VOTE:FPK", 0.0173)
            .addConstraint("VOTE:KNC", 0.0066)
            .addConstraint("VOTE:CSD", 0.0057)
            .addConstraint("VOTE:OTH", 0.0066)
            // WEALTH
            .addConstraint("WEALTH:MARGINAL", 0.287)
            .addConstraint("WEALTH:LOW", 0.325)
            .addConstraint("WEALTH:HIGH", 0.12)
            .addConstraint("WEALTH:ULTRA", 0.001)
            // AGE
            .addConstraint("AGE:CHILD", 0.388)
            .addConstraint("AGE:YOUNG_ADULT", 0.318)
            .addConstraint("AGE:MIDDLE_AGE", 0.209)
            .addConstraint("AGE:ELDERLY", 0.085)
            // DISTRICT_TYPE
            .addConstraint("DISTRICT_TYPE:URBAN", 0.35)
            .addConstraint("DISTRICT_TYPE:SUBURBAN", 0.25)
            .addConstraint("DISTRICT_TYPE:RURAL", 0.30)
            .addConstraint("DISTRICT_TYPE:FRONTIER", 0.10)
            // ---
            // CONDITIONALS
            // DISTRICT_TYPE | DISTRICT
            .addConstraint("DISTRICT_TYPE:URBAN", List.of("DISTRICT:CAPITAL_CITY"), 1.0)
            .addConstraint("DISTRICT_TYPE:SUBURBAN", List.of("DISTRICT:CITY_SUBURBS"), 1.0)
            .addConstraint("DISTRICT_TYPE:RURAL", List.of("DISTRICT:FARM_TOWN"), 1.0)
            .addConstraint("DISTRICT_TYPE:FRONTIER", List.of("DISTRICT:MINING_OUTPOST"), 1.0)
            // Age|Race
            .addConstraint("AGE:MIDDLE_AGE", List.of("RACE:ORC"), 0.177)
            .addConstraint("AGE:YOUNG_ADULT", List.of("RACE:ANK"), 0.335)
            .addConstraint("AGE:CHILD", List.of("RACE:DWARF"), 0.303)
            .addConstraint("AGE:CHILD", List.of("RACE:GOBLIN"), 0.410)
            .addConstraint("AGE:ELDERLY", List.of("RACE:ELF"), 0.13)
            .addConstraint("AGE:MIDDLE_AGE", List.of("RACE:ELF"), 0.23)
            // Race|DISTRICT_TYPE
            .addConstraint("RACE:HUMAN", List.of("DISTRICT_TYPE:URBAN"), 0.98 * 0.65)
            .addConstraint("RACE:ANK", List.of("DISTRICT_TYPE:SUBURBAN"), 0.78 * 0.14)
            .addConstraint("RACE:ANK", List.of("DISTRICT_TYPE:RURAL"), 0.92 * 0.14)
            .addConstraint("RACE:ORC", List.of("DISTRICT_TYPE:SUBURBAN"), 0.73 * 0.07)
            .addConstraint("RACE:GOBLIN", List.of("DISTRICT_TYPE:SUBURBAN"), 0.70 * 0.06)
            .addConstraint("RACE:GOBLIN", List.of("DISTRICT_TYPE:RURAL"), 0.92 * 0.06)
            .addConstraint("RACE:ELF", List.of("DISTRICT_TYPE:URBAN"), 0.98 * 0.03)

            // Wealth|Race,DISTRICT_TYPE
            .addConstraint("WEALTH:MARGINAL", List.of("RACE:HUMAN", "DISTRICT:CAPITAL_CITY"), 0.25)
            .addConstraint("WEALTH:MARGINAL", List.of("RACE:HUMAN", "DISTRICT_TYPE:RURAL"), 0.23)
            .addConstraint("WEALTH:MARGINAL", List.of("RACE:HUMAN", "DISTRICT_TYPE:SUBURBAN"), 0.10)
            .addConstraint("WEALTH:MARGINAL", List.of("RACE:HUMAN", "DISTRICT_TYPE:FRONTIER"), 0.29)
            .addConstraint("WEALTH:LOW", List.of("RACE:HUMAN", "DISTRICT_TYPE:URBAN"), 0.36)
            .addConstraint("WEALTH:HIGH", List.of("RACE:HUMAN", "DISTRICT_TYPE:URBAN"), 0.13)
            // Outlook|Race,Wealth,Age,DISTRICT_TYPE
            .addConstraint(
                "OUTLOOK:REACTIONARY",
                List.of("RACE:HUMAN", "WEALTH:MIDDLE", "AGE:YOUNG_ADULT", "DISTRICT_TYPE:SUBURBAN"),
                0.42)
            .addConstraint(
                "OUTLOOK:REVOLUTIONARY",
                List.of("RACE:ANK", "WEALTH:MARGINAL", "AGE:YOUNG_ADULT", "DISTRICT_TYPE:URBAN"),
                0.66)
            .addConstraint(
                "OUTLOOK:CONSERVATIVE",
                List.of("RACE:HUMAN", "WEALTH:LOW", "AGE:MIDDLE_AGE", "DISTRICT_TYPE:RURAL"),
                0.387)
            .addConstraint(
                "OUTLOOK:PROGRESSIVE",
                List.of("RACE:DWARF", "WEALTH:MIDDLE", "AGE:YOUNG_ADULT", "DISTRICT_TYPE:URBAN"),
                0.42)
            .addConstraint(
                "OUTLOOK:MODERATE",
                List.of("RACE:ELF", "WEALTH:MIDDLE", "AGE:MIDDLE_AGE", "DISTRICT_TYPE:SUBURBAN"),
                0.41)
            // NON-LOCAL CONDITIONALS
            // Never-will-votes
            .addConstraint("VOTE:CPK", List.of("OUTLOOK:CONSERVATIVE"), 0.01)
            .addConstraint("VOTE:CPK", List.of("OUTLOOK:REACTIONARY"), 0.01)
            .addConstraint("VOTE:UNF", List.of("OUTLOOK:PROGRESSIVE"), 0.01)
            .addConstraint("VOTE:UNF", List.of("OUTLOOK:REVOLUTIONARY"), 0.01)
            // VOTE | DISTRICT, OUTLOOK
            .addConstraint(
                "VOTE:CPK", List.of("OUTLOOK:REVOLUTIONARY", "DISTRICT:CAPITAL_CITY"), 0.8)
            .addConstraint("VOTE:CPK", List.of("OUTLOOK:REVOLUTIONARY", "DISTRICT:FARM_TOWN"), 0.62)
            .addConstraint(
                "VOTE:FPK", List.of("OUTLOOK:CONSERVATIVE", "DISTRICT:CAPITAL_CITY"), 0.25)
            .addConstraint("VOTE:FPK", List.of("OUTLOOK:CONSERVATIVE", "DISTRICT:FARM_TOWN"), 0.05)
            .addConstraint(
                "VOTE:VNG", List.of("OUTLOOK:CONSERVATIVE", "DISTRICT:CAPITAL_CITY"), 0.21)
            .addConstraint("VOTE:VNG", List.of("OUTLOOK:CONSERVATIVE", "DISTRICT:FARM_TOWN"), 0.45)
            .addConstraint(
                "VOTE:SDP", List.of("OUTLOOK:PROGRESSIVE", "DISTRICT:CAPITAL_CITY"), 0.51)
            .addConstraint("VOTE:SDP", List.of("OUTLOOK:PROGRESSIVE", "DISTRICT:FARM_TOWN"), 0.28)
            // DISTRICT
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("DISTRICT:CITY_SUBURBS"), 0.05)
            .addConstraint("OUTLOOK:REACTIONARY", List.of("DISTRICT:CITY_SUBURBS"), 0.258)
            .addConstraint("AGE:CHILD", List.of("DISTRICT:CITY_SUBURBS"), 0.345)
            .addConstraint("OUTLOOK:REACTIONARY", List.of("DISTRICT:CAPITAL_CITY"), 0.1)
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("DISTRICT:CAPITAL_CITY"), 0.275)
            .addConstraint("OUTLOOK:CONSERVATIVE", List.of("DISTRICT:FARM_TOWN"), 0.300)
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("DISTRICT:FARM_TOWN"), 0.08)
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("DISTRICT:MINING_OUTPOST"), 0.15)
            // OTHERS
            .addConstraint("VOTE:NONE", List.of("AGE:CHILD"), 1.0)
            .addConstraint("VOTE:NONE", List.of("OUTLOOK:APATHY"), 1.0)
            .addConstraint("OUTLOOK:APATHY", List.of("VOTE:NONE"), 0.85)
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("WEALTH:MARGINAL"), 0.44)
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("WEALTH:ULTRA"), 0.02)
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("WEALTH:HIGH"), 0.05)
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("VOTE:CPK"), 0.65)
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("VOTE:SND"), 0.55)
            .addConstraint("OUTLOOK:REVOLUTIONARY", List.of("VOTE:SDP"), 0.20)
            .addConstraint("OUTLOOK:PROGRESSIVE", List.of("RACE:DWARF"), 0.46)
            .addConstraint("OUTLOOK:PROGRESSIVE", List.of("VOTE:SDP"), 0.48)
            .addConstraint("OUTLOOK:PROGRESSIVE", List.of("VOTE:CPK"), 0.20)
            .addConstraint("OUTLOOK:MODERATE", List.of("VOTE:VNG"), 0.20)
            .addConstraint("OUTLOOK:MODERATE", List.of("VOTE:FPK"), 0.41)
            .addConstraint("OUTLOOK:MODERATE", List.of("VOTE:SDP"), 0.25)
            .addConstraint("OUTLOOK:CONSERVATIVE", List.of("WEALTH:HIGH"), 0.48)
            .addConstraint("OUTLOOK:CONSERVATIVE", List.of("VOTE:FPK"), 0.55)
            .addConstraint("OUTLOOK:CONSERVATIVE", List.of("VOTE:VNG"), 0.42)
            .addConstraint("OUTLOOK:CONSERVATIVE", List.of("VOTE:UNF"), 0.35)
            .addConstraint("OUTLOOK:REACTIONARY", List.of("VOTE:UNF"), 0.50)
            .addConstraint("OUTLOOK:REACTIONARY", List.of("VOTE:CSD"), 0.44)
            .addConstraint("VOTE:UNF", List.of("RACE:ANK"), 0.02)
            .addConstraint("VOTE:UNF", List.of("RACE:DWARF"), 0.02)
            .addConstraint("VOTE:UNF", List.of("RACE:GOBLIN"), 0.03)
            .addConstraint("VOTE:UNF", List.of("RACE:ORC"), 0.08)
            .addConstraint("RACE:ORC", List.of("VOTE:KNC"), 0.59)
            .addConstraint("WEALTH:ULTRA", List.of("RACE:ANK"), 0.0005)
            .addConstraint("WEALTH:HIGH", List.of("RACE:ANK"), 0.008)
            .addConstraint("WEALTH:MIDDLE", List.of("RACE:ANK"), 0.0726)
            .addConstraint("WEALTH:ULTRA", List.of("RACE:ORC"), 0.0005)
            .addConstraint("WEALTH:HIGH", List.of("RACE:ORC"), 0.008)
            .addConstraint("WEALTH:MIDDLE", List.of("RACE:ORC"), 0.0726)
            .addConstraint("WEALTH:ULTRA", List.of("RACE:GOBLIN"), 0.0005)
            .addConstraint("WEALTH:HIGH", List.of("RACE:GOBLIN"), 0.008)
            .addConstraint("WEALTH:MIDDLE", List.of("RACE:GOBLIN"), 0.0726)
            .addConstraint("VOTE:CPK", List.of("RACE:ANK", "AGE:YOUNG_ADULT"), 0.22);
  }

  public static Supplier<BayesianNetwork> buildAhNetwork() {
    // A   B   C
    //  \ / \ /
    //   D   E
    //  / \ / \
    // F   G   H
    return () ->
        BayesianNetwork.newNetwork("A_TO_H")
            .addNewNode("A", List.of("A+", "A-"))
            .addNewNode("B", List.of("B+", "B-"))
            .addNewNode("C", List.of("C+", "C-"))
            .addNewNode("D", List.of("D+", "D-"))
            .addNewNode("E", List.of("E+", "E-"))
            .addNewNode("F", List.of("F+", "F-"))
            .addNewNode("G", List.of("G+", "G-"))
            .addNewNode("H", List.of("H+", "H-"))
            .addParents("D", List.of("A", "B"))
            .addParents("E", List.of("B", "C"))
            .addParents("F", List.of("D"))
            .addParents("G", List.of("D", "E"))
            .addParents("H", List.of("E"))
            // A
            .addConstraint("A+", 0.80)
            // B
            .addConstraint("B+", 0.40)
            // C
            .addConstraint("C-", 0.55)
            // D
            .addConstraint("D+", List.of("A+", "B+"), 0.90)
            .addConstraint("D+", List.of("A+", "B-"), 0.00)
            .addConstraint("D+", List.of("A-", "B+"), 0.20)
            .addConstraint("D+", List.of("A-", "B-"), 0.00)
            // E
            .addConstraint("E+", List.of("B+", "C+"), 0.85)
            .addConstraint("E+", List.of("B+", "C-"), 0.05)
            .addConstraint("E+", List.of("B-", "C+"), 0.00)
            .addConstraint("E+", List.of("B-", "C-"), 0.00)
            // F
            .addConstraint("F+", List.of("D+"), 0.95)
            .addConstraint("F+", List.of("D-"), 0.30)
            // G
            .addConstraint("G+", List.of("D+", "E+"), 0.15)
            .addConstraint("G+", List.of("D+", "E-"), 0.40)
            .addConstraint("G+", List.of("D-", "E+"), 0.75)
            .addConstraint("G+", List.of("D-", "E-"), 0.60)
            // H
            .addConstraint("H+", List.of("E+"), 0.95)
            .addConstraint("H+", List.of("E-"), 0.55);
  }

  public static Supplier<BayesianNetwork> buildRainNetwork() {
    return () ->
        new BayesianNetworkBuilder("RAIN_SPRINKLER_GRASS")
            .addNode("RAIN", List.of("RAIN:TRUE", "RAIN:FALSE"), new double[] {0.2, 0.8})
            .addNode(
                "SPRINKLER",
                List.of("SPRINKLER:TRUE", "SPRINKLER:FALSE"),
                List.of("RAIN", "SPRINKLER"),
                new double[] {0.01, 0.99, 0.4, 0.6})
            .addNode(
                "WET_GRASS",
                List.of("WET_GRASS:TRUE", "WET_GRASS:FALSE"),
                List.of("RAIN", "SPRINKLER", "WET_GRASS"),
                new double[] {0.99, 0.01, 0.9, 0.1, 0.9, 0.1, 0.0, 1.0})
            .build();
  }

  public static Supplier<BayesianNetwork> buildDiamondNetwork() {
    //   A
    //  / \
    // B   C
    //  \ /
    //   D
    return () ->
        BayesianNetwork.newNetwork("DIAMOND NETWORK")
            .addNewNode("A", List.of("A+", "A-", "Ax"))
            .addNewNode("B", List.of("B+", "B-", "Bx"))
            .addNewNode("C", List.of("C+", "C-", "Cx"))
            .addNewNode("D", List.of("D++", "D+-", "D--", "Dx"))
            .addParents("B", List.of("A"))
            .addParents("C", List.of("A"))
            .addParents("D", List.of("B", "C"))
            .addConstraint("A-", 0.05)
            .addConstraint("Cx", List.of("Ax"), 0.7)
            .addConstraint("C+", List.of("Ax"), 0.025)
            .addConstraint("C+", List.of("A+"), 0.625)
            .addConstraint("C+", List.of("A-"), 0.68)
            .addConstraint("Bx", List.of("Ax"), 1.0)
            .addConstraint(List.of("B+", "B-"), List.of("A+"), 1.0)
            .addConstraint("C+", List.of("D++"), 1.0)
            .addConstraint(List.of("D++", "D+-"), List.of(), 0.325);
  }

  public static Supplier<BayesianNetwork> buildWeatherNetwork() {
    // CLOUD_COVER
    //      |
    // PRECIPITATION
    return () ->
        BayesianNetwork.newNetwork("WEATHER_AND_PRECIP")
            .addNewNode("CLOUD_COVER", List.of("CLOUD:CLEAR", "CLOUD:LIGHT", "CLOUD:HEAVY"))
            .addNewNode("PRECIPITATION", List.of("PRECIP:NONE", "PRECIP:RAIN", "PRECIP:SNOW"))
            .addParents("PRECIPITATION", List.of("CLOUD_COVER"))
            .addConstraint("CLOUD:HEAVY", 0.30)
            .addConstraint(List.of("CLOUD:CLEAR", "CLOUD:LIGHT"), List.of(), 0.70)
            .addConstraint("PRECIP:NONE", List.of("CLOUD:CLEAR"), 1.0)
            .addConstraint(List.of("CLOUD:LIGHT", "PRECIP:RAIN"), List.of(), 0.15)
            .addConstraint(List.of("PRECIP:RAIN", "PRECIP:SNOW"), List.of("CLOUD:HEAVY"), 0.90);
  }

  public static Supplier<BayesianNetwork> buildCarTrimNetwork() {
    //          TRIM LEVEL---------
    //         /     |    \        \
    // ENGINE TYPE   |    INTERIOR  COLOUR
    //    | |      / |     |
    //    | GEARBOX  |     |
    //     \  \      |    /
    //      ----  PRICE
    return () ->
        BayesianNetwork.newNetwork("CAR MODEL VARIANTS")
            .addNewNode("TRIM", List.of("STANDARD", "SPORT", "GT", "GTR", "HOMOLOGATION"))
            .addNewNode("ENGINE", List.of("1L ECO", "1.6 TURBO", "2.0 DIESEL", "3.0 V6"))
            .addNewNode("INTERIOR", List.of("BASIC", "MID", "LUX", "LIGHT"))
            .addNewNode("COLOUR", List.of("WHITE", "BLACK", "SILVER", "RED", "BLUE"))
            .addNewNode("GEARBOX", List.of("5x MANUAL", "6x MANUAL", "3x AUTO", "6x DCT"))
            .addNewNode("PRICE", List.of("£20k", "£40k", "£80k", "£250k"))
            // STRUCTURE
            .addParents("ENGINE", "TRIM")
            .addParents("INTERIOR", "TRIM")
            .addParents("COLOUR", "TRIM")
            .addParents("GEARBOX", List.of("ENGINE", "TRIM"))
            .addParents("PRICE", List.of("ENGINE", "GEARBOX", "INTERIOR", "TRIM"))
            // CONSTRAINTS
            .addConstraint(
                List.of("3.0 V6", "6x DCT", "LIGHT", "£250k"), List.of("HOMOLOGATION"), 1.0)
            .addConstraint(List.of("3.0 V6", "6x MANUAL", "LUX", "£80k"), List.of("GTR"), 1.0)
            .addConstraint("1L ECO", "£20k", 1.0)
            .addConstraint("BASIC", "£20k", 1.0)
            .addConstraint(List.of("5x MANUAL", "3x AUTO"), List.of("£20k", "1L ECO"), 1.0)
            .addConstraint("STANDARD", "£20k", 1.0)
            .addConstraint(List.of("6x MANUAL", "3x AUTO"), List.of("GT"), 1.0)
            .addConstraint(
                List.of("6x MANUAL", "3x AUTO", "6x DCT"), List.of("2.0 DIESEL", "3.0 V6"), 1.0)
            // .addConstraint(List.of("6x MANUAL", "3x AUTO", "6x DCT"), List.of("3.0 V6"), 1.0)
            .addConstraint(List.of("6x MANUAL", "5x MANUAL", "1.6 TURBO"), List.of("SPORT"), 1.0)
            .addConstraint("£40k", "SPORT", 1.0)
            .addConstraint("£80k", "GT", 0.6)
            .addConstraint(List.of("£20k", "£40k"), List.of("3.0 V6"), 0.0)
            .addConstraint("BLACK", "GTR", 0.5)
            .addConstraint("BLUE", "HOMOLOGATION", 0.85)
            .addConstraint("BLUE", "SPORT", 0.65)
            .addConstraint("HOMOLOGATION", "£250k", 1.0)
            // MARGINALS
            // PRICE
            .addConstraint("£80k", 0.075)
            .addConstraint("£250k", 1e-5)
            // ENGINES
            .addConstraint("1L ECO", 0.65)
            .addConstraint("1.6 TURBO", 0.1)
            .addConstraint("2.0 DIESEL", 0.2)
            // COLOUR
            .addConstraint("BLACK", 0.25)
            .addConstraint("BLUE", 0.35)
            .addConstraint("WHITE", 0.1);
  }

  /*
      V
     / \
    W   X
    |   |
    Y   |
     \ /
      Z
  */
  public static Supplier<BayesianNetwork> buildNetworkLopsided() {
    return () ->
        BayesianNetwork.newNetwork("LOPSIDED")
            .addNewNode("V", List.of("V+", "V-"))
            .addNewNode("W", List.of("W+", "W-"))
            .addNewNode("X", List.of("X+", "X-"))
            .addNewNode("Y", List.of("Y+", "Y-"))
            .addNewNode("Z", List.of("Z+", "Z-"))
            .addParents("W", "V")
            .addParents("X", "V")
            .addParents("Y", "W")
            .addParents("Z", List.of("X", "Y"))
            .addConstraint("V+", 0.7)
            .addConstraint("W+", "V+", 0.25)
            .addConstraint("W+", "V-", 0.60)
            .addConstraint("X+", "V+", 0.90)
            .addConstraint("X+", "V-", 0.40)
            .addConstraint("Y+", "W+", 0.33)
            .addConstraint("Y+", "W-", 0.33)
            .addConstraint("Z+", List.of("X+", "Y+"), 0.80)
            .addConstraint("Z+", List.of("X+", "Y-"), 0.60)
            .addConstraint("Z+", List.of("X-", "Y+"), 0.40)
            .addConstraint("Z+", List.of("X-", "Y-"), 0.20);
  }

  public static Supplier<BayesianNetwork> buildAsiaNetwork() {
    String a = "ASIA_VISIT";
    String s = "SMOKING";
    String t = "TUBERCULOSIS";
    String l = "LUNG_CANCER";
    String b = "BRONCHITIS";
    String e = "TUB_OR_LUNG_CANCER";
    String x = "POSITIVE_X-RAY";
    String d = "DYSPNOEA";
    Function<String, List<String>> statesOf = str -> List.of(str + ":TRUE", str + ":FALSE");
    return () ->
        new BayesianNetworkBuilder("ASIA VISIT")
                .addNode(a,statesOf.apply(a),                  new double[]{0.01, 0.99})
                .addNode(s,statesOf.apply(s),                  new double[]{0.50, 0.50})
                .addNode(t,statesOf.apply(t),List.of(a,t),     new double[]{0.05,0.95,0.01,1-0.01})
                .addNode(l,statesOf.apply(l),List.of(s,l),     new double[]{0.1,1-0.1,0.01,1-0.01})
                .addNode(b,statesOf.apply(b),List.of(s,b),     new double[]{0.6,-1,0.3,-1})
                .addNode(e,statesOf.apply(e),List.of(l,t,e),   new double[]{1.0,0.0,1.0,0.0,1.0,0.0,0.0,1.0})
                .addNode(x,statesOf.apply(x),List.of(e,x),     new double[]{0.98,0.02,0.05,0.95})
                .addNode(d,statesOf.apply(d),List.of(e,b,d),   new double[]{0.9,2,0.7,2,0.8,2,0.1,2})
                .build();
  }
}
