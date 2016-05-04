package twipoli

import java.util.Base64
import java.nio.charset.StandardCharsets
import java.net.URLEncoder

import scala.concurrent.{ Future, ExecutionContext }

import play.api.libs.json.{ Json, JsValue }
import dispatch.{ Http, url, as }

object Twitter {

  def retrieveBearerToken(key: String, secret: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val urlEncodedKey = URLEncoder.encode(key, "UTF-8")
    val urlEncodedSecret = URLEncoder.encode(secret, "UTF-8")
    val auth = s"${urlEncodedKey}:${urlEncodedSecret}"
    val base64Auth = Base64.getEncoder.encodeToString(auth.getBytes(StandardCharsets.UTF_8))
    val request =
      url("https://api.twitter.com/oauth2/token")
        .addHeader("Authorization", s"Basic ${base64Auth}")
        .addHeader("Content-Type", "application/x-www-form-urlencoded;charset:UTF-8") << "grant_type=client_credentials"
    Http(request OK as.String).map(extractBearerToken)
  }

  private def extractBearerToken(response: String): Option[String] = {
    val json = Json.parse(response)
    val tokenTypeOpt = (json \ "token_type").asOpt[String]
    val accessTokenOpt = (json \ "access_token").asOpt[String]
    if (tokenTypeOpt.isDefined && tokenTypeOpt.get == "bearer")
      accessTokenOpt
    else
      None
  }

  def retrieveTweets(token: String, username: String, sinceId: Option[Long] = None)(implicit ec: ExecutionContext): Future[Seq[Tweet]] = {
    var request = url("https://api.twitter.com/1.1/statuses/user_timeline.json")
      .addHeader("Authorization", s"Bearer ${token}")
      .addQueryParameter("screen_name", username)

    if (sinceId.isDefined)
      request = request.addQueryParameter("since_id", sinceId.get.toString)

    Http(request OK as.String).map(extractTweets)
  }

  private def extractTweets(response: String): Seq[Tweet] = {
    Json.parse(response).as[Seq[JsValue]].map(extractTweet)
  }

  private def extractTweet(jsval: JsValue): Tweet = {
    Tweet(
      id = (jsval \ "id").as[Long],
      username = (jsval \ "user" \ "name").as[String],
      text = (jsval \ "text").as[String],
      created = (jsval \ "created_at").as[String])
  }
}

case class Tweet(id: Long, username: String, text: String, created: String)
