package com.amol.realapp.chatty.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.amol.realapp.chatty.R;
import com.amol.realapp.chatty.activity.ChatActivity;
import com.amol.realapp.chatty.model.userProfile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class userAdapter extends RecyclerView.Adapter<userAdapter.userItemHolder> {
  private Context context;
  private ArrayList<userProfile> users;

  public userAdapter(Context context, ArrayList<userProfile> users) {
    this.context = context;
    this.users = users;
  }

  @Override
  public userAdapter.userItemHolder onCreateViewHolder(ViewGroup p1, int p2) {
    View v =
        LayoutInflater.from(context).inflate(R.layout.user_profile_chats_item_layout, p1, false);

    return new userItemHolder(v);
  }

  @Override
  public void onBindViewHolder(userAdapter.userItemHolder p1, int p2) {
    userProfile user = users.get(p2);

    String senderId = FirebaseAuth.getInstance().getUid();
    String senderRoom = senderId + user.getUid();

    FirebaseDatabase.getInstance()
        .getReference()
        .child("chats")
        .child(senderRoom)
        .addValueEventListener(
            new ValueEventListener() {

              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                  SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

                  String lastMssg = dataSnapshot.child("lastMessage").getValue(String.class);
                  long time = dataSnapshot.child("lastMessageTime").getValue(long.class);
                  if (lastMssg.length() > 30) {
                    p1.userChatMessage.setText(lastMssg);

                    p1.userChatMessage.setSingleLine(false);
                    p1.userChatMessage.setEllipsize(TextUtils.TruncateAt.END);
                    p1.userChatMessage.setLines(2);
                  } else {
                    p1.userChatMessage.setText(lastMssg);
                  }

                  p1.userChatTime.setText(dateFormat.format(new Date(time)));

                } else {

                }
              }

              @Override
              public void onCancelled(DatabaseError dataSnapshot) {}
            });
    p1.username.setText(user.getName());
    if (!user.getUserProfileImage().equals("no image")) {

      Glide.with(context)
          .load(user.getUserProfileImage())
          .diskCacheStrategy(DiskCacheStrategy.DATA)
          .placeholder(R.drawable.ic_profile)
          .into(p1.profileImage);
    } else {
      p1.profileImage.setImageResource(R.drawable.ic_profile);
    }
    p1.chatsDetailsContainer.setOnClickListener(
        new View.OnClickListener() {

          @Override
          public void onClick(View view) {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userChatsName", user.getName());
            intent.putExtra("userChatsProfile", user.getUserProfileImage());
            intent.putExtra("userChatsUid", user.getUid());
            context.startActivity(intent);
          }
        });
    p1.profileImage.setOnTouchListener(
        new View.OnTouchListener() {

          @Override
          public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
              // TODO click
              View v =
                  LayoutInflater.from(context)
                      .inflate(R.layout.dialog_view_user_profile_lists, null, false);
              Dialog mBuilder = new Dialog(context);
              mBuilder.setContentView(v);
              mBuilder.setCancelable(true);
              mBuilder.show();
              TextView userDialogName = v.findViewById(R.id.userDialogName);
              ImageView userDialogProfileImage = v.findViewById(R.id.userDialogProfileImage);
              ImageView userDialogInfo = v.findViewById(R.id.userDialogInfo);

              userDialogName.setText(user.getName());
              if (!user.getUserProfileImage().equals("no image")) {

                Glide.with(context)
                    .load(user.getUserProfileImage())
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(userDialogProfileImage);

              } else {
                userDialogProfileImage.setImageResource(R.drawable.ic_profile);
              }
              
              userDialogInfo.setOnClickListener(
                  new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                      Toast.makeText(
                              context, "User:" + user.getName() + "-info", Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
            } else if (action == MotionEvent.ACTION_UP) {
              // TODO up
            } else if (action == MotionEvent.ACTION_MOVE) {
              // TODO move
            }
            return true;
          }
        });
  }

  @Override
  public int getItemCount() {
    return users.size();
  }

  public class userItemHolder extends RecyclerView.ViewHolder {
    ShapeableImageView profileImage;
    TextView username, time, userChatMessage, userChatTime;
    MaterialCardView chatsDetailsContainer;

    public userItemHolder(View v) {
      super(v);
      profileImage = v.findViewById(R.id.userImage);
      username = v.findViewById(R.id.userName);
      time = v.findViewById(R.id.userTime);
      chatsDetailsContainer = v.findViewById(R.id.chatsDetailsContainer);
      userChatMessage = v.findViewById(R.id.userChatMessage);
      userChatTime = v.findViewById(R.id.userTime);
    }
  }
}
