// @GENERATOR:play-routes-compiler
// @SOURCE:conf/prod.routes

import play.api.mvc.Call


import _root_.controllers.Assets.Asset

// @LINE:7
package com.kenshoo.play.metrics {

  // @LINE:7
  class ReverseMetricsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:7
    def metrics: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "admin/metrics")
    }
  
  }


}
