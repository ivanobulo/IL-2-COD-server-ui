package org.ivnbl.gametools.il2cod

import com.vaadin.Application
import com.vaadin.ui.{Window, Label}

class ServiceNotAvailableApplication extends Application{
  def init() {
    val mainWindow = new Window("Service Unavailable");
    val label = new Label("Service Unavailable. Please try again later");
    mainWindow.addComponent(label);
  }
}