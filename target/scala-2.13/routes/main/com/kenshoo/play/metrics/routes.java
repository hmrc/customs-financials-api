// @GENERATOR:play-routes-compiler
// @SOURCE:conf/prod.routes

package com.kenshoo.play.metrics;

import prod.RoutesPrefix;

public class routes {
  
  public static final com.kenshoo.play.metrics.ReverseMetricsController MetricsController = new com.kenshoo.play.metrics.ReverseMetricsController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final com.kenshoo.play.metrics.javascript.ReverseMetricsController MetricsController = new com.kenshoo.play.metrics.javascript.ReverseMetricsController(RoutesPrefix.byNamePrefix());
  }

}
