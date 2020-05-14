package org.example.firebasedemo.webconfig;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    private final String[] bannedWordList = new String[] {
        "Shit",
        "bom",
        "drugs"
    };

    @PostConstruct
    public void init() throws IOException {

        //file is located outside the project folder, replace with your own firebase configuration file
        GoogleCredentials credentials = GoogleCredentials.fromStream(
            new FileInputStream("../my-project-12345.json")
        );

        //replace url with your own url (see firebase instructions for Java)
        FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(credentials)
            .setDatabaseUrl("https://my-project-12345.firebaseio.com")
            .build();

        FirebaseApp.initializeApp(options);

        //read out all messages
        FirebaseDatabase.getInstance()
            .getReference("messages")
            .addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        snapshot.getChildren().forEach(dataSnapshot -> {
                            //grab message and sender
                            String message = (String) dataSnapshot.getValue();
                            String user = dataSnapshot.getKey();

                            //use the reference to update the value in the database
                            DatabaseReference ref = dataSnapshot.getRef();

                            message = message.toLowerCase();

                            //iterate over all banned words
                            //if illegal words are detected, redact the message
                            for (String bannedWord : bannedWordList) {
                                if (message.contains(bannedWord.toLowerCase())) {
                                    ref.setValueAsync("!!REDACTED BY POLICE BOT!!");
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                }
            );
    }
}