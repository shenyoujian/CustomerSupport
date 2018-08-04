package pojo;

/**
 * @Author ljs
 * @Description TODO
 * @Date 2018/8/3 23:05
 **/
public class Attachment {

    private String name;

    private byte[] contents;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }
}
