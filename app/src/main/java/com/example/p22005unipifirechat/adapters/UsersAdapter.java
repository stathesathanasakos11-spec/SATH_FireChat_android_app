package com.example.p22005unipifirechat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.p22005unipifirechat.interfaces.IUsersActionListener;
import com.example.p22005unipifirechat.utils.AvatarUtils;
import com.example.p22005unipifirechat.R;
import com.example.p22005unipifirechat.modelclasses.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private IUsersActionListener listener;

    public UsersAdapter(Context context, List<User> userList, IUsersActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = userList.get(position);

        holder.tvUsername.setText(currentUser.username);
        AvatarUtils.setAvatar(holder.imgUserAvatar, currentUser.imageUrl);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onUserClicked(currentUser);
                }
            }
        });

        holder.btnDeleteChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteChatClicked(currentUser);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername;
        public ImageView imgUserAvatar;
        public ImageButton btnDeleteChat;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            imgUserAvatar = itemView.findViewById(R.id.imgAvatar);
            btnDeleteChat = itemView.findViewById(R.id.btnDeleteChat);
        }
    }
}