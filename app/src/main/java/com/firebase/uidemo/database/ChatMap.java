package com.firebase.uidemo.database;

import com.google.firebase.database.FirebaseDatabase;

import br.com.firebase.ui.databinding.FirebaseArrayMap;

/**
 * Created by ventura on 1/20/17.
 */

public class ChatMap extends FirebaseArrayMap<String, ChatActivity.Chat> {

    public ChatMap() {
        super(FirebaseDatabase.getInstance().getReference().child("chats2"));
    }

    @Override
    public Class<ChatActivity.Chat> getType() {
        return ChatActivity.Chat.class;
    }

}
