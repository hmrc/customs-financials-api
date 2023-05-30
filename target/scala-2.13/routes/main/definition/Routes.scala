// @GENERATOR:play-routes-compiler
// @SOURCE:conf/definition.routes

package definition

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:1
  DocumentationController_0: controllers.definition.DocumentationController,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:1
    DocumentationController_0: controllers.definition.DocumentationController
  ) = this(errorHandler, DocumentationController_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    definition.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, DocumentationController_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/definition""", """controllers.definition.DocumentationController.definition()"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:1
  private[this] lazy val controllers_definition_DocumentationController_definition0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/definition")))
  )
  private[this] lazy val controllers_definition_DocumentationController_definition0_invoker = createInvoker(
    DocumentationController_0.definition(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "definition",
      "controllers.definition.DocumentationController",
      "definition",
      Nil,
      "GET",
      this.prefix + """api/definition""",
      """""",
      Seq()
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:1
    case controllers_definition_DocumentationController_definition0_route(params@_) =>
      call { 
        controllers_definition_DocumentationController_definition0_invoker.call(DocumentationController_0.definition())
      }
  }
}
