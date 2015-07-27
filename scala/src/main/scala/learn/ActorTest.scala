import akka.actor._

class HelloActor extends Actor {
  def receive = {
    case "hello" => println("hello!")
    case _ => println("who are you?")
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")

  val helloActor = system.actorOf(Props[HelloActor], name = "helloactor")
  helloActor ! "hello"
  helloActor ! "huh"
}
