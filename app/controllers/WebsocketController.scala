package controllers

import akka.actor.ActorSystem
import akka.actor.typed.{ ActorRef, Scheduler }
import akka.stream.Materializer
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import play.api.libs.json.JsValue
import play.api.Logging
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.typed.scaladsl.AskPattern._
import play.api.mvc.WebSocket.MessageFlowTransformer

import actors._
import scala.concurrent.{ ExecutionContext, Future }
import akka.stream.scaladsl.Flow
import akka.NotUsed

@Singleton
class WebsocketController @Inject()(controllerComponents: ControllerComponents,
                                    stockParent: ActorRef[StockParent.Create])
    (implicit executionContext: ExecutionContext, 
              scheduler: Scheduler) extends AbstractController(controllerComponents) with Logging {

implicit val messageFlowTransformer: MessageFlowTransformer[JsValue, JsValue] = MessageFlowTransformer.jsonMessageFlowTransformer[JsValue, JsValue]


  def connect = WebSocket.acceptOrResult[JsValue, JsValue] { request =>
    wsFutureFlow(request).map { flow => Right(flow) }
  }
  private def wsFutureFlow(request: RequestHeader): Future[Flow[JsValue, JsValue, NotUsed]] = {
    implicit val timeout = Timeout(1.second) // the first run in dev can take a while :-(
    stockParent.ask(replyTo => StockParent.Create(request.id.toString, replyTo))
  }
}