package app.binder;

import app.controller.*;
import cx.ath.mancel01.webframework.integration.dependencyshot.WebBinder;

public class AppBinder extends WebBinder {

    @Override
    public void configureBindings() {
        registerController(Todo.class);
    }
}
