package pojo;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author ljs
 * @Description TODO
 * @Date 2018/8/3 22:56
 **/
public class Ticket {
    private String customerName;        //票据名

    private String subject;             //票类型

    private String body;

    private Map<String, Attachment> attachments = new LinkedHashMap<>();    //和附件是一对多

    public Attachment getAttachment(String name)
    {
        return this.attachments.get(name);
    }

    public Collection<Attachment> getAttachments()
    {
        return this.attachments.values();
    }

    public void addAttachment(Attachment attachment)
    {
        this.attachments.put(attachment.getName(), attachment);
    }

    public int getNumberOfAttachments()
    {
        return this.attachments.size();
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
