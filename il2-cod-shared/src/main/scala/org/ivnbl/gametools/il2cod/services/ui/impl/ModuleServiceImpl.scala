package org.ivnbl.gametools.il2cod.services.ui.impl

import org.ivnbl.gametools.il2cod.services.ui.{ModuleServiceListener, AdminModule, ModuleService}
import aQute.bnd.annotation.component.{Component, Reference}

/**
 * Module service implementation
 */
@Component(provide = Array[Class[_]](classOf[ModuleService]))
class ModuleServiceImpl extends ModuleService {
  private var moduleRegistrations: List[AdminModule] = Nil
  private var moduleListeners: List[ModuleServiceListener] = Nil

  def adminModules = moduleRegistrations

  @Reference(multiple = true, dynamic = true)
  def addAdminModule(adminModule: AdminModule) {
    this.synchronized {
      moduleRegistrations = moduleRegistrations ::: List(adminModule)
    }
    //Notify listeners
    moduleListeners.foreach(_.moduleRegistered(this, adminModule))
  }

  def removeAdminModule(adminModule: AdminModule) {
    this.synchronized {
      moduleRegistrations = moduleRegistrations.filterNot(_ == adminModule)
    }
    //Notify listeners
    moduleListeners.foreach(_.moduleUnregistered(this, adminModule))
  }

  def addListener(listener: ModuleServiceListener) {
    this.synchronized {
      moduleListeners = moduleListeners ::: List(listener)
    }
  }

  def removeListener(listener: ModuleServiceListener) {
    this.synchronized {
      moduleListeners = moduleListeners.filterNot(_ == listener)
    }
  }
}