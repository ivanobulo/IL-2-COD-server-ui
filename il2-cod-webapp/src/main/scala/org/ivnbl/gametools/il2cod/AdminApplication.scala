package org.ivnbl.gametools.il2cod

import com.vaadin.{Application => VaadinApp}
import services.ui.{AdminModule, ModuleService, ModuleServiceListener}

class AdminApplication(moduleService:ModuleService) extends VaadinApp with ModuleServiceListener {
  private var mainWindow:CodUiAdminWindow = null

  def init() {
    mainWindow = new CodUiAdminWindow(moduleService.adminModules)
    //Register listener
    moduleService addListener this
    //Complete initialization
    setMainWindow(mainWindow);
  }

  override def close() {
    super.close()
    //Remove listener
    moduleService removeListener this
  }

  def moduleRegistered(service: ModuleService, adminModule: AdminModule) {
    mainWindow.addTab(adminModule)
  }

  def moduleUnregistered(service: ModuleService, adminModule: AdminModule) {
    mainWindow.removeTab(adminModule.name)
  }
}
