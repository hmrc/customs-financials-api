// @GENERATOR:play-routes-compiler
// @SOURCE:conf/metadata.routes

import play.api.routing.JavaScriptReverseRoute


import _root_.controllers.Assets.Asset

// @LINE:1
package controllers.metadata.javascript {

  // @LINE:1
  class ReverseMetadataController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:1
    def addNotifications: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.metadata.MetadataController.addNotifications",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "metadata"})
        }
      """
    )
  
  }


}
