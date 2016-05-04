package twipoli

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{ Try, Success, Failure }

import akka.actor.{ ActorSystem, Props }

object Main extends App {

  if (args.size < 1) {
    println("Missing username argument!")
    System.exit(1)
  }

  val TWITTER_CONSUMER_KEY = "..."
  val TWITTER_CONSUMER_SECRET = "..."
  val username = args(0)

  println("Initializing Twipoli ActorSystem")
  val system = ActorSystem("twipoli")
  implicit val ec = system.dispatcher

  system.log.info("[Main] Retrieving Twitter Bearer Token")
  val tokenOpt = Try(Await.result(Twitter.retrieveBearerToken(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET), 5.seconds)) match {
    case Success(tokenOpt) =>
      tokenOpt
    case Failure(err) =>
      system.log.error(err, "[Main] Error Receieved While Retrieving Bearer Token")
      None
  }

  if (tokenOpt.isEmpty) {
    system.log.error("[Main] FATAL: Could Not Retrieve Bearer Token")
    system.terminate()
    System.exit(1)
  }

  system.log.info("[Main] Starting Twitter Feed Monitor Actor")
  system.actorOf(Props(new TwitterFeedMonitor(tokenOpt.get, username)))
}
