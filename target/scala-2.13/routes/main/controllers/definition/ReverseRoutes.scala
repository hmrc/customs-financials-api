// @GENERATOR:play-routes-compiler
// @SOURCE:conf/definition.routes

import play.api.mvc.Call


import _root_.controllers.Assets.Asset

// @LINE:1
package controllers.definition {

  // @LINE:1
  class ReverseDocumentationController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:1
    def definition(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/definition")
    }
  
  }


}
