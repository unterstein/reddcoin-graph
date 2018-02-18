package io.github.unterstein;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Import data from twitter to local data structure
 */
public class TwitterImport {

  private static final Logger log = LoggerFactory.getLogger(TwitterImport.class);
  private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE MMM dd.MM.yy HH.mm.ss zzz");

  public static void main(String[] args) {
    // do rudimentary input arg validation
    if (args.length != 4) {
      log.error("Please provide the following twitter api information in this order:");
      log.error("$customerKey $customerSecret $accessToken $accessTokenSecret");
      System.exit(1);
    }
    // setup twitter client
    TwitterFactory factory = new TwitterFactory();
    Twitter twitter = factory.getInstance();
    twitter.setOAuthConsumer(args[0], args[1]);
    twitter.setOAuthAccessToken(new AccessToken(args[2], args[3]));

    // search in twitter timeline
    File outputFile = new File("output.txt");
    int i = 0;
    try {
      Query query = new Query("\"you have received a tip\" from:tipreddcoin");
      while (true) {
        log.info("Starting run #" + i);
        query.setCount(100);
        QueryResult result = twitter.search(query);
        if (result.getRateLimitStatus().getRemaining() == 0) {
          int coolDown = (result.getRateLimitStatus().getSecondsUntilReset() + 2);
          log.warn(String.format("Sleeping %d seconds to cool down rate limit", coolDown));
          Thread.sleep(coolDown * 1000);
        } else {
          if (result.getTweets().size() == 0) {
            log.info("Did not found new tweets, stopping.");
            break;
          }
          for (Status status : result.getTweets()) {
            String text = status.getText();
            if (text.contains("confirmed:  -->>@")) {
              String receiver = StringUtils.substringBefore(text, ", you have received a");
              String sender = StringUtils.substringBetween(text, " from ", ".");
              String amount = StringUtils.substringBetween(text, "Ɍ", " Reddcoins");
              String date = simpleDateFormat.format(status.getCreatedAt());
              String output = String.format("%s:%s:%s:%s", sender, receiver, amount, date);
              log.info(output);
              FileUtils.writeStringToFile(outputFile, output + System.lineSeparator(), "UTF-8", true);
            }
          }
        }
        if (result.hasNext()) {
          query = result.nextQuery();
        }
        i++;
      }
    } catch (Exception e) {
      log.error("Unable to perform twitter import", e);
    }
  }
}
