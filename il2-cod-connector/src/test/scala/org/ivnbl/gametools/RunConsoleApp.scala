package org.ivnbl.gametools

import il2cod.impl.Connector
import java.util.Scanner
import org.slf4j.LoggerFactory
import actors.Actor

/**
 * @author ${user.name}
 */
object RunConsoleApp {
  private val log = LoggerFactory.getLogger(RunConsoleApp.getClass)

  def main(args: Array[String]) {
    val connector = new Connector("127.0.0.1", 20000)
    connector.start()

    val inputScanner = new InputScanner(connector)
    inputScanner.start()
    while (inputScanner.getState != Actor.State.Terminated) {
      Thread.sleep(100)
    }
    connector.stop = true
  }

  class OutputActor extends Actor {
    def act() {
      loop {
        react {
          case line: String => {
            log.debug("Line received: {}", line)
          }
          case Some(x) => log.warn("Nothing {}", x)
        }
      }
    }
  }

  private class InputScanner(conn: Connector) extends Actor {
    def act() {
      val scanner: Scanner = new Scanner(System.in)
      var emptyLines = 0
      while (emptyLines < 2) {
        val cmd = scanner.nextLine
        if (cmd.trim.isEmpty) {
          emptyLines += 1
        } else {
          emptyLines = 0
        }
        conn !? cmd match {
          case line: TraversableOnce[String] => {
            line.foreach(log.debug(_))
          }
        }
      }
      log.info("Finished")
    }
  }

}
