import _root_.akka.actor.{Props, ActorSystem}
import org.scalatra._
import javax.servlet.ServletContext

import techsupply.flagship.{JsonTestController, FutureController, MyActor, MyActorApp}


class ScalatraBootstrap extends LifeCycle {

  val system = ActorSystem()
  val myActor = system.actorOf(Props[MyActor])
  val DSE_HOST = "172.16.131.142"

  override def init(context: ServletContext) {
    context.mount(new JsonTestController(system, DSE_HOST), "/*")
    context.mount(new FutureController(system, DSE_HOST), "/futures/*")
    context.mount(new MyActorApp(system, myActor), "/actors/*")
  }

  override def destroy(context:ServletContext) {
    system.shutdown()
  }
}
