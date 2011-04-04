package org.ivnbl.gametools.il2cod

import org.apache.commons.net.telnet.TelnetClient
import org.slf4j.LoggerFactory
import scala.collection.mutable.Queue
import java.io._

class Connector(hostname: String, port: Int) extends Runnable {
  var observer: CommandResponseObserver = LoggingCallback
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
        readNextBlock(TelnetSession.reader, observer, () => runCondition)

        //Execute command if there is one in the queue
        if (!commandPool.isEmpty) {
          executeCommand(() => runCondition, commandPool.dequeue())
        }
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

  private def executeCommand(runCondition: () => Boolean, queuedCommand:QueuedCommand) {
    val observer = new DelegateToObserver
    observer.setDelegateObserver(queuedCommand.observer)
    //Execute command
    TelnetSession.writer.write(queuedCommand.cmd)
    TelnetSession.writer.write('\n')
    TelnetSession.writer.flush()
    //Read response
    var waitTime = 0
    def waitTooLong = waitTime > commandTimeout
    while (runCondition() && !observer.noMoreMessagesReceived && !waitTooLong) {
      readNextBlock(TelnetSession.reader, observer, runCondition)
      //Wait for the next portion of data
      Thread.sleep(100)
      waitTime += 100
    }
    log.debug("No more response from command '{}'", queuedCommand.cmd)
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

  private def readNextBlock(reader: BufferedReader, receiver: CommandResponseObserver,
                            runCondition: () => Boolean, waitInterval: Int = 100) {
    var endOfMessage = false

    while (reader.ready && runCondition() && !endOfMessage) {
      var line: String = reader.readLine
      if (line == null) {
        Thread.sleep(waitInterval)
      } else {
        if (0 <= line.indexOf("<consoleN>")) {
          receiver.noMoreMessages()
          endOfMessage = true
        } else if (!line.trim.isEmpty) {
          receiver.receiveMsg(line)
        }
      }
    }
  }

  object LoggingCallback extends CommandResponseObserver {
    def receiveMsg(msg: String) {
      log.debug("Received: {}", msg)
    }
  }

  class DelegateToObserver extends CommandResponseObserver {
    var noMoreMessagesReceived = false
    private var delegate: CommandResponseObserver = null

    def setDelegateObserver(delegate: CommandResponseObserver) {
      this.delegate = delegate
    }

    def receiveMsg(msg: String) {
      if (null != delegate) {
        delegate.receiveMsg(msg)
      }
    }

    override def noMoreMessages() {
      if (null != delegate) {
        delegate.noMoreMessages()
      }
      noMoreMessagesReceived = true
    }
  }

}

trait CommandResponseObserver {
  def receiveMsg(msg: String)

  def noMoreMessages() {}
}
