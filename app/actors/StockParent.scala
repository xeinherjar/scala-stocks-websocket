package actors

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, Scheduler }
import akka.stream.scaladsl.Flow
import play.api.libs.json.JsValue

import play.api.libs.concurrent.ActorModule
import com.google.inject.Provides

import scala.concurrent.ExecutionContext
import actors.StockActor

object StockParent extends ActorModule {
  type Message = Create
  
  final case class Create(id: String, replyTo: ActorRef[Flow[JsValue, JsValue, NotUsed]])
  
  @Provides def apply()(implicit executionContext: ExecutionContext, scheduler: Scheduler): Behavior[Create] = {
    
    Behaviors.setup { context =>
      Behaviors.logMessages {
        Behaviors.receiveMessage {
          case Create(id, replyTo) =>
          val name = s"stockActor-$id"
          context.log.info(s"Creating $name")
          
          val child = context.spawn(StockActor(), name)
          child ! StockActor.Init(replyTo)
          
          Behaviors.same
        }
      }
    }
  }
}