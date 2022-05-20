package com.amol.realapp.chatty.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amol.realapp.chatty.R;
import com.amol.realapp.chatty.activity.ProfileActivity;
import com.amol.realapp.chatty.activity.RegisterActivity;
import com.amol.realapp.chatty.adapter.TopStatusAdapter;
import com.amol.realapp.chatty.adapter.userAdapter;
import com.amol.realapp.chatty.model.Status;
import com.amol.realapp.chatty.model.userProfile;
import com.amol.realapp.chatty.model.userStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ChatsFragment extends Fragment {
  private ArrayList<userProfile> users = new ArrayList<>();

  private FirebaseDatabase database;
  private userAdapter adapter;
  private RecyclerView chatsList;
  private View v;
  private DatabaseReference dRef;
  private RecyclerView storiesList;
  private TopStatusAdapter statusAdapter;
  private ArrayList<userStatus> UserStatusList;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    v = inflater.inflate(R.layout.fragment_chats, container, false);
    init();
    initListener();
    return v;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.chat_item, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.profile:
        startActivity(new Intent(getActivity(), ProfileActivity.class));

        break;
      case R.id.logout:
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getActivity(), RegisterActivity.class));
        getActivity().finish();
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  private void initListener() {
    dRef = database.getReference().child("Users");
    dRef.keepSynced(true);
    dRef.addValueEventListener(
        new ValueEventListener() {

          @Override
          public void onDataChange(DataSnapshot p1) {
            users.clear();
            for (DataSnapshot snapshot1 : p1.getChildren()) {
              userProfile user = snapshot1.getValue(userProfile.class);

              if (!user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                users.add(user);
              }
            }
            adapter.notifyDataSetChanged();
          }

          @Override
          public void onCancelled(DatabaseError p1) {}
        });
    adapter = new userAdapter(getActivity(), users);
    chatsList.setAdapter(adapter);
    UserStatusList = new ArrayList<>();

    FirebaseDatabase.getInstance()
        .getReference()
        .child("Stories")
        .addValueEventListener(
            new ValueEventListener() {

              @Override
              public void onDataChange(DataSnapshot p1) {
                if (p1.exists()) {
                  UserStatusList.clear();
                  for (DataSnapshot storySnapshot : p1.getChildren()) {
                    userStatus status = new userStatus();
                    status.setName(storySnapshot.child("name").getValue(String.class));
                    status.setProfileImage(
                        storySnapshot.child("profileImage").getValue(String.class));
                    status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                    status.setUid(storySnapshot.child("uid").getValue(String.class));
                    ArrayList<Status> statuses = new ArrayList<>();
                    for (DataSnapshot statusSnapshot :
                        storySnapshot.child("statuses").getChildren()) {
                      Status sampleStatus = statusSnapshot.getValue(Status.class);
                      statuses.add(sampleStatus);
                    }
                    status.setStatuses(statuses);
                    UserStatusList.add(status);
                  }
                  statusAdapter.notifyDataSetChanged();
                }
              }

              @Override
              public void onCancelled(DatabaseError p1) {}
            });

    statusAdapter = new TopStatusAdapter(getActivity(), UserStatusList);

    LinearLayoutManager llm = new LinearLayoutManager(getActivity());
    llm.setOrientation(LinearLayoutManager.HORIZONTAL);

    storiesList.setLayoutManager(llm);
    storiesList.setAdapter(statusAdapter);
  }

  private void init() {
    database = FirebaseDatabase.getInstance();

    storiesList = v.findViewById(R.id.userStatusList);
    chatsList = v.findViewById(R.id.chatsList);
  }
}
