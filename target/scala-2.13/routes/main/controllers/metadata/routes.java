// @GENERATOR:play-routes-compiler
// @SOURCE:conf/metadata.routes

package controllers.metadata;

import metadata.RoutesPrefix;

public class routes {
  
  public static final controllers.metadata.ReverseMetadataController MetadataController = new controllers.metadata.ReverseMetadataController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.metadata.javascript.ReverseMetadataController MetadataController = new controllers.metadata.javascript.ReverseMetadataController(RoutesPrefix.byNamePrefix());
  }

}
