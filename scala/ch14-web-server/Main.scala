package app

import java.lang.System.nanoTime
import scalatags.Text.all.*
import cask.endpoints.staticResources

def profile[R](code: => R, t: Long = nanoTime) =
  (code, (nanoTime - t) / 1000 / 1000) // convert nanotime to milisec

object MinimalApplication extends cask.MainRoutes:
  var messages = Vector(("alice", "hello world"), ("bob", "I am cow, hear me moo"))
  var openConnections = collection.mutable.Set.empty[cask.endpoints.WsChannelActor]
  val bootstrap = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.css"

  @cask.staticFiles("/static")
  def staticResources() = "static"

  @cask.get("/")
  def hello() = doctype("html")(
    html(
      head(
        link(rel := "stylesheet", href := bootstrap),
        script(src := "/static/app.js")
      ),
      body(
        div(cls := "container")(
          h1("Scala Chat!"),
          div(id := "messageList")(messageList()),
          div(id := "errorDiv", color.red),
          form(onsubmit := "submitForm(); return false")(
            input(`type` := "text", id := "nameInput", placeholder := "User Name"),
            input(`type` := "text", id := "msgInput", placeholder := "Write a message!"),
            input(`type` := "submit")
          )
        )
      )
    )
  )

  def messageList() = frag(for (name, msg) <- messages yield p(b(name), " ", msg))

  @cask.postJson("/")
  def postChatMsg(name: String, msg: String) =
    if name.isBlank then ujson.Obj("success" -> false, "err" -> "Name cannot by empty")
    else if msg.isBlank then
      ujson.Obj("success" -> false, "err" -> "Message cannot by empty")
    else
      messages = messages :+ (name -> msg)
      for conn <- openConnections do conn.send(cask.Ws.Text(messageList().render))
      ujson.Obj("sucess" -> true, "err" -> "")

  @cask.websocket("/subscribe")
  def subscribe() = cask.WsHandler { connection =>
    connection.send(cask.Ws.Text(messageList().render))
    openConnections += connection
    cask.WsActor { case cask.Ws.Close(_, _) => openConnections -= connection }
  }

  initialize()
