package com.warmplace.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() {
        super("이미 사용 중인 사용자 이름입니다");
    }
}
