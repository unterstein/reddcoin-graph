package io.github.unterstein;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.File;

public class TwitterImport {

  private static final Logger log = LoggerFactory.getLogger(TwitterImport.class);

  public static void main(String[] args) {
    // do rudimentary input arg validation
    if (args.length != 4) {
      log.error("Please provide the following twitter api information in this order:");
      log.error("$customerKey $customerSecret $accessToken $accessTokenSecret");
    }
    // setup twitter client
    TwitterFactory factory = new TwitterFactory();
    Twitter twitter = factory.getInstance();
    twitter.setOAuthConsumer(args[0], args[1]);
    twitter.setOAuthAccessToken(new AccessToken(args[2], args[3]));

    // search in twitter timeline
    File outputFile = new File("output.txt");
    try {
      boolean requestsAvailable = true;
      for (int i = 1; i < 500; i++) {
        log.info("Starting run #" + i);
        if (requestsAvailable) {
          ResponseList<Status> tipreddcoin = twitter.getUserTimeline("tipreddcoin", new Paging(i, 100));
          requestsAvailable = tipreddcoin.getRateLimitStatus().getRemaining() > 0;
          for (Status status : tipreddcoin) {
            String text = status.getText();
            if (text.contains("confirmed:  -->>@")) {
              String sender = StringUtils.substringBefore(text, " confirmed:  -->>");
              String receiver = StringUtils.substringBetween(text, " confirmed:  -->>", " ");
              String amount = StringUtils.substringBetween(text, "Ɍ", " Reddcoins");
              String output = String.format("%s:%s:%s", sender, receiver, amount);
              log.info(output);
              FileUtils.writeStringToFile(outputFile, output + System.lineSeparator(), "UTF-8", true);
            }
          }
        } else {
          log.info("rate limited");
        }
        log.info("sleeping");
        Thread.sleep(1500);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
