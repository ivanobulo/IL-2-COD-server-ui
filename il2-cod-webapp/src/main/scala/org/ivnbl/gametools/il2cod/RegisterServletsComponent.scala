package org.ivnbl.gametools.il2cod

import org.slf4j.LoggerFactory
import aQute.bnd.annotation.component.{Activate, Reference, Component}
import org.osgi.service.http.HttpService
import services.ui.ModuleService

@Component(immediate = true, provide = Array[Class[_]]())
class RegisterServletsComponent {
  var http:HttpService = null
  var codUiServlet = new CodUiServlet

  private val log = LoggerFactory.getLogger(classOf[RegisterServletsComponent]);

  @Reference
  def setHttp(http: HttpService) {
    this.http = http
  }

  @Reference
  def setModuleService(moduleService:ModuleService) {
    codUiServlet.setModuleService(moduleService)
  }

  def unsetAdminModule(moduleService:ModuleService) {
    codUiServlet.unsetModuleService(moduleService)
  }

  @Activate
  def activate() {
    log.info("Register Vaadin resource servlet")
    http.registerServlet("/VAADIN", new VaadinStaticResourceServlet, null, null);
    log.info("Register IL2 CoD Admin UI application")
    http.registerServlet("/admin", codUiServlet, null, null);

  }
}