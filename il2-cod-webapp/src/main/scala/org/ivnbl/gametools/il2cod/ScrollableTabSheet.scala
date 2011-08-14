package org.ivnbl.gametools.il2cod

import services.ui.AdminModule
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import com.vaadin.ui.{Panel, TabSheet, VerticalLayout}

class ScrollableTabSheet(modules:List[AdminModule]) extends VerticalLayout {
  private val log = LoggerFactory.getLogger(classOf[ScrollableTabSheet])
  private val tabSheet = new TabSheet()

  for (module <- modules) {
    addTab(module)
  }
  addComponent(tabSheet)

  def addTab(module: AdminModule) {
    log.debug("Adding tab \"{}\"", module.name)
    val panel = new Panel()
    panel.addComponent(module.buildComponent())
    tabSheet.addTab(panel, module.name, module.icon)
  }

  def removeTab(caption: String) {
    tabSheet.getComponentIterator.toIterable.map(c => tabSheet.getTab(c)).filterNot(_.getCaption == caption)
  }

}