package servlet;

import pojo.Attachment;
import pojo.Ticket;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author ljs
 * @Description TODO
 * @Date 2018/8/4 3:14
 **/
@WebServlet(
        name = "ticketServlet",
        urlPatterns = {"/tickets"},                    //servlet映射
        loadOnStartup = 1                              //表明该servlet在web容器初始化就
                                                       //就加载而不是在第一次请求的时候再加载
)
@MultipartConfig(                                      //告诉web容器该servlet提供文件上传的支持
        fileSizeThreshold = 5_242_880, //5MB           //小于5mb的将文件保存到内存中，请求结束，垃圾回收器回收
        maxFileSize = 20_971_520L, //20MB              //不能上传超过20mb的文件，20以下5以上的保存到web容器指定的目录中
        maxRequestSize = 41_943_040L //40MB            //不能超过40mb的请求
)
public class TicketServlet extends HttpServlet
{
    private volatile int TICKET_ID_SEQUENCE = 1;

    private Map<Integer, Ticket> ticketDatabase = new LinkedHashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String action = request.getParameter("action");
        if(action == null)
            action = "list";
        switch(action)
        {
            case "create":
                this.showTicketForm(request, response);
                break;
            case "view":
                this.viewTicket(request, response);
                break;
            case "download":
                this.downloadAttachment(request, response);
                break;
            case "list":
            default:
                this.listTickets(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String action = request.getParameter("action");
        if(action == null)
            action = "list";
        switch(action)
        {
            case "create":
                this.createTicket(request, response);
                break;
            case "list":
            default:
                response.sendRedirect("tickets");
                break;
        }
    }

    private void showTicketForm(HttpServletRequest request,
                                HttpServletResponse response)
            throws ServletException, IOException
    {
        request.getRequestDispatcher("/WEB-INF/jsp/view/ticketForm.jsp")
                .forward(request, response);
    }

    private void viewTicket(HttpServletRequest request,
                            HttpServletResponse response)
            throws ServletException, IOException
    {
        String idString = request.getParameter("ticketId");
        Ticket ticket = this.getTicket(idString, response);
        if(ticket == null)
            return;

        request.setAttribute("ticketId", idString);
        request.setAttribute("ticket", ticket);

        request.getRequestDispatcher("/WEB-INF/jsp/view/viewTicket.jsp")
                .forward(request, response);
    }

    private void downloadAttachment(HttpServletRequest request,
                                    HttpServletResponse response)
            throws ServletException, IOException
    {
        String idString = request.getParameter("ticketId");
        Ticket ticket = this.getTicket(idString, response);
        if(ticket == null)
            return;

        String name = request.getParameter("attachment");
        if(name == null)
        {
            response.sendRedirect("tickets?action=view&ticketId=" + idString);
            return;
        }

        Attachment attachment = ticket.getAttachment(name);
        if(attachment == null)
        {
            response.sendRedirect("tickets?action=view&ticketId=" + idString);
            return;
        }

        response.setHeader("Content-Disposition",
                "attachment; filename=" + attachment.getName());
        response.setContentType("application/octet-stream");

        ServletOutputStream stream = response.getOutputStream();
        stream.write(attachment.getContents());
    }

    private void listTickets(HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setAttribute("ticketDatabase", this.ticketDatabase);

        request.getRequestDispatcher("/WEB-INF/jsp/view/listTickets.jsp")
                .forward(request, response);
    }

    private void createTicket(HttpServletRequest request,
                              HttpServletResponse response)
            throws ServletException, IOException
    {
        Ticket ticket = new Ticket();
        ticket.setCustomerName(request.getParameter("customerName"));
        ticket.setSubject(request.getParameter("subject"));
        ticket.setBody(request.getParameter("body"));

        Part filePart = request.getPart("file1");
        if(filePart != null && filePart.getSize() > 0)
        {
            Attachment attachment = this.processAttachment(filePart);
            if(attachment != null)
                ticket.addAttachment(attachment);
        }

        int id;
        synchronized(this)
        {
            id = this.TICKET_ID_SEQUENCE++;
            this.ticketDatabase.put(id, ticket);
        }

        response.sendRedirect("tickets?action=view&ticketId=" + id);
    }

    private Attachment processAttachment(Part filePart)
            throws IOException
    {
        InputStream inputStream = filePart.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int read;
        final byte[] bytes = new byte[1024];

        while((read = inputStream.read(bytes)) != -1)
        {
            outputStream.write(bytes, 0, read);
        }

        Attachment attachment = new Attachment();
        attachment.setName(filePart.getSubmittedFileName());
        attachment.setContents(outputStream.toByteArray());

        return attachment;
    }

    private Ticket getTicket(String idString, HttpServletResponse response)
            throws ServletException, IOException
    {
        if(idString == null || idString.length() == 0)
        {
            response.sendRedirect("tickets");
            return null;
        }

        try
        {
            Ticket ticket = this.ticketDatabase.get(Integer.parseInt(idString));
            if(ticket == null)
            {
                response.sendRedirect("tickets");
                return null;
            }
            return ticket;
        }
        catch(Exception e)
        {
            response.sendRedirect("tickets");
            return null;
        }
    }
}

