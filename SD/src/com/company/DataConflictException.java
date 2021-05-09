package com.company;

public class DataConflictException extends Exception{
    public static class DuplicatedUsername extends Exception{
    }
    public static class DuplicatedNumero_CC extends Exception{
    }
    public static class InvalidType extends Exception{
    }
}
