package io.github.unterstein;

import org.apache.commons.io.FileUtils;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Export data from local data structure to neo4j
 */
public class NeoExporter {

  private static final Logger log = LoggerFactory.getLogger(TwitterImport.class);

  public static void main(String[] args) throws IOException {
    // do rudimentary input arg validation
    if (args.length != 3) {
      log.error("Please provide the following twitter api information in this order:");
      log.error("$neo4jUri $neo4jUser $neo4jPassword");
      System.exit(1);
    }
    String[] lines = FileUtils.readFileToString(new File("output.txt"), "UTF-8").split(System.lineSeparator());
    List<String> userNames = new ArrayList<>();
    List<TipEntry> entries = new ArrayList<>();

    // build internal data structure from input file
    for (String line : lines) {
      String[] split = line.split(":");
      if (!userNames.contains(split[0])) {
        userNames.add(split[0]);
      }
      if (!userNames.contains(split[1])) {
        userNames.add(split[1]);
      }
      entries.add(new TipEntry(userNames.indexOf(split[0]), userNames.indexOf(split[1]), Integer.valueOf(split[2])));
    }

    // setup neo4j driver
    Driver neo4jDriver = GraphDatabase.driver(args[0], AuthTokens.basic(args[1], args[2]));
    Session session = neo4jDriver.session();

    // delete previous nodes
    session.run("MATCH (u:User) DETACH DELETE u;");

    // create graph user nodes from known users
    String createUsers = "CREATE ";
    String separator = "";
    for (String userName : userNames) {
      createUsers += separator + "(: User {id: " + userNames.indexOf(userName) + ", name: \"" + userName + "\"} )";
      separator = ",";
    }
    session.run(createUsers);

    // create connections between users based on known tips
    for (TipEntry entry : entries) {
      session.run("MATCH (sender:User {id: " + entry.sender + "}), (receiver:User {id: " + entry.receiver + "}) MERGE (sender)-[r:TIPS { amount: " + entry.amount + "}]->(receiver)");
    }
  }

  /**
   * Internal data structure to model a tip from one user to another
   */
  private static class TipEntry {
    int sender;
    int receiver;
    int amount;

    TipEntry(int sender, int receiver, int amount) {
      this.sender = sender;
      this.receiver = receiver;
      this.amount = amount;
    }
  }
}
