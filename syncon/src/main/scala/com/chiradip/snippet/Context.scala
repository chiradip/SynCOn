package com.chiradip {
package snippet {

import _root_.scala.xml.NodeSeq
import _root_.net.liftweb.util.Helpers
import Helpers._

import model._

class Context {
  def howdy(in: NodeSeq): NodeSeq = {
    //Helpers.bind("b", in, "time" -> (new _root_.java.util.Date).toString)
	val cond = User.loggedIn_?
	Helpers.bind("b", in, "time" -> (new _root_.java.util.Date).toString)
	
	}
	
	def loggedIn(in: NodeSeq): NodeSeq = {
		if(User.loggedIn_?) {
			Helpers.bind("c", in, "doc" -> User.currentUserId.toList(0))
		}else {
			Helpers.bind("c", in, "doc" -> "test my tests - failed")
		}
	}
	
	def ifLoggedIn(in: NodeSeq): NodeSeq = {
		if(User.loggedIn_?)
			in
		else {	
			<div>Advertisement goes here or guide</div>
		}
	}
}

}
}