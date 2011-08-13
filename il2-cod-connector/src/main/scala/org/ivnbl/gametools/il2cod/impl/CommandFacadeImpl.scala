package org.ivnbl.gametools.il2cod.impl

import org.ivnbl.gametools.il2cod.{Command, CommandFacade}

class CommandFacadeImpl(val connector:Connector) extends CommandFacade {
  def act() {
    loop {
      react {
        case Command(commandName, params) => {
          commandName match {
            case "help" => {
              val charArray:Array[Char] = "Do you really need help? :)".toCharArray
              reply(charArray.toStream)
            }
            case _ => {
              connector ! commandName //todo
            }
          }
        }
      }
    }
  }
}