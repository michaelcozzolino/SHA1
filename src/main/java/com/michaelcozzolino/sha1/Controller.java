package com.michaelcozzolino.sha1;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.util.LinkedHashMap;
import java.util.Map;

public class Controller{
    @FXML
    private TextArea stringToBeHashed;

    @FXML
    private TextArea binaryHashedString;

    @FXML
    private TextArea hexadecimalHashedString;

    private Sha1 sha1;


    public void initialize() {
        sha1 = new Sha1();
        waitForInput();
    }


    private void waitForInput() {
        stringToBeHashed.textProperty().addListener((observableValue, oldText, newText) -> {
            Map<String,String> hashedString = new LinkedHashMap<>();
            hashedString = this.sha1.compute(newText);
            this.binaryHashedString.setText(hashedString.get("BINARY"));
            this.hexadecimalHashedString.setText(hashedString.get("HEXADECIMAL"));
        });
    }

}
