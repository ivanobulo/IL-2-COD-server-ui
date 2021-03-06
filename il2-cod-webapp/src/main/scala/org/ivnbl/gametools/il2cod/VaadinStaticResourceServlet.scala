package org.ivnbl.gametools.il2cod

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import java.util.{Calendar, Date}
import java.io.{InputStream, OutputStream}
import org.slf4j.LoggerFactory
import javax.servlet.ServletConfig

class VaadinStaticResourceServlet extends HttpServlet {
  val log = LoggerFactory.getLogger(getClass)

  var servletInitDate: Date = null

  override def init() {
    super.init()
    servletInitDate = new Date()
  }

  override def init(servletConfig: ServletConfig) {
    super.init(servletConfig)
    log.info("Servlet Name: {}", servletConfig.getServletName)
  }


  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    //Expire all cached resources in one hour
    val expiredCalendar = Calendar.getInstance
    expiredCalendar.add(Calendar.HOUR, 1) //let it be cached by the browser for one hour

    //Caching headers
    resp.addDateHeader("Last-Modified", servletInitDate.getTime)
    resp.addDateHeader("Expires", expiredCalendar.getTime.getTime)

    req.getDateHeader("If-Modified-Since") match {
      case since if (new Date(since).after(servletInitDate)) => {
        resp.sendError(HttpServletResponse.SC_NOT_MODIFIED)
      }
      case _ => streamRequestedResource(req,resp)
    }
  }

  private def streamRequestedResource(req: HttpServletRequest, resp: HttpServletResponse) {
    //Use Vaadin bundle class loader to provide static resources
    val pathToResource = "/VAADIN" + req.getPathInfo
    val resourceInStream = classOf[com.vaadin.Application].getResourceAsStream(pathToResource)
    try {
      if (null == resourceInStream) {
        //No such resource
        resp.sendError(HttpServletResponse.SC_NOT_FOUND)
        return
      }
      streamResource(resp.getOutputStream, resourceInStream)
    } finally {
      if (null != resourceInStream) resourceInStream.close()
    }
  }

  private def streamResource(out: OutputStream, in: InputStream) {
    Stream.continually(in.read).takeWhile(-1 != _).foreach(out.write(_))
  }
}
