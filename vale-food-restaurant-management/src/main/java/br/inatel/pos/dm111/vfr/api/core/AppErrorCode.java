package br.inatel.pos.dm111.vfr.api.core;

public enum AppErrorCode {

    CONFLICTED_USER_EMAIL("user.email.conflicted", "Provided email is already in use!", 409),
    USER_NOT_FOUND("user.not.found", "User was not found.", 404),
    RESTAURANT_NOT_FOUND("restaurant.not.found", "Restaurant was not found.", 404),
    INVALID_USER_TYPE("user.invalid.type", "Provided user is not supported for the current operation.", 403),
    INVALID_USER_CREDENTIALS("user.invalid.credentials", "Provided credentials are not valid!", 401),
    OPERATION_NOT_SUPPORTED("operation.not.supported", "Operation not supported by the given user type.", 403),
    INTERNAL_DATABASE_COMMUNICATION_ERROR("internal.error", "Failure to communicate with repository.", 500);



    private String code;
    private String message;
    private int status;

    AppErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
