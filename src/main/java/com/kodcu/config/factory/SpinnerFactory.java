package com.kodcu.config.factory;

import com.dooapp.fxform.view.FXFormNode;
import com.dooapp.fxform.view.FXFormNodeWrapper;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Spinner;
import javafx.util.Callback;

/**
 * Created by usta on 17.07.2015.
 */
public class SpinnerFactory<T> implements Callback<Void, FXFormNode> {

    private final Spinner<T> spinner;
    private final ObjectProperty<T> objectProperty = new SimpleObjectProperty<>();

    public SpinnerFactory(Spinner<T> spinner) {
        this.spinner = spinner;
        this.spinner.setEditable(true);
    }

    @Override
    public FXFormNode call(Void param) {
        return new FXFormNodeWrapper(spinner, spinner.getValueFactory().valueProperty());
    }
}
