
package app.services;

import cx.ath.mancel01.webframework.security.LoginModule;

public class ${classname} implements LoginModule {

    public boolean authenticate(String username, String password) {
        return true;
    }

    public String defaultCallbackURL() {
        return "/";
    }

    public String authenticationFailURL() {
        return "http://www.google.com";
    }

    public String logoutURL() {
        return "/security/loginpage";
    }

}
