package io.github.unterstein;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

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
    QueryResult result;
    try {
      Query query = new Query("you received a tip from:tipreddcoin");
      query.setCount(80);
      do {
        log.info("Starting run #" + i);
        log.info(query.toString());
        result = twitter.search(query);
        log.info(String.format("Rate limit status remaining: %d", result.getRateLimitStatus().getRemaining()));
        if (result.getRateLimitStatus().getRemaining() == 0) {
          int coolDown = (result.getRateLimitStatus().getSecondsUntilReset() + 2);
          log.warn(String.format("Sleeping %d seconds to cool down rate limit", coolDown));
          Thread.sleep(coolDown * 1000);
        } else {
          List<Status> tweets = result.getTweets();
          if (tweets.size() == 0) {
            log.info("Did not found new tweets, stopping.");
            break;
          }
          log.info(String.format("Found %d tweets", tweets.size()));
          for (Status status : tweets) {
            String text = status.getText();
            if (text.contains(", you have received a tip of Ɍ")) {
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
        i++;
        Thread.sleep(2000);
      } while ((query = result.nextQuery()) != null);
    } catch (Exception e) {
      log.error("Unable to perform twitter import", e);
    }
  }
}
