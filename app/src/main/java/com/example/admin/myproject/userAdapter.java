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

public class userAdapter extends RecyclerView.Adapter<userAdapter.MyViewHolder> {

    private List<User> usersList;
    private Context mcontext;



    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView username, location, time;
        public ImageView photo;

        public MyViewHolder(View view) {
            super(view);
            username = (TextView) view.findViewById(R.id.username);
            location = (TextView) view.findViewById(R.id.userlocation);
            time = (TextView) view.findViewById(R.id.time);
            photo = (ImageView) view.findViewById(R.id.profile_photo);
        }
    }


    public userAdapter(Context context,List<User> usersList) {
        this.usersList = usersList;
        mcontext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_user, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = usersList.get(position);
        holder.username.setText(user.getName());
        holder.location.setText(user.getlatlng());
        holder.time.setText(GetDate.getDate(user.getTimestamp()));
        Log.d(user.getName(), user.getUrl());
        if (user.getUrl() != null && !user.getUrl().equals("")) {
            Picasso.with(mcontext).load(user.getUrl())
                    .placeholder(R.drawable.placeholder).resize(50, 50).centerCrop().transform(new CropCircleTransformation()).into(holder.photo);
        }
        else{
            Picasso.with(mcontext).load(R.drawable.placeholder).
                    resize(50,50).centerCrop().transform(new CropCircleTransformation()).into(holder.photo);

        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }
}