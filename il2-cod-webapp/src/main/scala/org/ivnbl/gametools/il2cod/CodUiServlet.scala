package org.ivnbl.gametools.il2cod

import com.vaadin.terminal.gwt.server.AbstractApplicationServlet
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletConfig
import services.ui.ModuleService

class CodUiServlet extends AbstractApplicationServlet  {

  val log = LoggerFactory.getLogger(getClass)

  var moduleService:ModuleService = null

  def getApplicationClass = classOf[AdminApplication]

  def getNewApplication(request: HttpServletRequest) = {
    new AdminApplication(moduleService)
  }

  override def init(servletConfig: ServletConfig) {
    super.init(servletConfig)
    log.info("Servlet Context: {}", servletConfig.getServletContext.getContextPath)
  }

  def setModuleService(moduleService:ModuleService) {
    this.moduleService = moduleService
  }

  def unsetModuleService(moduleService:ModuleService) {
    this.moduleService = null
  }
}