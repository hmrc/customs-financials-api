// @GENERATOR:play-routes-compiler
// @SOURCE:conf/app.routes

package app

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:3
  CustomsAccountsController_4: controllers.CustomsAccountsController,
  // @LINE:5
  HistoricDocumentRequestController_8: controllers.HistoricDocumentRequestController,
  // @LINE:7
  GuaranteeTransactionsController_7: controllers.GuaranteeTransactionsController,
  // @LINE:10
  CashTransactionsController_9: controllers.CashTransactionsController,
  // @LINE:13
  AccountAuthoritiesController_3: controllers.AccountAuthoritiesController,
  // @LINE:17
  DutyDefermentContactDetailsController_1: controllers.DutyDefermentContactDetailsController,
  // @LINE:20
  SubscriptionDisplayRequestController_10: controllers.SubscriptionDisplayRequestController,
  // @LINE:22
  SearchAuthoritiesController_13: controllers.SearchAuthoritiesController,
  // @LINE:24
  AuthoritiesCsvGenerationController_5: controllers.AuthoritiesCsvGenerationController,
  // @LINE:26
  SDESNotificationsController_12: controllers.SDESNotificationsController,
  // @LINE:30
  SubscriptionController_6: controllers.SubscriptionController,
  // @LINE:33
  Assets_11: controllers.Assets,
  // @LINE:35
  TPIClaimsController_2: controllers.TPIClaimsController,
  // @LINE:38
  FileUploadController_0: controllers.FileUploadController,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:3
    CustomsAccountsController_4: controllers.CustomsAccountsController,
    // @LINE:5
    HistoricDocumentRequestController_8: controllers.HistoricDocumentRequestController,
    // @LINE:7
    GuaranteeTransactionsController_7: controllers.GuaranteeTransactionsController,
    // @LINE:10
    CashTransactionsController_9: controllers.CashTransactionsController,
    // @LINE:13
    AccountAuthoritiesController_3: controllers.AccountAuthoritiesController,
    // @LINE:17
    DutyDefermentContactDetailsController_1: controllers.DutyDefermentContactDetailsController,
    // @LINE:20
    SubscriptionDisplayRequestController_10: controllers.SubscriptionDisplayRequestController,
    // @LINE:22
    SearchAuthoritiesController_13: controllers.SearchAuthoritiesController,
    // @LINE:24
    AuthoritiesCsvGenerationController_5: controllers.AuthoritiesCsvGenerationController,
    // @LINE:26
    SDESNotificationsController_12: controllers.SDESNotificationsController,
    // @LINE:30
    SubscriptionController_6: controllers.SubscriptionController,
    // @LINE:33
    Assets_11: controllers.Assets,
    // @LINE:35
    TPIClaimsController_2: controllers.TPIClaimsController,
    // @LINE:38
    FileUploadController_0: controllers.FileUploadController
  ) = this(errorHandler, CustomsAccountsController_4, HistoricDocumentRequestController_8, GuaranteeTransactionsController_7, CashTransactionsController_9, AccountAuthoritiesController_3, DutyDefermentContactDetailsController_1, SubscriptionDisplayRequestController_10, SearchAuthoritiesController_13, AuthoritiesCsvGenerationController_5, SDESNotificationsController_12, SubscriptionController_6, Assets_11, TPIClaimsController_2, FileUploadController_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    app.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, CustomsAccountsController_4, HistoricDocumentRequestController_8, GuaranteeTransactionsController_7, CashTransactionsController_9, AccountAuthoritiesController_3, DutyDefermentContactDetailsController_1, SubscriptionDisplayRequestController_10, SearchAuthoritiesController_13, AuthoritiesCsvGenerationController_5, SDESNotificationsController_12, SubscriptionController_6, Assets_11, TPIClaimsController_2, FileUploadController_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/eori/accounts""", """controllers.CustomsAccountsController.getCustomsAccountsDod09()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/historic-document-request""", """controllers.HistoricDocumentRequestController.makeRequest()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/account/guarantee/open-transactions""", """controllers.GuaranteeTransactionsController.retrieveOpenGuaranteeTransactionsSummary()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/account/guarantee/open-transactions-detail""", """controllers.GuaranteeTransactionsController.retrieveOpenGuaranteeTransactionsDetail()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/account/cash/transactions""", """controllers.CashTransactionsController.getSummary()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/account/cash/transactions-detail""", """controllers.CashTransactionsController.getDetail()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/""" + "$" + """eori<[^/]+>/account-authorities""", """controllers.AccountAuthoritiesController.get(eori:models.EORI)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/""" + "$" + """eori<[^/]+>/account-authorities/grant""", """controllers.AccountAuthoritiesController.grant(eori:models.EORI)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/""" + "$" + """eori<[^/]+>/account-authorities/revoke""", """controllers.AccountAuthoritiesController.revoke(eori:models.EORI)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/duty-deferment/update-contact-details""", """controllers.DutyDefermentContactDetailsController.updateContactDetails()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/duty-deferment/contact-details""", """controllers.DutyDefermentContactDetailsController.getContactDetails()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/eori/""" + "$" + """eori<[^/]+>/validate""", """controllers.SubscriptionDisplayRequestController.validateEORI(eori:models.EORI)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/search-authorities""", """controllers.SearchAuthoritiesController.searchAuthorities()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/standing-authorities-file""", """controllers.AuthoritiesCsvGenerationController.initiateAuthoritiesCsvGeneration()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/eori/""" + "$" + """eori<[^/]+>/notifications""", """controllers.SDESNotificationsController.getNotifications(eori:models.EORI)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/eori/""" + "$" + """eori<[^/]+>/notifications/""" + "$" + """fileRole<[^/]+>""", """controllers.SDESNotificationsController.deleteNonRequestedNotifications(eori:models.EORI, fileRole:models.FileRole)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/eori/""" + "$" + """eori<[^/]+>/requested-notifications/""" + "$" + """fileRole<[^/]+>""", """controllers.SDESNotificationsController.deleteRequestedNotifications(eori:models.EORI, fileRole:models.FileRole)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/subscriptions/subscriptionsdisplay""", """controllers.SubscriptionController.getVerifiedEmail()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/subscriptions/unverified-email-display""", """controllers.SubscriptionController.getUnverifiedEmail()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/subscriptions/email-display""", """controllers.SubscriptionController.getEmail()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/conf/1.0/""" + "$" + """file<.+>""", """controllers.Assets.at(path:String = "/public/api/conf/1.0", file:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/get-claims""", """controllers.TPIClaimsController.getReimbursementClaims()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/get-specific-claim""", """controllers.TPIClaimsController.getSpecificClaim()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """customs-financials-api/submit-file-upload""", """controllers.FileUploadController.enqueueUploadedFiles()"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:3
  private[this] lazy val controllers_CustomsAccountsController_getCustomsAccountsDod090_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/eori/accounts")))
  )
  private[this] lazy val controllers_CustomsAccountsController_getCustomsAccountsDod090_invoker = createInvoker(
    CustomsAccountsController_4.getCustomsAccountsDod09(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.CustomsAccountsController",
      "getCustomsAccountsDod09",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/eori/accounts""",
      """""",
      Seq()
    )
  )

  // @LINE:5
  private[this] lazy val controllers_HistoricDocumentRequestController_makeRequest1_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/historic-document-request")))
  )
  private[this] lazy val controllers_HistoricDocumentRequestController_makeRequest1_invoker = createInvoker(
    HistoricDocumentRequestController_8.makeRequest(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.HistoricDocumentRequestController",
      "makeRequest",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/historic-document-request""",
      """""",
      Seq()
    )
  )

  // @LINE:7
  private[this] lazy val controllers_GuaranteeTransactionsController_retrieveOpenGuaranteeTransactionsSummary2_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/account/guarantee/open-transactions")))
  )
  private[this] lazy val controllers_GuaranteeTransactionsController_retrieveOpenGuaranteeTransactionsSummary2_invoker = createInvoker(
    GuaranteeTransactionsController_7.retrieveOpenGuaranteeTransactionsSummary(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.GuaranteeTransactionsController",
      "retrieveOpenGuaranteeTransactionsSummary",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/account/guarantee/open-transactions""",
      """""",
      Seq()
    )
  )

  // @LINE:8
  private[this] lazy val controllers_GuaranteeTransactionsController_retrieveOpenGuaranteeTransactionsDetail3_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/account/guarantee/open-transactions-detail")))
  )
  private[this] lazy val controllers_GuaranteeTransactionsController_retrieveOpenGuaranteeTransactionsDetail3_invoker = createInvoker(
    GuaranteeTransactionsController_7.retrieveOpenGuaranteeTransactionsDetail(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.GuaranteeTransactionsController",
      "retrieveOpenGuaranteeTransactionsDetail",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/account/guarantee/open-transactions-detail""",
      """""",
      Seq()
    )
  )

  // @LINE:10
  private[this] lazy val controllers_CashTransactionsController_getSummary4_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/account/cash/transactions")))
  )
  private[this] lazy val controllers_CashTransactionsController_getSummary4_invoker = createInvoker(
    CashTransactionsController_9.getSummary(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.CashTransactionsController",
      "getSummary",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/account/cash/transactions""",
      """""",
      Seq()
    )
  )

  // @LINE:11
  private[this] lazy val controllers_CashTransactionsController_getDetail5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/account/cash/transactions-detail")))
  )
  private[this] lazy val controllers_CashTransactionsController_getDetail5_invoker = createInvoker(
    CashTransactionsController_9.getDetail(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.CashTransactionsController",
      "getDetail",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/account/cash/transactions-detail""",
      """""",
      Seq()
    )
  )

  // @LINE:13
  private[this] lazy val controllers_AccountAuthoritiesController_get6_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/"), DynamicPart("eori", """[^/]+""",true), StaticPart("/account-authorities")))
  )
  private[this] lazy val controllers_AccountAuthoritiesController_get6_invoker = createInvoker(
    AccountAuthoritiesController_3.get(fakeValue[models.EORI]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.AccountAuthoritiesController",
      "get",
      Seq(classOf[models.EORI]),
      "GET",
      this.prefix + """customs-financials-api/""" + "$" + """eori<[^/]+>/account-authorities""",
      """""",
      Seq()
    )
  )

  // @LINE:14
  private[this] lazy val controllers_AccountAuthoritiesController_grant7_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/"), DynamicPart("eori", """[^/]+""",true), StaticPart("/account-authorities/grant")))
  )
  private[this] lazy val controllers_AccountAuthoritiesController_grant7_invoker = createInvoker(
    AccountAuthoritiesController_3.grant(fakeValue[models.EORI]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.AccountAuthoritiesController",
      "grant",
      Seq(classOf[models.EORI]),
      "POST",
      this.prefix + """customs-financials-api/""" + "$" + """eori<[^/]+>/account-authorities/grant""",
      """""",
      Seq()
    )
  )

  // @LINE:15
  private[this] lazy val controllers_AccountAuthoritiesController_revoke8_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/"), DynamicPart("eori", """[^/]+""",true), StaticPart("/account-authorities/revoke")))
  )
  private[this] lazy val controllers_AccountAuthoritiesController_revoke8_invoker = createInvoker(
    AccountAuthoritiesController_3.revoke(fakeValue[models.EORI]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.AccountAuthoritiesController",
      "revoke",
      Seq(classOf[models.EORI]),
      "POST",
      this.prefix + """customs-financials-api/""" + "$" + """eori<[^/]+>/account-authorities/revoke""",
      """""",
      Seq()
    )
  )

  // @LINE:17
  private[this] lazy val controllers_DutyDefermentContactDetailsController_updateContactDetails9_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/duty-deferment/update-contact-details")))
  )
  private[this] lazy val controllers_DutyDefermentContactDetailsController_updateContactDetails9_invoker = createInvoker(
    DutyDefermentContactDetailsController_1.updateContactDetails(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.DutyDefermentContactDetailsController",
      "updateContactDetails",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/duty-deferment/update-contact-details""",
      """""",
      Seq()
    )
  )

  // @LINE:18
  private[this] lazy val controllers_DutyDefermentContactDetailsController_getContactDetails10_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/duty-deferment/contact-details")))
  )
  private[this] lazy val controllers_DutyDefermentContactDetailsController_getContactDetails10_invoker = createInvoker(
    DutyDefermentContactDetailsController_1.getContactDetails(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.DutyDefermentContactDetailsController",
      "getContactDetails",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/duty-deferment/contact-details""",
      """""",
      Seq()
    )
  )

  // @LINE:20
  private[this] lazy val controllers_SubscriptionDisplayRequestController_validateEORI11_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/eori/"), DynamicPart("eori", """[^/]+""",true), StaticPart("/validate")))
  )
  private[this] lazy val controllers_SubscriptionDisplayRequestController_validateEORI11_invoker = createInvoker(
    SubscriptionDisplayRequestController_10.validateEORI(fakeValue[models.EORI]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.SubscriptionDisplayRequestController",
      "validateEORI",
      Seq(classOf[models.EORI]),
      "GET",
      this.prefix + """customs-financials-api/eori/""" + "$" + """eori<[^/]+>/validate""",
      """""",
      Seq()
    )
  )

  // @LINE:22
  private[this] lazy val controllers_SearchAuthoritiesController_searchAuthorities12_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/search-authorities")))
  )
  private[this] lazy val controllers_SearchAuthoritiesController_searchAuthorities12_invoker = createInvoker(
    SearchAuthoritiesController_13.searchAuthorities(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.SearchAuthoritiesController",
      "searchAuthorities",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/search-authorities""",
      """""",
      Seq()
    )
  )

  // @LINE:24
  private[this] lazy val controllers_AuthoritiesCsvGenerationController_initiateAuthoritiesCsvGeneration13_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/standing-authorities-file")))
  )
  private[this] lazy val controllers_AuthoritiesCsvGenerationController_initiateAuthoritiesCsvGeneration13_invoker = createInvoker(
    AuthoritiesCsvGenerationController_5.initiateAuthoritiesCsvGeneration(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.AuthoritiesCsvGenerationController",
      "initiateAuthoritiesCsvGeneration",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/standing-authorities-file""",
      """""",
      Seq()
    )
  )

  // @LINE:26
  private[this] lazy val controllers_SDESNotificationsController_getNotifications14_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/eori/"), DynamicPart("eori", """[^/]+""",true), StaticPart("/notifications")))
  )
  private[this] lazy val controllers_SDESNotificationsController_getNotifications14_invoker = createInvoker(
    SDESNotificationsController_12.getNotifications(fakeValue[models.EORI]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.SDESNotificationsController",
      "getNotifications",
      Seq(classOf[models.EORI]),
      "GET",
      this.prefix + """customs-financials-api/eori/""" + "$" + """eori<[^/]+>/notifications""",
      """""",
      Seq()
    )
  )

  // @LINE:27
  private[this] lazy val controllers_SDESNotificationsController_deleteNonRequestedNotifications15_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/eori/"), DynamicPart("eori", """[^/]+""",true), StaticPart("/notifications/"), DynamicPart("fileRole", """[^/]+""",true)))
  )
  private[this] lazy val controllers_SDESNotificationsController_deleteNonRequestedNotifications15_invoker = createInvoker(
    SDESNotificationsController_12.deleteNonRequestedNotifications(fakeValue[models.EORI], fakeValue[models.FileRole]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.SDESNotificationsController",
      "deleteNonRequestedNotifications",
      Seq(classOf[models.EORI], classOf[models.FileRole]),
      "DELETE",
      this.prefix + """customs-financials-api/eori/""" + "$" + """eori<[^/]+>/notifications/""" + "$" + """fileRole<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:28
  private[this] lazy val controllers_SDESNotificationsController_deleteRequestedNotifications16_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/eori/"), DynamicPart("eori", """[^/]+""",true), StaticPart("/requested-notifications/"), DynamicPart("fileRole", """[^/]+""",true)))
  )
  private[this] lazy val controllers_SDESNotificationsController_deleteRequestedNotifications16_invoker = createInvoker(
    SDESNotificationsController_12.deleteRequestedNotifications(fakeValue[models.EORI], fakeValue[models.FileRole]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.SDESNotificationsController",
      "deleteRequestedNotifications",
      Seq(classOf[models.EORI], classOf[models.FileRole]),
      "DELETE",
      this.prefix + """customs-financials-api/eori/""" + "$" + """eori<[^/]+>/requested-notifications/""" + "$" + """fileRole<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:30
  private[this] lazy val controllers_SubscriptionController_getVerifiedEmail17_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/subscriptions/subscriptionsdisplay")))
  )
  private[this] lazy val controllers_SubscriptionController_getVerifiedEmail17_invoker = createInvoker(
    SubscriptionController_6.getVerifiedEmail(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.SubscriptionController",
      "getVerifiedEmail",
      Nil,
      "GET",
      this.prefix + """customs-financials-api/subscriptions/subscriptionsdisplay""",
      """""",
      Seq()
    )
  )

  // @LINE:31
  private[this] lazy val controllers_SubscriptionController_getUnverifiedEmail18_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/subscriptions/unverified-email-display")))
  )
  private[this] lazy val controllers_SubscriptionController_getUnverifiedEmail18_invoker = createInvoker(
    SubscriptionController_6.getUnverifiedEmail(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.SubscriptionController",
      "getUnverifiedEmail",
      Nil,
      "GET",
      this.prefix + """customs-financials-api/subscriptions/unverified-email-display""",
      """""",
      Seq()
    )
  )

  // @LINE:32
  private[this] lazy val controllers_SubscriptionController_getEmail19_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/subscriptions/email-display")))
  )
  private[this] lazy val controllers_SubscriptionController_getEmail19_invoker = createInvoker(
    SubscriptionController_6.getEmail(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.SubscriptionController",
      "getEmail",
      Nil,
      "GET",
      this.prefix + """customs-financials-api/subscriptions/email-display""",
      """""",
      Seq()
    )
  )

  // @LINE:33
  private[this] lazy val controllers_Assets_at20_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/conf/1.0/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_at20_invoker = createInvoker(
    Assets_11.at(fakeValue[String], fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.Assets",
      "at",
      Seq(classOf[String], classOf[String]),
      "GET",
      this.prefix + """api/conf/1.0/""" + "$" + """file<.+>""",
      """""",
      Seq()
    )
  )

  // @LINE:35
  private[this] lazy val controllers_TPIClaimsController_getReimbursementClaims21_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/get-claims")))
  )
  private[this] lazy val controllers_TPIClaimsController_getReimbursementClaims21_invoker = createInvoker(
    TPIClaimsController_2.getReimbursementClaims(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.TPIClaimsController",
      "getReimbursementClaims",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/get-claims""",
      """""",
      Seq()
    )
  )

  // @LINE:36
  private[this] lazy val controllers_TPIClaimsController_getSpecificClaim22_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/get-specific-claim")))
  )
  private[this] lazy val controllers_TPIClaimsController_getSpecificClaim22_invoker = createInvoker(
    TPIClaimsController_2.getSpecificClaim(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.TPIClaimsController",
      "getSpecificClaim",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/get-specific-claim""",
      """""",
      Seq()
    )
  )

  // @LINE:38
  private[this] lazy val controllers_FileUploadController_enqueueUploadedFiles23_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("customs-financials-api/submit-file-upload")))
  )
  private[this] lazy val controllers_FileUploadController_enqueueUploadedFiles23_invoker = createInvoker(
    FileUploadController_0.enqueueUploadedFiles(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "app",
      "controllers.FileUploadController",
      "enqueueUploadedFiles",
      Nil,
      "POST",
      this.prefix + """customs-financials-api/submit-file-upload""",
      """""",
      Seq()
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:3
    case controllers_CustomsAccountsController_getCustomsAccountsDod090_route(params@_) =>
      call { 
        controllers_CustomsAccountsController_getCustomsAccountsDod090_invoker.call(CustomsAccountsController_4.getCustomsAccountsDod09())
      }
  
    // @LINE:5
    case controllers_HistoricDocumentRequestController_makeRequest1_route(params@_) =>
      call { 
        controllers_HistoricDocumentRequestController_makeRequest1_invoker.call(HistoricDocumentRequestController_8.makeRequest())
      }
  
    // @LINE:7
    case controllers_GuaranteeTransactionsController_retrieveOpenGuaranteeTransactionsSummary2_route(params@_) =>
      call { 
        controllers_GuaranteeTransactionsController_retrieveOpenGuaranteeTransactionsSummary2_invoker.call(GuaranteeTransactionsController_7.retrieveOpenGuaranteeTransactionsSummary())
      }
  
    // @LINE:8
    case controllers_GuaranteeTransactionsController_retrieveOpenGuaranteeTransactionsDetail3_route(params@_) =>
      call { 
        controllers_GuaranteeTransactionsController_retrieveOpenGuaranteeTransactionsDetail3_invoker.call(GuaranteeTransactionsController_7.retrieveOpenGuaranteeTransactionsDetail())
      }
  
    // @LINE:10
    case controllers_CashTransactionsController_getSummary4_route(params@_) =>
      call { 
        controllers_CashTransactionsController_getSummary4_invoker.call(CashTransactionsController_9.getSummary())
      }
  
    // @LINE:11
    case controllers_CashTransactionsController_getDetail5_route(params@_) =>
      call { 
        controllers_CashTransactionsController_getDetail5_invoker.call(CashTransactionsController_9.getDetail())
      }
  
    // @LINE:13
    case controllers_AccountAuthoritiesController_get6_route(params@_) =>
      call(params.fromPath[models.EORI]("eori", None)) { (eori) =>
        controllers_AccountAuthoritiesController_get6_invoker.call(AccountAuthoritiesController_3.get(eori))
      }
  
    // @LINE:14
    case controllers_AccountAuthoritiesController_grant7_route(params@_) =>
      call(params.fromPath[models.EORI]("eori", None)) { (eori) =>
        controllers_AccountAuthoritiesController_grant7_invoker.call(AccountAuthoritiesController_3.grant(eori))
      }
  
    // @LINE:15
    case controllers_AccountAuthoritiesController_revoke8_route(params@_) =>
      call(params.fromPath[models.EORI]("eori", None)) { (eori) =>
        controllers_AccountAuthoritiesController_revoke8_invoker.call(AccountAuthoritiesController_3.revoke(eori))
      }
  
    // @LINE:17
    case controllers_DutyDefermentContactDetailsController_updateContactDetails9_route(params@_) =>
      call { 
        controllers_DutyDefermentContactDetailsController_updateContactDetails9_invoker.call(DutyDefermentContactDetailsController_1.updateContactDetails())
      }
  
    // @LINE:18
    case controllers_DutyDefermentContactDetailsController_getContactDetails10_route(params@_) =>
      call { 
        controllers_DutyDefermentContactDetailsController_getContactDetails10_invoker.call(DutyDefermentContactDetailsController_1.getContactDetails())
      }
  
    // @LINE:20
    case controllers_SubscriptionDisplayRequestController_validateEORI11_route(params@_) =>
      call(params.fromPath[models.EORI]("eori", None)) { (eori) =>
        controllers_SubscriptionDisplayRequestController_validateEORI11_invoker.call(SubscriptionDisplayRequestController_10.validateEORI(eori))
      }
  
    // @LINE:22
    case controllers_SearchAuthoritiesController_searchAuthorities12_route(params@_) =>
      call { 
        controllers_SearchAuthoritiesController_searchAuthorities12_invoker.call(SearchAuthoritiesController_13.searchAuthorities())
      }
  
    // @LINE:24
    case controllers_AuthoritiesCsvGenerationController_initiateAuthoritiesCsvGeneration13_route(params@_) =>
      call { 
        controllers_AuthoritiesCsvGenerationController_initiateAuthoritiesCsvGeneration13_invoker.call(AuthoritiesCsvGenerationController_5.initiateAuthoritiesCsvGeneration())
      }
  
    // @LINE:26
    case controllers_SDESNotificationsController_getNotifications14_route(params@_) =>
      call(params.fromPath[models.EORI]("eori", None)) { (eori) =>
        controllers_SDESNotificationsController_getNotifications14_invoker.call(SDESNotificationsController_12.getNotifications(eori))
      }
  
    // @LINE:27
    case controllers_SDESNotificationsController_deleteNonRequestedNotifications15_route(params@_) =>
      call(params.fromPath[models.EORI]("eori", None), params.fromPath[models.FileRole]("fileRole", None)) { (eori, fileRole) =>
        controllers_SDESNotificationsController_deleteNonRequestedNotifications15_invoker.call(SDESNotificationsController_12.deleteNonRequestedNotifications(eori, fileRole))
      }
  
    // @LINE:28
    case controllers_SDESNotificationsController_deleteRequestedNotifications16_route(params@_) =>
      call(params.fromPath[models.EORI]("eori", None), params.fromPath[models.FileRole]("fileRole", None)) { (eori, fileRole) =>
        controllers_SDESNotificationsController_deleteRequestedNotifications16_invoker.call(SDESNotificationsController_12.deleteRequestedNotifications(eori, fileRole))
      }
  
    // @LINE:30
    case controllers_SubscriptionController_getVerifiedEmail17_route(params@_) =>
      call { 
        controllers_SubscriptionController_getVerifiedEmail17_invoker.call(SubscriptionController_6.getVerifiedEmail())
      }
  
    // @LINE:31
    case controllers_SubscriptionController_getUnverifiedEmail18_route(params@_) =>
      call { 
        controllers_SubscriptionController_getUnverifiedEmail18_invoker.call(SubscriptionController_6.getUnverifiedEmail())
      }
  
    // @LINE:32
    case controllers_SubscriptionController_getEmail19_route(params@_) =>
      call { 
        controllers_SubscriptionController_getEmail19_invoker.call(SubscriptionController_6.getEmail())
      }
  
    // @LINE:33
    case controllers_Assets_at20_route(params@_) =>
      call(Param[String]("path", Right("/public/api/conf/1.0")), params.fromPath[String]("file", None)) { (path, file) =>
        controllers_Assets_at20_invoker.call(Assets_11.at(path, file))
      }
  
    // @LINE:35
    case controllers_TPIClaimsController_getReimbursementClaims21_route(params@_) =>
      call { 
        controllers_TPIClaimsController_getReimbursementClaims21_invoker.call(TPIClaimsController_2.getReimbursementClaims())
      }
  
    // @LINE:36
    case controllers_TPIClaimsController_getSpecificClaim22_route(params@_) =>
      call { 
        controllers_TPIClaimsController_getSpecificClaim22_invoker.call(TPIClaimsController_2.getSpecificClaim())
      }
  
    // @LINE:38
    case controllers_FileUploadController_enqueueUploadedFiles23_route(params@_) =>
      call { 
        controllers_FileUploadController_enqueueUploadedFiles23_invoker.call(FileUploadController_0.enqueueUploadedFiles())
      }
  }
}
