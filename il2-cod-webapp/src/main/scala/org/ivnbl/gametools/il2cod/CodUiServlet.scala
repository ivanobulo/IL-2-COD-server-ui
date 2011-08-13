package org.ivnbl.gametools.il2cod

import com.vaadin.terminal.gwt.server.AbstractApplicationServlet
import javax.servlet.http.HttpServletRequest

class CodUiServlet extends AbstractApplicationServlet  {
  def getApplicationClass = classOf[AdminApplication]

  def getNewApplication(p1: HttpServletRequest) = new AdminApplication
}