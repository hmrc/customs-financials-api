// @GENERATOR:play-routes-compiler
// @SOURCE:conf/prod.routes

import play.api.routing.JavaScriptReverseRoute


import _root_.controllers.Assets.Asset

// @LINE:7
package com.kenshoo.play.metrics.javascript {

  // @LINE:7
  class ReverseMetricsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:7
    def metrics: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "com.kenshoo.play.metrics.MetricsController.metrics",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "admin/metrics"})
        }
      """
    )
  
  }


}
