// @GENERATOR:play-routes-compiler
// @SOURCE:conf/metadata.routes

import play.api.mvc.Call


import _root_.controllers.Assets.Asset

// @LINE:1
package controllers.metadata {

  // @LINE:1
  class ReverseMetadataController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:1
    def addNotifications(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "metadata")
    }
  
  }


}
