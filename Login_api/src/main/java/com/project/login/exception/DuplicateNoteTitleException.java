package com.project.login.exception;

/**
 * 笔记标题在同一笔记本下重复时抛出的业务异常
 * 用于与普通运行时错误区分开，方便前端识别并给出友好提示
 */
public class DuplicateNoteTitleException extends RuntimeException {

    public DuplicateNoteTitleException(String message) {
        super(message);
    }
}

