package com.couchbase.userprofile.profile;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.userprofile.util.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

public class UserProfilePresenter
        implements UserProfileContract.UserActionsListener {
    private UserProfileContract.View mUserProfileView;

    public UserProfilePresenter(UserProfileContract.View mUserProfileView) {
        this.mUserProfileView = mUserProfileView;
    }

    // tag::fetchProfile[]
    public void fetchProfile()
    // end::fetchProfile[]
    {
        Database database = DatabaseManager.getUserProfileDatabase();

        // tag::docfetch[]
        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();

        if (database != null) {

            Map<String, Object> profile = new HashMap<>(); // <1>

            profile.put("email", DatabaseManager.getSharedInstance().currentUser); // <2>

            Document document = database.getDocument(docId); // <3>

            if (document != null) {
                profile.put("name", document.getString("name")); // <4>
                profile.put("address", document.getString("address")); // <4>
                profile.put("imageData", document.getBlob("imageData")); // <4>
                profile.put("university", document.getString("university"));
            }

            mUserProfileView.showProfile(profile); // <5>
        }
        // end::docfetch[]
    }

    // tag::saveProfile[]
    public void saveProfile(Map<String,Object> profile)
    // end::saveProfile[]
    {
        Database database = DatabaseManager.getUserProfileDatabase();

        String docId = DatabaseManager.getSharedInstance().getCurrentUserDocId();

        // tag::docset[]
        MutableDocument mutableDocument = new MutableDocument(docId, profile);
        // end::docset[]

        try {
            // tag::docsave[]
            database.save(mutableDocument);
            // end::docsave[]
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}
