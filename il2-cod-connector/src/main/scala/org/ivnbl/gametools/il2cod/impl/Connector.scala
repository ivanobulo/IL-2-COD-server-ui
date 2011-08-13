package org.ivnbl.gametools.il2cod.impl

import org.apache.commons.net.telnet.TelnetClient
import org.slf4j.LoggerFactory
import java.io._
import actors.Actor

class Connector(hostname: String, port: Int, val commandTimeout: Long = 10000) extends Actor {

  private val log = LoggerFactory.getLogger(getClass)
  var stop = false;

  def act() {
    TelnetSession.connect()
    log.info("Connected to {}:{}", hostname, port)
    try {
      while (!stop) {
        receiveWithin(100) {
          case cmd: String => {
            //Execute the command
            TelnetSession.executeCommand(cmd);
            //Read output and return to the sender
            sender ! TelnetSession.readNextBlock()
          }
          case _ => {
            //TODO Need to notify someone
            //Read whatever received from the telnet
            TelnetSession.readNextBlock().foreach(log.debug("Received: {}", _))
          }
        } //end of receive
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

  private object TelnetSession {
    private val telnetClient = new TelnetClient()
    private var reader: BufferedReader = null
    private var writer: PrintWriter = null

    def connect() {
      telnetClient.connect(hostname, port)
      reader = new LineNumberReader(new InputStreamReader(telnetClient.getInputStream))
      writer = new PrintWriter(telnetClient.getOutputStream)
    }

    def disconnect() {
      if (isConnected) telnetClient.disconnect()
    }

    def isConnected = telnetClient.isConnected

    def executeCommand(cmd: String) {
      writer.write(cmd)
      writer.write('\n')
      writer.flush()
    }

    def readNextBlock(): List[String] = {
      readInner(Nil, 50, 100)
    }

    private def getReady(readyDelay: Int = 10, readyRetryCount: Int = 10): Boolean = {
      reader.ready() match {
        case true => true
        case false if (readyRetryCount > 0) => {
          Thread.sleep(readyDelay)
          getReady(readyDelay, readyRetryCount - 1)
        }
        case _ => false
      }
    }

    private def readInner(list: List[String], retryDelay: Int, retryCount: Int): List[String] = {
      if (!getReady()) {
        return list
      }
      reader.readLine match {
        case null => {
          //Retry for given number of times
          retryCount match {
            case 0 => list
            case n => {
              //Wait a little before the retry
              Thread.sleep(retryDelay)
              readInner(list, retryDelay, retryCount - 1)
            }
          }
        } // end of case null
        case line if (line.trim().isEmpty) => readInner(list, retryDelay, retryCount - 1)
        case line if (-1 != line.indexOf("<consoleN>")) => list
        case line => line :: readInner(list, retryDelay, retryCount)
      } //end of readLine match
    } //end of readInner
  }

}
