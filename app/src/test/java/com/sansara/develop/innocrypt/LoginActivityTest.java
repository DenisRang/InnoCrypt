package com.sansara.develop.innocrypt;

import com.sansara.develop.innocrypt.ui.LoginActivity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class LoginActivityTest {
    private LoginActivity loginActivity;

    @Before
    public void setUp(){
        loginActivity=new LoginActivity();
    }

    @Test
    public void validEmailAndPasswordPassed() throws Exception {
        assertTrue(loginActivity.validate("asdasd@gmail.com","1"));
    }

    @Test
    public void invalidEmailOrPasswordFails() throws Exception {
        assertFalse(loginActivity.validate("asdasdgmail.com","1"));
    }
}