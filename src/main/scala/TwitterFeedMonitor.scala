package twipoli

import scala.concurrent.duration._
import scala.util.{ Success, Failure }

import akka.actor.{ Actor, ActorLogging }

object TwitterFeedMonitor {
  case object CheckTwitterFeed
  case class NewTweets(tweets: Seq[Tweet])
}

import TwitterFeedMonitor._

class TwitterFeedMonitor(token: String, username: String) extends Actor with ActorLogging {

  implicit val ec = context.dispatcher
  var newestId: Option[Long] = None

  def receive = {
    case CheckTwitterFeed =>
      log.debug("[TwitterFeedMonitor] Checking twitter feed.")
      checkTwitterFeed()
    case NewTweets(tweets) =>
      log.debug("[TwitterFeedMonitor] Found {} new tweets", tweets.size)
      processTweets(tweets)
    case _ =>
  }

  def checkTwitterFeed() {
    Twitter.retrieveTweets(token, username, newestId).onComplete {
      case Success(tweets) => if (tweets.nonEmpty) self ! NewTweets(tweets)
      case Failure(err) => log.error("[TwitterFeedMonitor] Caught error trying to retrieve tweets.")
    }
  }

  def processTweets(tweets: Seq[Tweet]) {
    tweets.foreach { tweet =>
      if (newestId.isEmpty || newestId.get < tweet.id)
        newestId = Some(tweet.id)

      log.info("[TwitterFeedMonitor] New Tweet: {} - {}", tweet.text, tweet.created)

    }
  }

  override def preStart() {
    context.system.scheduler.schedule(1.seconds, 5.seconds, self, CheckTwitterFeed)
  }
}
