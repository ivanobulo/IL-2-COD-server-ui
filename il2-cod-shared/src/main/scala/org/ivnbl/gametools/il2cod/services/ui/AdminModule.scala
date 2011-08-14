package org.ivnbl.gametools.il2cod.services.ui

import com.vaadin.ui.Component
import com.vaadin.terminal.Resource

trait AdminModule {
  def name: String

  def buildComponent(): Component

  def icon:Resource = null
}