// @GENERATOR:play-routes-compiler
// @SOURCE:conf/definition.routes

package controllers.definition;

import definition.RoutesPrefix;

public class routes {
  
  public static final controllers.definition.ReverseDocumentationController DocumentationController = new controllers.definition.ReverseDocumentationController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.definition.javascript.ReverseDocumentationController DocumentationController = new controllers.definition.javascript.ReverseDocumentationController(RoutesPrefix.byNamePrefix());
  }

}
