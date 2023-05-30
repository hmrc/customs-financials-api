// @GENERATOR:play-routes-compiler
// @SOURCE:conf/app.routes

import play.api.mvc.Call


import _root_.controllers.Assets.Asset

// @LINE:3
package controllers {

  // @LINE:33
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:33
    def at(file:String): Call = {
      implicit lazy val _rrc = new play.core.routing.ReverseRouteContext(Map(("path", "/public/api/conf/1.0"))); _rrc
      Call("GET", _prefix + { _defaultPrefix } + "api/conf/1.0/" + implicitly[play.api.mvc.PathBindable[String]].unbind("file", file))
    }
  
  }

  // @LINE:17
  class ReverseDutyDefermentContactDetailsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:17
    def updateContactDetails(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/duty-deferment/update-contact-details")
    }
  
    // @LINE:18
    def getContactDetails(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/duty-deferment/contact-details")
    }
  
  }

  // @LINE:5
  class ReverseHistoricDocumentRequestController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:5
    def makeRequest(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/historic-document-request")
    }
  
  }

  // @LINE:26
  class ReverseSDESNotificationsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:26
    def getNotifications(eori:models.EORI): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "customs-financials-api/eori/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[models.EORI]].unbind("eori", eori)) + "/notifications")
    }
  
    // @LINE:27
    def deleteNonRequestedNotifications(eori:models.EORI, fileRole:models.FileRole): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "customs-financials-api/eori/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[models.EORI]].unbind("eori", eori)) + "/notifications/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[models.FileRole]].unbind("fileRole", fileRole)))
    }
  
    // @LINE:28
    def deleteRequestedNotifications(eori:models.EORI, fileRole:models.FileRole): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "customs-financials-api/eori/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[models.EORI]].unbind("eori", eori)) + "/requested-notifications/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[models.FileRole]].unbind("fileRole", fileRole)))
    }
  
  }

  // @LINE:30
  class ReverseSubscriptionController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:30
    def getVerifiedEmail(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "customs-financials-api/subscriptions/subscriptionsdisplay")
    }
  
    // @LINE:31
    def getUnverifiedEmail(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "customs-financials-api/subscriptions/unverified-email-display")
    }
  
    // @LINE:32
    def getEmail(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "customs-financials-api/subscriptions/email-display")
    }
  
  }

  // @LINE:10
  class ReverseCashTransactionsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:10
    def getSummary(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/account/cash/transactions")
    }
  
    // @LINE:11
    def getDetail(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/account/cash/transactions-detail")
    }
  
  }

  // @LINE:38
  class ReverseFileUploadController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:38
    def enqueueUploadedFiles(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/submit-file-upload")
    }
  
  }

  // @LINE:3
  class ReverseCustomsAccountsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:3
    def getCustomsAccountsDod09(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/eori/accounts")
    }
  
  }

  // @LINE:22
  class ReverseSearchAuthoritiesController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:22
    def searchAuthorities(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/search-authorities")
    }
  
  }

  // @LINE:7
  class ReverseGuaranteeTransactionsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:7
    def retrieveOpenGuaranteeTransactionsSummary(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/account/guarantee/open-transactions")
    }
  
    // @LINE:8
    def retrieveOpenGuaranteeTransactionsDetail(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/account/guarantee/open-transactions-detail")
    }
  
  }

  // @LINE:35
  class ReverseTPIClaimsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:35
    def getReimbursementClaims(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/get-claims")
    }
  
    // @LINE:36
    def getSpecificClaim(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/get-specific-claim")
    }
  
  }

  // @LINE:13
  class ReverseAccountAuthoritiesController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:13
    def get(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "customs-financials-api/account-authorities")
    }
  
    // @LINE:14
    def grant(eori:models.EORI): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[models.EORI]].unbind("eori", eori)) + "/account-authorities/grant")
    }
  
    // @LINE:15
    def revoke(eori:models.EORI): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[models.EORI]].unbind("eori", eori)) + "/account-authorities/revoke")
    }
  
  }

  // @LINE:20
  class ReverseEORIHistoryRequestController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:20
    def validateEORI(eori:models.EORI): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "customs-financials-api/eori/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[models.EORI]].unbind("eori", eori)) + "/validate")
    }
  
  }

  // @LINE:24
  class ReverseAuthoritiesCsvGenerationController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:24
    def initiateAuthoritiesCsvGeneration(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "customs-financials-api/standing-authorities-file")
    }
  
  }


}
