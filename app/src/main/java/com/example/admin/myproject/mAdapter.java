package com.example.admin.myproject;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class mAdapter extends RecyclerView.Adapter<mAdapter.MyViewHolder> {

    private List<User> usersList;
    private Context mcontext;

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private ItemClickListener clickListener;
        public TextView friendname, latlng;
        public ImageView icon,photo;

        public MyViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.icon);
            friendname = (TextView) view.findViewById(R.id.friendname);
            latlng = (TextView) view.findViewById(R.id.latlng);
            photo = (ImageView) view.findViewById(R.id.profile);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onClick(view, getAdapterPosition(), true);
            return true;
        }
    }


    public mAdapter(Context applicationContext, List<User> usersList) {
        this.usersList = usersList;
        mcontext = applicationContext;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.friendname.setText(user.getName());
        holder.latlng.setText(user.getEmail());
        Log.d(user.getName(),user.getUrl());
        if(user.getUrl()!=null && !user.getUrl().equals("")) {
            Picasso.with(mcontext).load(user.getUrl())
                    .placeholder(R.drawable.placeholder).resize(50,50).centerCrop().transform(new CropCircleTransformation()).into(holder.photo);
        }
        else{
            Picasso.with(mcontext).load(R.drawable.placeholder).
                    resize(50,50).centerCrop().transform(new CropCircleTransformation()).into(holder.photo);
        }
        setIcon(user, holder);

        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (isLongClick) {
                    User user = usersList.get(position);
                    setOnClick(user);
                } else {
                    userList.setUser(usersList.get(position));
                    Intent intent = new Intent(mcontext,profileActivity.class);
                    mcontext.startActivity(intent);
                    Toast.makeText(mcontext, "#" + position + " - " + usersList.get(position).getName(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void setIcon(User user, MyViewHolder holder) {
        if (userList.getFriends().get(user.getUid()) != null
                && userList.getFriends().get(user.getUid()) == 1) {

            holder.icon.setImageResource(R.drawable.final_added);
            Log.d("adapter", "friends");
        } else if (userList.getReceived().get(user.getUid()) != null
                && userList.getReceived().get(user.getUid()) == 1) {

            holder.icon.setImageResource(R.drawable.final_received);
            Log.d("adapter ", "received request");
        } else if (userList.getSent().get(user.getUid()) != null
                && userList.getSent().get(user.getUid()) == 1) {

            holder.icon.setImageResource(R.drawable.final_sent);
            Log.d("adapter", "sent request");
        } else {
            holder.icon.setImageResource(R.drawable.final_add);
            Log.d("adapter "+user.getUid()+" "+userList.getFriends().get(user.getUid()), "nothing");
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public void setOnClick(User user) {
        if (userList.getFriends().get(user.getUid()) != null
                && userList.getFriends().get(user.getUid()) == 1) {

            Log.d("click", "friends");
        }
        else if (userList.getReceived().get(user.getUid()) != null
                && userList.getReceived().get(user.getUid()) == 1) {
            addFriend(user);
            Log.d("click", "received request");
        }
        else if (userList.getSent().get(user.getUid()) != null
                && userList.getSent().get(user.getUid()) == 1) {
            cancelRequest(user);
            Log.d("click", "sent request");
        }
        else {
            sendRequest(user);
            Log.d("click", "nothing");
        }
    }

    private void sendRequest(User user){
        FirebaseDatabase.getInstance()
                .getReference("FriendRequestReceiver")
                .child(user.getUid())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(1).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            Log.d("database","send receive failed");
                        }
                    }
                }
        );
        FirebaseDatabase.getInstance()
                .getReference("FriendRequestSender")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(user.getUid())
                .setValue(1).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            Log.d("database","send sender failed");
                        }
                    }
                }
        );
    }

    private void cancelRequest(User user) {
        FirebaseDatabase.getInstance()
                .getReference("FriendRequestReceiver")
                .child(user.getUid())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(0).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            Log.d("database","send receive failed");
                        }
                    }
                }
        );
        FirebaseDatabase.getInstance()
                .getReference("FriendRequestSender")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(user.getUid())
                .setValue(0).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            Log.d("database","send sender failed");
                        }
                    }
                }
        );
    }

    private void addFriend(User user) {
        FirebaseDatabase.getInstance()
                .getReference("FriendRequestSender")
                .child(user.getUid())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(0).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            Log.d("database","send receive failed");
                        }
                    }
                }
        );
        FirebaseDatabase.getInstance()
                .getReference("FriendRequestReceiver")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(user.getUid())
                .setValue(0).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            Log.d("database","send sender failed");
                        }
                    }
                }
        );
        FirebaseDatabase.getInstance()
                .getReference("friends")
                .child(user.getUid())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(1).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            Log.d("database","send receive failed");
                        }
                    }
                }
        );
        FirebaseDatabase.getInstance()
                .getReference("friends")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(user.getUid())
                .setValue(1).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // successfully added user
                        } else {
                            Log.d("database","send sender failed");
                        }
                    }
                }
        );
    }
}