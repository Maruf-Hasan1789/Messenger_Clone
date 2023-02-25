package com.example.mark3;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.service.autofill.DateTransformation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {


    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;



    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView=inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactsList=(RecyclerView)ContactsView.findViewById(R.id.contacts_list);


        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactsView;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>adapter
                =new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model)
            {
                String userIDs=getRef(position).getKey();
                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild("image"))
                        {
                            String userImage=dataSnapshot.child("image").getValue().toString();
                            String proifleName=dataSnapshot.child("name").getValue().toString();
                            String profileStatus=dataSnapshot.child("status").getValue().toString();


                            holder.userName.setText(proifleName);
                            holder.userStatus.setText(profileStatus);
                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                        }
                        else
                        {
                            String proifleName=dataSnapshot.child("name").getValue().toString();
                            String profileStatus=dataSnapshot.child("status").getValue().toString();


                            holder.userName.setText(proifleName);
                            holder.userStatus.setText(profileStatus);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);

                ContactsViewHolder viewHolder= new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImage;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.users_profile_name);
            userStatus=itemView.findViewById(R.id.users_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);

        }
    }
}
