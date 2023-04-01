package com.webinar.common;

/**
 * @ProjectName RestApiResponse
 * @author Webinar
 * @version 1.0.0
 * @Description 附件分片上传
 * @createTime 2023/3/29 0013 15:59
 */
public class RestApiResponse<T> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 响应数据
     */
    private T data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> RestApiResponse<T> success(T data) {
        RestApiResponse<T> result = new RestApiResponse<>();
        result.success = true;
        result.data = data;
        return result;
    }

    public static <T> RestApiResponse<T> success() {
        RestApiResponse<T> result = new RestApiResponse<>();
        result.success = true;
        return result;
    }

    public static <T> RestApiResponse<T> error(T data) {
        RestApiResponse<T> result = new RestApiResponse<>();
        result.success = false;
        result.data = data;
        return result;
    }

    public static <T> RestApiResponse<T> flag(boolean data) {
        RestApiResponse<T> result = new RestApiResponse<>();
        result.success = data;
        return result;
    }

}

