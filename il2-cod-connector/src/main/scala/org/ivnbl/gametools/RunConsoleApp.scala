package org.ivnbl.gametools

import il2cod.{CommandResponseObserver, Connector}
import java.util.Scanner
import org.slf4j.LoggerFactory

/**
 * @author ${user.name}
 */
object RunConsoleApp {
  private val log = LoggerFactory.getLogger(RunConsoleApp.getClass)
  
  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)
  
  def main(args : Array[String]) {
    val connector = new Connector("127.0.0.1", 20000)
    val thread = new Thread(connector)
    thread.start()

    val scanner = new InputScanner(connector)
    scanner.start()

    scanner.join()
    thread.interrupt()
  }


  private class InputScanner(conn:Connector) extends Thread {
    override def run() {
      val scanner: Scanner = new Scanner(System.in)
      var stop = 0
      while (stop % 2 == 0) {
        val cmd = scanner.nextLine
        if (cmd.trim.isEmpty) stop += 1
        conn.sendCommand(cmd, new CommandResponseObserver {
          def receiveMsg(msg: String) {
            log.debug("Command '{}' response: {}", cmd, msg)
          }
        })
      }
    }
  }
}
