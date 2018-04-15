package com.sansara.develop.innocrypt.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sansara.develop.innocrypt.R;
import com.sansara.develop.innocrypt.data.SharedPreferenceHelper;
import com.sansara.develop.innocrypt.data.StaticConfig;
import com.sansara.develop.innocrypt.model.Consersation;
import com.sansara.develop.innocrypt.model.Message;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mRecyclerViewChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private RecyclerViewAdapterMsg mRecyclerViewAdapterMsg;
    private String mRoomId;

    private ArrayList<CharSequence> mFriendId;
    private Consersation mConsersation;
    private EditText mEditWriteMessage;
    private ImageButton mButtonSend;
    public static HashMap<String, Bitmap> sBitmapAvataFriend;
    public Bitmap mBitmapAvataUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intentData = getIntent();
        mFriendId = intentData.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        mRoomId = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
        String nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);

        mConsersation = new Consersation();
        mButtonSend = (ImageButton) findViewById(R.id.button_send);
        mButtonSend.setOnClickListener(this);

        String base64AvataUser = SharedPreferenceHelper.getInstance(this).getUserInfo().avata;
        if (!base64AvataUser.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            byte[] decodedString = Base64.decode(base64AvataUser, Base64.DEFAULT);
            mBitmapAvataUser = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } else {
            mBitmapAvataUser = null;
        }

        mEditWriteMessage = (EditText) findViewById(R.id.edit_write_msg);
        if (mFriendId != null && nameFriend != null) {
            getSupportActionBar().setTitle(nameFriend);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecyclerViewChat = (RecyclerView) findViewById(R.id.recycler_chat);
            mRecyclerViewChat.setLayoutManager(linearLayoutManager);
            mRecyclerViewAdapterMsg = new RecyclerViewAdapterMsg(this, mConsersation, sBitmapAvataFriend, mBitmapAvataUser);
            FirebaseDatabase.getInstance().getReference().child("message/" + mRoomId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                        Message newMessage = new Message();
                        newMessage.idSender = (String) mapMessage.get("idSender");
                        newMessage.idReceiver = (String) mapMessage.get("idReceiver");
                        newMessage.text = (String) mapMessage.get("text");
                        newMessage.timestamp = (long) mapMessage.get("timestamp");
                        mConsersation.getListMessageData().add(newMessage);
                        mRecyclerViewAdapterMsg.notifyDataSetChanged();
                        linearLayoutManager.scrollToPosition(mConsersation.getListMessageData().size() - 1);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mRecyclerViewChat.setAdapter(mRecyclerViewAdapterMsg);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent result = new Intent();
            result.putExtra("idFriend", mFriendId.get(0));
            setResult(RESULT_OK, result);
            this.finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra("idFriend", mFriendId.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_send) {
            String content = mEditWriteMessage.getText().toString().trim();
            if (content.length() > 0) {
                mEditWriteMessage.setText("");
                Message newMessage = new Message();
                newMessage.text = content;
                newMessage.idSender = StaticConfig.UID;
                newMessage.idReceiver = mRoomId;
                newMessage.timestamp = System.currentTimeMillis();
                FirebaseDatabase.getInstance().getReference().child("message/" + mRoomId).push().setValue(newMessage);
            }
        }
    }
}

class RecyclerViewAdapterMsg extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private Consersation mConsersation;
    private HashMap<String, Bitmap> mBitmapAvata;
    private HashMap<String, DatabaseReference> mBitmapAvataReference;
    private Bitmap mBitmapAvataUser;

    public RecyclerViewAdapterMsg(Context context, Consersation consersation, HashMap<String, Bitmap> bitmapAvata, Bitmap bitmapAvataUser) {
        this.mContext = context;
        this.mConsersation = consersation;
        this.mBitmapAvata = bitmapAvata;
        this.mBitmapAvataUser = bitmapAvataUser;
        mBitmapAvataReference = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == ChatActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemMessageFriendHolder) {
            ((ItemMessageFriendHolder) holder).mTextMessage.setText(mConsersation.getListMessageData().get(position).text);
            Bitmap currentAvata = mBitmapAvata.get(mConsersation.getListMessageData().get(position).idSender);
            if (currentAvata != null) {
                ((ItemMessageFriendHolder) holder).mIconAvata.setImageBitmap(currentAvata);
            } else {
                final String id = mConsersation.getListMessageData().get(position).idSender;
                if(mBitmapAvataReference.get(id) == null){
                    mBitmapAvataReference.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/avata"));
                    mBitmapAvataReference.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                String avataStr = (String) dataSnapshot.getValue();
                                if(!avataStr.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                                    byte[] decodedString = Base64.decode(avataStr, Base64.DEFAULT);
                                    ChatActivity.sBitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                }else{
                                    ChatActivity.sBitmapAvataFriend.put(id, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_avata));
                                }
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        } else if (holder instanceof ItemMessageUserHolder) {
            ((ItemMessageUserHolder) holder).mTextMessage.setText(mConsersation.getListMessageData().get(position).text);
            if (mBitmapAvataUser != null) {
                ((ItemMessageUserHolder) holder).mIconAvata.setImageBitmap(mBitmapAvataUser);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mConsersation.getListMessageData().get(position).idSender.equals(StaticConfig.UID) ? ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return mConsersation.getListMessageData().size();
    }
}

class ItemMessageUserHolder extends RecyclerView.ViewHolder {
    public TextView mTextMessage;
    public CircleImageView mIconAvata;

    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        mTextMessage = (TextView) itemView.findViewById(R.id.text_message);
        mIconAvata = (CircleImageView) itemView.findViewById(R.id.icon_avata);
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public TextView mTextMessage;
    public CircleImageView mIconAvata;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        mTextMessage = (TextView) itemView.findViewById(R.id.text_message);
        mIconAvata = (CircleImageView) itemView.findViewById(R.id.icon_avata);
    }
}
