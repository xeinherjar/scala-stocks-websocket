package actors

import akka.actor.Cancellable
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, PostStop, Scheduler }
import akka.{ Done, NotUsed }
import akka.stream.scaladsl.{ Flow, Sink, Source }

import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{ Future }
import scala.jdk.CollectionConverters._

import yahoofinance.YahooFinance

object StockActor {
  sealed trait Message

  case class Init(replyTo: ActorRef[Flow[JsValue, JsValue, NotUsed]]) extends Message
  case class SubscribeToStock(symbol: String) extends Message
  case class UnsubscribeFromStock(symbol: String) extends Message

  case class Stock(symbol: String, price: java.math.BigDecimal)
  case class Action(action: String, value: String)

  def apply(): Behavior[Message] = Behaviors.setup { implicit context => new StockActor().behavior }

}

class StockActor(implicit context: ActorContext[StockActor.Message]) {
  import StockActor._

  implicit val BigDecimalWrite: Writes[java.math.BigDecimal] = new Writes[java.math.BigDecimal] {
    override def writes(bigDecimal: java.math.BigDecimal): JsValue = JsNumber(bigDecimal)
  }
  implicit val stockWrites = Json.writes[Stock]
  implicit val actionReads = Json.reads[Action]

  def behavior: Behavior[Message] = {
    Behaviors.receiveMessage[Message] {
      case Init(replyTo) => {
        context.log.info(s"Initializing StockActor")
        replyTo ! websocketFlow
        Behaviors.same
      }
      case SubscribeToStock(symbol) => {
        context.log.info(s"Subcribing to: $symbol")
        subscribe(symbol)
        Behaviors.same
      }
      case UnsubscribeFromStock(symbol) => {
        context.log.info(s"Unsubcribing from: $symbol")
        unSubscribe(symbol)
        Behaviors.same
      }
    }
  }
  private val stockSymbols = scala.collection.mutable.Set[String]()

  private def subscribe(symbol: String) = {
    stockSymbols += symbol
  }

  private def unSubscribe(symbol: String) = {
    stockSymbols -= symbol
  }

  private val sink: Sink[JsValue, Future[Done]] = Sink.foreach { payload =>
    Json.fromJson[Action](payload) match {
      case JsSuccess(Action("subscribe", symbol), _) => context.self ! SubscribeToStock(symbol) 
      case JsSuccess(Action("unsubscribe", symbol), _) => context.self ! UnsubscribeFromStock(symbol)
      case JsSuccess(Action(action: String, value), _) => { context.log.warn(s"Unknown action: $action")}
      case error @ JsError(_) => { context.log.warn(s"Parse error: $JsError.toJson(error): $payload")}
    }
  }

  private val source: Source[JsValue, Cancellable] = Source.tick(0.second, 1.second, "tick")
    .map(_ => {
      if (stockSymbols.isEmpty) { 
        Json.toJson(Array[Stock]())
      } else {
        val stockResults = YahooFinance.get(stockSymbols.toArray)
        val stocks = stockResults.asScala
        .filter{ case (k, v) => v.getQuote().getPrice() != null } // Bad ticker name
        .map{ case (k, v) => Json.toJson[Stock](Stock(k, v.getQuote().getPrice())) }
        Json.toJson(stocks) 
      }
    })

  private lazy val websocketFlow: Flow[JsValue, JsValue, NotUsed] = {
    Flow.fromSinkAndSourceCoupled(sink, source)
  }
}