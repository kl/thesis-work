package com.github.kl.contactpickerplugin.contactpicker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;

public class PluginActivity extends Activity {

    private static final int REQUEST_CODE = 0xDEED;
    private static final String EMAIL_KEY = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(getContactPickerIntent(), REQUEST_CODE);
    }

    private Intent getContactPickerIntent() {
        return new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent result = getResultIntent(data);
            setResult(Activity.RESULT_OK, result);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }

    private Intent getResultIntent(Intent result) {
        String address = queryEmailAddress(result.getData());

        Intent intent = new Intent();
        intent.putExtra(EMAIL_KEY, address);
        return intent;
    }

    private String queryEmailAddress(Uri data) {
        Cursor cursor = getContentResolver().query(data, null, null, null, null);
        cursor.moveToFirst();

        int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
        String email = cursor.getString(emailIndex);
        cursor.close();
        return email;
    }
}
