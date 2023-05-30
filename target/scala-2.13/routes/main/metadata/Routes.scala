// @GENERATOR:play-routes-compiler
// @SOURCE:conf/metadata.routes

package metadata

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:1
  MetadataController_0: controllers.metadata.MetadataController,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:1
    MetadataController_0: controllers.metadata.MetadataController
  ) = this(errorHandler, MetadataController_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    metadata.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, MetadataController_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """metadata""", """controllers.metadata.MetadataController.addNotifications()"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:1
  private[this] lazy val controllers_metadata_MetadataController_addNotifications0_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("metadata")))
  )
  private[this] lazy val controllers_metadata_MetadataController_addNotifications0_invoker = createInvoker(
    MetadataController_0.addNotifications(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "metadata",
      "controllers.metadata.MetadataController",
      "addNotifications",
      Nil,
      "POST",
      this.prefix + """metadata""",
      """""",
      Seq()
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:1
    case controllers_metadata_MetadataController_addNotifications0_route(params@_) =>
      call { 
        controllers_metadata_MetadataController_addNotifications0_invoker.call(MetadataController_0.addNotifications())
      }
  }
}
