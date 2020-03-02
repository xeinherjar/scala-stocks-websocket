package controllers

import akka.actor.ActorSystem
import akka.actor.typed.{ ActorRef, Scheduler }
import akka.actor.typed.scaladsl.AskPattern._
import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout

import javax.inject._

import play.api._
import play.api.libs.streams.ActorFlow
import play.api.libs.json.JsValue
import play.api.Logging
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import actors._

@Singleton
class WebsocketController @Inject()(controllerComponents: ControllerComponents, stockParent: ActorRef[StockParent.Create])
    (implicit executionContext: ExecutionContext, scheduler: Scheduler) extends AbstractController(controllerComponents) with Logging {

implicit val messageFlowTransformer: WebSocket.MessageFlowTransformer[JsValue, JsValue] = 
  WebSocket.MessageFlowTransformer.jsonMessageFlowTransformer[JsValue, JsValue]

  def connect = WebSocket.acceptOrResult[JsValue, JsValue] { request =>
    wsFutureFlow(request).map { flow => Right(flow) }
  }

  private def wsFutureFlow(request: RequestHeader): Future[Flow[JsValue, JsValue, NotUsed]] = {
    implicit val timeout = Timeout(1.second) // the first run in dev can take a while :-(
    stockParent.ask(replyTo => StockParent.Create(request.id.toString, replyTo))
  }
}