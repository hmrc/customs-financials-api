
package views.txt

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.txt._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._
/*1.2*/import play.api.libs.json.Json

object definition extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.TxtFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.TxtFormat.Appendable]](play.twirl.api.TxtFormat) with _root_.play.twirl.api.Template0[play.twirl.api.TxtFormat.Appendable] {

  /**/
  def apply():play.twirl.api.TxtFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*3.1*/("""{"""),format.raw/*3.2*/("""
  """),format.raw/*4.3*/(""""scopes": [
    """),format.raw/*5.5*/("""{"""),format.raw/*5.6*/("""
      """),format.raw/*6.7*/(""""key": "customs-financials-api",
      "name": "Statement Notifications",
      "description": "Send C79, PVAT and Security statement notifications for new statements"
    """),format.raw/*9.5*/("""}"""),format.raw/*9.6*/("""
  """),format.raw/*10.3*/("""],
  "api": """),format.raw/*11.10*/("""{"""),format.raw/*11.11*/("""
    """),format.raw/*12.5*/(""""name": "CDS Financials Notifications",
    "description": "Notifications API sends requests to customs financials api service to display new statements notifications",
    "context": "customs/statements",
    "versions": [
      """),format.raw/*16.7*/("""{"""),format.raw/*16.8*/("""
        """),format.raw/*17.9*/(""""version": "1.0",
        "status": "STABLE",
        "endpointsEnabled": true,
        "access": """),format.raw/*20.19*/("""{"""),format.raw/*20.20*/("""
          """),format.raw/*21.11*/(""""type": "PRIVATE",
          "whitelistedApplicationIds": []
        """),format.raw/*23.9*/("""}"""),format.raw/*23.10*/(""",
        "endpointsEnabled": true
      """),format.raw/*25.7*/("""}"""),format.raw/*25.8*/("""
    """),format.raw/*26.5*/("""]
  """),format.raw/*27.3*/("""}"""),format.raw/*27.4*/("""
"""),format.raw/*28.1*/("""}"""),format.raw/*28.2*/("""
"""))
      }
    }
  }

  def render(): play.twirl.api.TxtFormat.Appendable = apply()

  def f:(() => play.twirl.api.TxtFormat.Appendable) = () => apply()

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  SOURCE: app/views/definition.scala.txt
                  HASH: 387cd35d24fead43cab5210446c44782d6de9b97
                  MATRIX: 430->1|847->33|874->34|903->37|945->53|972->54|1005->61|1203->233|1230->234|1260->237|1300->249|1329->250|1361->255|1618->485|1646->486|1682->495|1808->593|1837->594|1876->605|1972->674|2001->675|2069->716|2097->717|2129->722|2160->726|2188->727|2216->728|2244->729
                  LINES: 17->1|27->3|27->3|28->4|29->5|29->5|30->6|33->9|33->9|34->10|35->11|35->11|36->12|40->16|40->16|41->17|44->20|44->20|45->21|47->23|47->23|49->25|49->25|50->26|51->27|51->27|52->28|52->28
                  -- GENERATED --
              */
          