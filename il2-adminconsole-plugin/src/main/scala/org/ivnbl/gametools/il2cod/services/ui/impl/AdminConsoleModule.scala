package org.ivnbl.gametools.il2cod.services.ui.impl

import org.ivnbl.gametools.il2cod.services.ui.AdminModule
import aQute.bnd.annotation.component.Component
import com.vaadin.ui.{VerticalLayout, Label}

@Component(provide = Array[Class[_]](classOf[AdminModule]))
class AdminConsoleModule extends AdminModule{
  def name = "Console"

  def buildComponent() = {
    val layout = new VerticalLayout()
    layout.setMargin(true)
    layout.addComponent(new Label("This is a test"))
    layout
  }
}