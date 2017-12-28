package com.likethatalsocan.whodat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by sathy on 28/12/17.
 */

public class ChatListRecycleAdapter extends RecyclerView.Adapter {

    private static int MESSAGE_TYPE_MINE = 1;
    private static int MESSAGE_TYPE_FRIEND = 2;

    private LayoutInflater inflater;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String accountId;
    private String toId;
    private boolean anonymous;


    private List<Message> messages = new ArrayList<>();

    public ChatListRecycleAdapter(Context context, String accountId, String toId, boolean anonymous) {
        inflater = LayoutInflater.from(context);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("development");
        mAuth = FirebaseAuth.getInstance();
        this.accountId = accountId;
        this.toId = toId;
        this.anonymous = anonymous;

        getMessages();
    }

    private void getMessages() {

        String myUid = mAuth.getCurrentUser().getUid();

        ChildEventListener listener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String messageId = dataSnapshot.getKey();
                mDatabase.child("messages").child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Message message = dataSnapshot.getValue(Message.class);
                        message.setId(messageId);
                        messages.add(message);

                        reloadData();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
        };

        mDatabase.child("user-messages").child(myUid).child(accountId).addChildEventListener(listener);



    }

    private void reloadData() {

        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message message, Message t1) {
                return message.getTimestamp().intValue() > t1.getTimestamp().intValue() ? -1 : 1;
            }
        });

        notifyDataSetChanged();

        resetReadCount();
    }

    private void resetReadCount() {

        String myUid = mAuth.getCurrentUser().getUid();
        mDatabase.child("last-user-message-read").child(myUid).child(accountId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    dataSnapshot.getRef().setValue(0);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}});
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == MESSAGE_TYPE_MINE) {
            View view = inflater.inflate(R.layout.view_my_message, parent, false);
            MyChatViewHolder chatViewHolder = new MyChatViewHolder(view);

            return chatViewHolder;
        } else {
            View view = inflater.inflate(R.layout.view_friend_message, parent, false);
            FriendChatViewHolder chatViewHolder = new FriendChatViewHolder(view);

            return chatViewHolder;

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            Message message = messages.get(position);

            if(isMine(position)) {
                ((MyChatViewHolder) holder).getMessageTextView().setText(message.getMessage());
            } else {
                ((FriendChatViewHolder) holder).getMessageTextView().setText(message.getMessage());
            }
    }

    @Override
    public int getItemViewType(int position) {


         return isMine(position) ? MESSAGE_TYPE_MINE : MESSAGE_TYPE_FRIEND ;
    }

    private boolean isMine(int position) {
        String myUid = mAuth.getCurrentUser().getUid();
        return (anonymous && messages.get(position).getFromId().equals(myUid))
                || (!anonymous && messages.get(position).getFromId().equals(toId));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MyChatViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.myMessageView);

        }

        public TextView getMessageTextView() {
            return messageTextView;
        }

        public void setMessageTextView(TextView messageTextView) {
            this.messageTextView = messageTextView;
        }
    }

    class FriendChatViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView;

        public FriendChatViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.friendMessageView);

        }

        public TextView getMessageTextView() {
            return messageTextView;
        }

        public void setMessageTextView(TextView messageTextView) {
            this.messageTextView = messageTextView;
        }
    }

    public void setToId(String toId) {
        this.toId = toId;
    }
}
