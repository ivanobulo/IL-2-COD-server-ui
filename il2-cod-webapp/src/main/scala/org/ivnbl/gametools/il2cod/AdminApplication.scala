package org.ivnbl.gametools.il2cod

import com.vaadin.{Application => VaadinApp}
import com.vaadin.ui._

class AdminApplication extends VaadinApp {
  def init() {
    val mainWindow = new Window("Hello World Application");
    val label = new Label("Greetings, Vaadin user!");
    mainWindow.addComponent(label);

    val comboBox: ComboBox = new ComboBox()
    comboBox.addItem(new Label("Apple"))
    comboBox.addItem(new Label("Orange"))
    mainWindow.addComponent(comboBox)

    setMainWindow(mainWindow);
  }
}
