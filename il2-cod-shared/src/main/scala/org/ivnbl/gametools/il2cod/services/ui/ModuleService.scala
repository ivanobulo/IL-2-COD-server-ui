package org.ivnbl.gametools.il2cod.services.ui

trait ModuleService {
  def adminModules: List[AdminModule]

  def addListener(listener: ModuleServiceListener)

  def removeListener(listener: ModuleServiceListener)
}