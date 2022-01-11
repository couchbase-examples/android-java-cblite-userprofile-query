package com.couchbase.userprofile;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import com.couchbase.lite.Array;
import com.couchbase.lite.Blob;
import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Function;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.userprofile.util.DatabaseManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class IntegrationTests {

    private Context context;

    private static final String TEST_USER1 = "testUser1@demo.com";
    private static final String TEST_USER2 = "testUser2@demo.com";

    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_IMAGE_DATA = "imageData";

    private static final String VALUE_NAME = "Bob Smith";
    private static final String VALUE_ADDRESS = "123 No Where";
    private static final String UNIVERSITY_NAME = "dev";
    private static final String UNIVERSITY_FULL_NAME = "Devi Ahilya University of Indore";
    private static final String UNIVERSITY_COUNTRY = "india";
    private static final String IMAGE_TYPE = "image/png";

    private static final String KEY_NAME = "name";
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_WEB_PAGES = "web_pages";
    private static final String KEY_DICTIONARY_UNIVERSITY = "universities";

    @Test
    public void getDatabases()
    {
        //arrange
        DatabaseManager databaseManager = getDatabaseManager(TEST_USER1);

        //act
        Database userProfileDatabase =  DatabaseManager.getUserProfileDatabase();
        Database universityDatabase =  DatabaseManager.getUniversityDatabase();

        //assert
        assertNotNull(userProfileDatabase);
        assertNotNull(universityDatabase);

        cleanUp();
    }

    @Test
    public void saveProfile()
    {
        //arrange
        byte[] imageBytes = null;
        DatabaseManager databaseManager = getDatabaseManager(TEST_USER1);

        //act
        Database userProfileDatabase = DatabaseManager.getUserProfileDatabase();
        String docId = databaseManager.getCurrentUserDocId();

        Map<String, Object> profile = new HashMap<>();
        profile.put(FIELD_EMAIL, docId);
        profile.put(FIELD_ADDRESS, VALUE_ADDRESS);
        profile.put(FIELD_NAME, VALUE_NAME);

        //get bitmap image to save
        imageBytes = getImageBytes();
        assertNotNull(imageBytes);
        profile.put(FIELD_IMAGE_DATA, new com.couchbase.lite.Blob(IMAGE_TYPE, imageBytes));

        MutableDocument mutableDocument = new MutableDocument(docId, profile);

        //assert
        try{
            userProfileDatabase.save(mutableDocument);
        } catch (CouchbaseLiteException e){
            assertTrue(false);
        }
    }

    @Test
    public void getProfileTestUser1()
    {
        //arrange
        saveProfile();

        DatabaseManager databaseManager = DatabaseManager.getSharedInstance();
        Database userProfileDatabase = DatabaseManager.getUserProfileDatabase();
        String docId = databaseManager.getCurrentUserDocId();

        //act
        Document document = userProfileDatabase.getDocument(docId);

        Blob imageBlob = (Blob)document.getBlob(FIELD_IMAGE_DATA);
        byte[] fileBytes = getImageBytes();
        byte[] imageBytes = imageBlob.getContent();
        String email = document.getString(FIELD_EMAIL).replace("user::", "");
        String name = document.getString(FIELD_NAME);
        String address = document.getString(FIELD_ADDRESS);

        //assert
        assertNotNull(imageBlob);
        assertNotNull(imageBytes);

        assertArrayEquals(fileBytes, imageBytes);
        assertEquals(VALUE_NAME, name);
        assertEquals(VALUE_ADDRESS, address);
        assertEquals(TEST_USER1, email);

        cleanUp();
    }

    @Test
    public void getProfileTestUser2()
    {
        //arrange
        saveProfile();

        Database database = DatabaseManager.getUserProfileDatabase();
        String docId = TEST_USER2;

        //act
        Document document = database.getDocument(docId);

        //assert
        assertNull(document);

        cleanUp();
    }

    @Test
    public void getUniversitiesNoCountry(){
        getUniversitiesCountryName(false);
    }

    @Test
    public void getUniversitiesWithCountryQuery(){
        getUniversitiesCountryName(true);
    }

    private void getUniversitiesCountryName(boolean testCountry)
    {
        //arrange
        setupPrebuiltDatabase();

        //taken from UniversitiesPresenter.java
        Database database = DatabaseManager.getUniversityDatabase();
        Expression whereQueryExpression = Function.lower(Expression.property("name")).like(Expression.string("%" + UNIVERSITY_NAME.toLowerCase() + "%"));

        if (testCountry) {
            Expression countryQueryExpression = Function.lower(Expression.property("country")).like(Expression.string("%" + UNIVERSITY_COUNTRY.toLowerCase() + "%"));
            whereQueryExpression = whereQueryExpression.and(countryQueryExpression);
        }

        Query query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(database))
                .where(whereQueryExpression);

        ResultSet rows = null;
        List<Map<String, Object>> data = new ArrayList<>();
        Result row;
        Map<String, Object> firstRecord = new HashMap<>();

        //act
        try {
            rows = query.execute();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            assertTrue(false);
            return;
        }
        while((row = rows.next()) != null) {

            Map<String, Object> properties = new HashMap<>();
            properties.put(KEY_NAME, row.getDictionary(KEY_DICTIONARY_UNIVERSITY).getString(KEY_NAME));
            properties.put(KEY_COUNTRY, row.getDictionary(KEY_DICTIONARY_UNIVERSITY).getString(KEY_COUNTRY));
            properties.put(KEY_WEB_PAGES, row.getDictionary(KEY_DICTIONARY_UNIVERSITY).getArray(KEY_WEB_PAGES));
            data.add(properties);

            if (row.getDictionary(KEY_DICTIONARY_UNIVERSITY).getString(KEY_NAME).equals(UNIVERSITY_FULL_NAME)){
                firstRecord = properties;
            }
        }

        //assert
        if (testCountry) {
            assertEquals(5, data.size());
        } else {
            assertEquals(14, data.size());
        }
        assertEquals(UNIVERSITY_FULL_NAME, firstRecord.get(KEY_NAME).toString());
        assertEquals(UNIVERSITY_COUNTRY, firstRecord.get(KEY_COUNTRY).toString().toLowerCase());
        assertEquals(1, ((Array)firstRecord.get(KEY_WEB_PAGES)).count());
    }

    private void cleanUp()
    {
       //get pointers to database
       DatabaseManager instance = DatabaseManager.getSharedInstance();
       Database userProfileDatabase = DatabaseManager.getUserProfileDatabase();
       Database universityDatabase = DatabaseManager.getUniversityDatabase();

        cleanUpDatabase(userProfileDatabase);
        cleanUpDatabase(universityDatabase);

        //set user back to null
        instance.currentUser = null;

    }

    private void cleanUpDatabase(Database database)
    {
        try {
            //get info to delete database with
            String name = database.getName();
            File path = new File(database.getPath().replace(("/" + name + ".cblite2"), ""));

            //remove database
            database.close();
            Database.delete(name, path);
        } catch (CouchbaseLiteException e){
        }
    }

    private DatabaseManager getDatabaseManager(String databaseName)
    {
        context = ApplicationProvider.getApplicationContext();
        DatabaseManager instance = DatabaseManager.getSharedInstance();
        instance.initCouchbaseLite(context);
        instance.openOrCreateDatabaseForUser(context, databaseName);
        instance.openPrebuiltDatabase(context);

        return instance;
    }

    private void setupPrebuiltDatabase() {
        context = ApplicationProvider.getApplicationContext();
        DatabaseManager instance = DatabaseManager.getSharedInstance();
        instance.initCouchbaseLite(context);
        instance.openPrebuiltDatabase(context);
    }

    private byte[] getImageBytes()
    {
        //arrange
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(),R.mipmap.logo);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}

