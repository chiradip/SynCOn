package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import js.jquery.JQuery14Artifacts
import sitemap._
import Loc._
import mapper._

/* import code.model._
# import _root_.net.liftweb.common._
# import _root_.net.liftweb.util._
# import _root_.net.liftweb.http._
# import _root_.net.liftweb.sitemap._
# import _root_.net.liftweb.sitemap.Loc._
# import Helpers._
# import scala._
# 
# import js.jquery.JQuery14Artifacts
# 
# import net.liftweb.mapper._ */

import com.chiradip.model._

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {

	if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr 
			     "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User)


    // where to search snippet
    LiftRules.addToPackages("com.chiradip")

	
	// make the below boolean value true if you want the link ("conditional"") to be visible 

    // Build SiteMap
    // val entries = Menu(Loc("Home", List("index"), "Home")) :: Nil
    // LiftRules.setSiteMap(SiteMap(entries:_*))

	def sitemap = SiteMap(
		Menu.i("index") / "index" >> User.AddUserMenusAfter, 
		Menu.i("TestLink") / "testlink" >> LocGroup("admin"),
		Menu.i("conditional") / "conditionl" >> If(User.loggedIn_? _, S? "Can't view now")
	)
	
	def sitemapMutators = User.sitemapMutator
	
	// To have page based access control 
	LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))
	
	//Show the spinny image when an Ajax call starts
	LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
	
	// Make the spinny image go away when it ends
	LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
	
	// Force the request to be UTF-8
	LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
	
	// Use HTML5 for rendering
	LiftRules.htmlProperties.default.set((r: Req) =>
	      new Html5Properties(r.userAgent))
  }
}

import rest._
import json._
import JsonDSL._
object FileUpload extends RestHelper {
  serve {
    case "upload" :: "thing" :: Nil Post req => {
      println("uploaded "+req.uploadedFiles)
      val ojv: Box[JValue] = 
        req.uploadedFiles.map(fph => ("name" -> fph.fileName) ~ 
                              ("type" -> fph.mimeType) ~
                              ("size" -> fph.length)).headOption

      val ajv = ("name" -> "n/a") ~ ("type" -> "n/a") ~ ("size" -> 0L) ~ ("yak" -> "brrrr")

      val ret = ojv openOr ajv

      // This is a tad bit of a hack, but we need to return text/plain, not JSON
      val jr = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
      InMemoryResponse(jr.data, ("Content-Length", jr.data.length.toString) :: 
                       ("Content-Type", "text/plain") :: S.getHeaders(Nil),
                       S.responseCookies, 200)
    }
  }
}