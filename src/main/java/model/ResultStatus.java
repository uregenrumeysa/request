package model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultStatus {
    private boolean result;
    private String message;
    private Object objects;
}
