package com.sansara.develop.innocrypt;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.sansara.develop.innocrypt.data.FriendDB;
import com.sansara.develop.innocrypt.model.Friend;
import com.sansara.develop.innocrypt.ui.LoginActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FriendDbTest {
    private FriendDB friendDB;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getTargetContext();
        friendDB = FriendDB.getInstance(context);
    }

    @Test
    public void cachedListFriendsNotEmpty() {
        ArrayList<Friend> friends = friendDB.getListFriend().getListFriend();
        Log.d("MY_TEST", friends.toString());
        assertNotEquals(null, friends);
    }
}
