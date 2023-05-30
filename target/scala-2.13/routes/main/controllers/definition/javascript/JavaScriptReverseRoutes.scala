// @GENERATOR:play-routes-compiler
// @SOURCE:conf/definition.routes

import play.api.routing.JavaScriptReverseRoute


import _root_.controllers.Assets.Asset

// @LINE:1
package controllers.definition.javascript {

  // @LINE:1
  class ReverseDocumentationController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:1
    def definition: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.definition.DocumentationController.definition",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/definition"})
        }
      """
    )
  
  }


}
