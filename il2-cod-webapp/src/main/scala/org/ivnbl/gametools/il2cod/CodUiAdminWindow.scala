package org.ivnbl.gametools.il2cod

import com.vaadin.ui._
import services.ui.AdminModule

class CodUiAdminWindow(modules:List[AdminModule]) extends Window {
  val sheet = new ScrollableTabSheet(modules)
  sheet.setWidth("600px")
  sheet.setHeight("200px")

  addComponent(new Label("Modules:"))
  addComponent(sheet)

  def addTab(adminModule: AdminModule) {
    sheet.addTab(adminModule)
  }

  def removeTab(caption: String) {
    sheet.removeTab(caption)
  }
}