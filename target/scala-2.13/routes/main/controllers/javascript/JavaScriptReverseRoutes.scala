// @GENERATOR:play-routes-compiler
// @SOURCE:conf/app.routes

import play.api.routing.JavaScriptReverseRoute


import _root_.controllers.Assets.Asset

// @LINE:3
package controllers.javascript {

  // @LINE:33
  class ReverseAssets(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:33
    def at: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Assets.at",
      """
        function(file1) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/conf/1.0/" + (""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("file", file1)})
        }
      """
    )
  
  }

  // @LINE:17
  class ReverseDutyDefermentContactDetailsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:17
    def updateContactDetails: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DutyDefermentContactDetailsController.updateContactDetails",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/duty-deferment/update-contact-details"})
        }
      """
    )
  
    // @LINE:18
    def getContactDetails: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DutyDefermentContactDetailsController.getContactDetails",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/duty-deferment/contact-details"})
        }
      """
    )
  
  }

  // @LINE:5
  class ReverseHistoricDocumentRequestController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:5
    def makeRequest: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HistoricDocumentRequestController.makeRequest",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/historic-document-request"})
        }
      """
    )
  
  }

  // @LINE:26
  class ReverseSDESNotificationsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:26
    def getNotifications: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SDESNotificationsController.getNotifications",
      """
        function(eori0) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/eori/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[models.EORI]].javascriptUnbind + """)("eori", eori0)) + "/notifications"})
        }
      """
    )
  
    // @LINE:27
    def deleteNonRequestedNotifications: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SDESNotificationsController.deleteNonRequestedNotifications",
      """
        function(eori0,fileRole1) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/eori/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[models.EORI]].javascriptUnbind + """)("eori", eori0)) + "/notifications/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[models.FileRole]].javascriptUnbind + """)("fileRole", fileRole1))})
        }
      """
    )
  
    // @LINE:28
    def deleteRequestedNotifications: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SDESNotificationsController.deleteRequestedNotifications",
      """
        function(eori0,fileRole1) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/eori/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[models.EORI]].javascriptUnbind + """)("eori", eori0)) + "/requested-notifications/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[models.FileRole]].javascriptUnbind + """)("fileRole", fileRole1))})
        }
      """
    )
  
  }

  // @LINE:30
  class ReverseSubscriptionController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:30
    def getVerifiedEmail: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SubscriptionController.getVerifiedEmail",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/subscriptions/subscriptionsdisplay"})
        }
      """
    )
  
    // @LINE:31
    def getUnverifiedEmail: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SubscriptionController.getUnverifiedEmail",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/subscriptions/unverified-email-display"})
        }
      """
    )
  
    // @LINE:32
    def getEmail: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SubscriptionController.getEmail",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/subscriptions/email-display"})
        }
      """
    )
  
  }

  // @LINE:10
  class ReverseCashTransactionsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:10
    def getSummary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CashTransactionsController.getSummary",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/account/cash/transactions"})
        }
      """
    )
  
    // @LINE:11
    def getDetail: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CashTransactionsController.getDetail",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/account/cash/transactions-detail"})
        }
      """
    )
  
  }

  // @LINE:38
  class ReverseFileUploadController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:38
    def enqueueUploadedFiles: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.FileUploadController.enqueueUploadedFiles",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/submit-file-upload"})
        }
      """
    )
  
  }

  // @LINE:3
  class ReverseCustomsAccountsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:3
    def getCustomsAccountsDod09: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.CustomsAccountsController.getCustomsAccountsDod09",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/eori/accounts"})
        }
      """
    )
  
  }

  // @LINE:22
  class ReverseSearchAuthoritiesController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:22
    def searchAuthorities: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.SearchAuthoritiesController.searchAuthorities",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/search-authorities"})
        }
      """
    )
  
  }

  // @LINE:7
  class ReverseGuaranteeTransactionsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:7
    def retrieveOpenGuaranteeTransactionsSummary: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GuaranteeTransactionsController.retrieveOpenGuaranteeTransactionsSummary",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/account/guarantee/open-transactions"})
        }
      """
    )
  
    // @LINE:8
    def retrieveOpenGuaranteeTransactionsDetail: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.GuaranteeTransactionsController.retrieveOpenGuaranteeTransactionsDetail",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/account/guarantee/open-transactions-detail"})
        }
      """
    )
  
  }

  // @LINE:35
  class ReverseTPIClaimsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:35
    def getReimbursementClaims: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TPIClaimsController.getReimbursementClaims",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/get-claims"})
        }
      """
    )
  
    // @LINE:36
    def getSpecificClaim: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TPIClaimsController.getSpecificClaim",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/get-specific-claim"})
        }
      """
    )
  
  }

  // @LINE:13
  class ReverseAccountAuthoritiesController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:13
    def get: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.AccountAuthoritiesController.get",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/account-authorities"})
        }
      """
    )
  
    // @LINE:14
    def grant: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.AccountAuthoritiesController.grant",
      """
        function(eori0) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[models.EORI]].javascriptUnbind + """)("eori", eori0)) + "/account-authorities/grant"})
        }
      """
    )
  
    // @LINE:15
    def revoke: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.AccountAuthoritiesController.revoke",
      """
        function(eori0) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[models.EORI]].javascriptUnbind + """)("eori", eori0)) + "/account-authorities/revoke"})
        }
      """
    )
  
  }

  // @LINE:20
  class ReverseEORIHistoryRequestController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:20
    def validateEORI: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.EORIHistoryRequestController.validateEORI",
      """
        function(eori0) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/eori/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[models.EORI]].javascriptUnbind + """)("eori", eori0)) + "/validate"})
        }
      """
    )
  
  }

  // @LINE:24
  class ReverseAuthoritiesCsvGenerationController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:24
    def initiateAuthoritiesCsvGeneration: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.AuthoritiesCsvGenerationController.initiateAuthoritiesCsvGeneration",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "customs-financials-api/standing-authorities-file"})
        }
      """
    )
  
  }


}
