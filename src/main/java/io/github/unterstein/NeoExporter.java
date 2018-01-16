package io.github.unterstein;

import org.apache.commons.io.FileUtils;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NeoExporter {

  public static void main(String[] args) throws IOException {
    String[] lines = FileUtils.readFileToString(new File("output.txt"), "UTF-8").split(System.lineSeparator());
    List<String> userNames = new ArrayList<>();
    List<TipEntry> entries = new ArrayList<>();

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

    Driver neo4jDriver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "neo4j123"));
    Session session = neo4jDriver.session();

    session.run("MATCH (u:User) DETACH DELETE u;");

    String createUsers = "CREATE ";
    String separator = "";
    for (String userName : userNames) {
      createUsers += separator + "(: User {id: " + userNames.indexOf(userName) + ", name: \"" + userName + "\"} )";
      separator = ",";
    }
    session.run(createUsers);

    for (TipEntry entry : entries) {
      session.run("MATCH (sender:User {id: " + entry.sender + "}), (receiver:User {id: " + entry.receiver + "}) MERGE (sender)-[r:TIPS { amount: " + entry.amount + "}]->(receiver)");
    }
  }

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
