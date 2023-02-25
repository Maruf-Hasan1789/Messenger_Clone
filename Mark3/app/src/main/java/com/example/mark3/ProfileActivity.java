package com.example.mark3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.security.CryptoPrimitive;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID,senderUserID,Current_state;
    private CircleImageView userProfileImage;
    private TextView userProfilename,userProfileStatus;
    private Button sendMessageRequestButton, DeclineMessageRequestButton;
    private DatabaseReference UserRef,ChatRequestRef, ContactsRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth=FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        
       ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID=getIntent().getExtras().get("visit_user_id").toString();
        senderUserID=mAuth.getCurrentUser().getUid();



        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfilename=(TextView)findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView)findViewById(R.id.visit_profile_status);
        sendMessageRequestButton=(Button)findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton=(Button)findViewById(R.id.decline_message_request_button);

        Current_state="new";
        
        RetrieveUserInfo();
    }



    private void RetrieveUserInfo()
    {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String UserImage=dataSnapshot.child("image").getValue().toString();
                    String UserName=dataSnapshot.child("name").getValue().toString();
                    String UserStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(UserImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfilename.setText(UserName);
                    userProfileStatus.setText(UserStatus);

                    ManageChatRequests();

                }
                else
                {
                    String UserName=dataSnapshot.child("name").getValue().toString();
                    String UserStatus=dataSnapshot.child("status").getValue().toString();

                    userProfilename.setText(UserName);
                    userProfileStatus.setText(UserStatus);

                     ManageChatRequests(); 
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void ManageChatRequests()
    {

        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type=dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                            if(request_type.equals("sent"))
                            {
                                Current_state="request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received"))
                            {
                                Current_state="request_received";
                                sendMessageRequestButton.setText("Accept Chat Request");

                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if(dataSnapshot.hasChild(receiverUserID))
                                            {
                                                Current_state="friends";
                                                sendMessageRequestButton.setText("Remove this Contact");

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                        
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


         if(!senderUserID.equals(receiverUserID))
         {
              sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v)
                  {
                      sendMessageRequestButton.setEnabled(false);

                      if(Current_state.equals("new"))
                      {
                          sendChatRequest();
                      }
                      if(Current_state.equals("request_sent"))
                      {
                          CancelChatRequest();
                      }
                      if(Current_state.equals("request_received"))
                      {
                          AcceptChatRequest();
                      }
                      if(Current_state.equals("friends"))
                      {
                          RemoveSpecficContact();
                      }
                      
                  }
              });
         }
         else
         {
             sendMessageRequestButton.setVisibility(View.INVISIBLE);
         }
    }

    private void RemoveSpecficContact()
    {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_state="new";
                                                sendMessageRequestButton.setText("Send Message");


                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }

                                        }
                                    });









                        }

                    }
                });




















    }












    private void AcceptChatRequest()
    {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                               ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {

                                                            if(task.isSuccessful())
                                                            {

                                                                ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                sendMessageRequestButton.setEnabled(true);
                                                                                Current_state="friends";
                                                                                sendMessageRequestButton.setText("Remove this Contact");
                                                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                DeclineMessageRequestButton.setEnabled(false);



                                                                            }
                                                                        });













                                                            }

                                                        }
                                                    });





                                            }

                                        }
                                    });











                        }

                    }
                });
    }











    private void CancelChatRequest()
    {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                       if(task.isSuccessful())
                       {
                           ChatRequestRef.child(receiverUserID).child(senderUserID)
                                   .removeValue()                                               
                                   .addOnCompleteListener(new OnCompleteListener<Void>() {      
                                       @Override                                                
                                       public void onComplete(@NonNull Task<Void> task)         
                                       {                                                        
                                           if(task.isSuccessful())
                                           {
                                               sendMessageRequestButton.setEnabled(true);
                                               Current_state="new";
                                               sendMessageRequestButton.setText("Send Message");


                                               DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                               DeclineMessageRequestButton.setEnabled(false);
                                           }
                                                                                                
                                       }                                                        
                                   });                                                          









                       }

                    }
                });
    }




    private void sendChatRequest()
    {
           ChatRequestRef.child(senderUserID).child(receiverUserID)
                   .child("request_type").setValue("sent")
                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task)
                       {
                           if(task.isSuccessful())
                           {
                               ChatRequestRef.child(receiverUserID).child(senderUserID)
                                        .child("request_type").setValue("received")
                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task)
                                           {
                                               if(task.isSuccessful())
                                               {
                                                   sendMessageRequestButton.setEnabled(true);
                                                   Current_state="request_sent";
                                                   sendMessageRequestButton.setText("Cancel Chat Request");
                                               }
                                               
                                           }
                                       });
                           }
                           
                       }
                   });
    }
}
