package org.ivnbl.gametools.il2cod

import org.apache.commons.net.telnet.TelnetClient
import org.slf4j.LoggerFactory
import scala.collection.mutable.Queue
import java.io._

class Connector(hostname: String, port: Int) extends Runnable {
  var commandTimeout = 10000
  private val log = LoggerFactory.getLogger(classOf[Connector])
  private val commandPool = new Queue[QueuedCommand]()

  def sendCommand(cmd: String, observer: CommandResponseObserver) {
    commandPool += new QueuedCommand(cmd, observer)
  }

  override def run() {
    TelnetSession.connect()

    log.info("Connected to {}:{}", hostname, port)

    //Read output
    try {
      def runCondition = {
        !Thread.currentThread.isInterrupted && TelnetSession.isConnected
      }
      while (runCondition) {
        readNextBlock(() => runCondition)
        OutputObserver.checkForQueuedCommands()
        //Wait for the next portion of data
        Thread.sleep(100)
      }
    } catch {
      case ie: InterruptedException => log.info("Interrupted")
    } finally {
      try {
        if (TelnetSession.isConnected) TelnetSession.disconnect()
      } catch {
        case e: Exception => log.error("Error closing telnet", e)
      }
    }
  }

  private object OutputObserver {
    var currentCommand: QueuedCommand = null

    def receivedLine(msg: String) {
      if (null == currentCommand) {
        //log
        LoggingCallback.receiveLine(msg)
      } else {
        //notify command observer
        currentCommand.observer.receiveLine(msg)
      }
    }

    def endOfOutput() {
      if (null != currentCommand) {
        log.debug("No more response from command '{}'", currentCommand.cmd)
        currentCommand.observer.noMoreMessages()
        currentCommand = null
      }
      checkForQueuedCommands()
    }

    def checkForQueuedCommands() {
      if (currentCommand != null) {
        return
      }
      //Execute command from queue if there is one
      if (!commandPool.isEmpty) {
        currentCommand = commandPool.dequeue()
        executeCommand(currentCommand)
      }
    }

    private def executeCommand(queuedCommand: QueuedCommand) {
      TelnetSession.writer.write(queuedCommand.cmd)
      TelnetSession.writer.write('\n')
      TelnetSession.writer.flush()
    }
  }

  private class QueuedCommand(var cmd: String, var observer: CommandResponseObserver) {
  }

  private object TelnetSession {
    private val telnetClient = new TelnetClient()
    var reader: BufferedReader = null
    var writer: PrintWriter = null

    def connect() {
      telnetClient.connect(hostname, port)
      reader = new LineNumberReader(new InputStreamReader(telnetClient.getInputStream))
      writer = new PrintWriter(telnetClient.getOutputStream)
    }

    def disconnect() {
      if (isConnected) telnetClient.disconnect()
    }

    def isConnected = telnetClient.isConnected
  }

  private def readNextBlock(runCondition: () => Boolean) {
    var endOfMessage = false

    while (TelnetSession.reader.ready && runCondition() && !endOfMessage) {
      var line: String = TelnetSession.reader.readLine
      if (line == null) {
        Thread.sleep(100)
      } else {
        if (0 <= line.indexOf("<consoleN>")) {
          OutputObserver.endOfOutput()
          endOfMessage = true
        } else if (!line.trim.isEmpty) {
          OutputObserver.receivedLine(line)
        }
      }
    }
  }

  object LoggingCallback extends CommandResponseObserver {
    def receiveLine(msg: String) {
      log.debug("Received: {}", msg)
    }
  }
}

trait CommandResponseObserver {
  def receiveLine(msg: String)

  def noMoreMessages() {}
}
