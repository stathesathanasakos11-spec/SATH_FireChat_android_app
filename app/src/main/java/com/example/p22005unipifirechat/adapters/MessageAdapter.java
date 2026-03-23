package com.example.p22005unipifirechat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.p22005unipifirechat.interfaces.IMessageActionListener;
import com.example.p22005unipifirechat.modelclasses.Message;
import com.example.p22005unipifirechat.R;
import com.example.p22005unipifirechat.utils.AvatarUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<Message> mChat;
    private String imageUrl;
    // use an interface to communicate with the activity
    private IMessageActionListener listener;
    FirebaseUser fuser;

    public MessageAdapter(Context context, List<Message> mChat, String imageUrl, IMessageActionListener listener){
        this.context = context;
        this.mChat = mChat;
        this.imageUrl = imageUrl;
        this.listener = listener;
    }


    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // decide which xml file to inflate based on the UID of the sender
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
            return new ViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Message chat = mChat.get(position);
        holder.show_message.setText(chat.messageText);

        if (getItemViewType(position) == MSG_TYPE_LEFT && holder.imgChatAvatar != null) {
            // use the AvatarUtils class to set user's avatar
            // next to his message layout
            holder.imgChatAvatar.setVisibility(View.VISIBLE);
            AvatarUtils.setAvatar(holder.imgChatAvatar, imageUrl);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onMessageLongClick(chat);
                }
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return mChat.size();
    }




    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView show_message;
        public ImageView imgChatAvatar;

        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.tvMessage);
            imgChatAvatar = itemView.findViewById(R.id.imgChatAvatar);
        }
    }


    // this method is used to decide which xml file to inflate
    //it is called whenever the adapter needs to create a new ViewHolder (while scrolling)
    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        //check if the message is from the current user or not
        if (fuser != null && mChat.get(position).senderId.equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}