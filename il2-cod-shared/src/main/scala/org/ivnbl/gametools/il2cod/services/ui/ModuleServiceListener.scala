package org.ivnbl.gametools.il2cod.services.ui

trait ModuleServiceListener {
  def moduleRegistered(service: ModuleService, adminModule: AdminModule)

  def moduleUnregistered(service: ModuleService, adminModule: AdminModule)
}