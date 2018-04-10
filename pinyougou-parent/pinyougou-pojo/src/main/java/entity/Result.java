package entity;

import java.io.Serializable;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package entity
 * @company www.itheima.com
 */
public class Result implements Serializable{
    private boolean success;
    private String message;
    public Result(boolean success, String message) {
        super();
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
