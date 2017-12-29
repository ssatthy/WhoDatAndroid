package com.likethatalsocan.whodat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class NewMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        ListView contactList = findViewById(R.id.contactList);

        ContactListViewAdapter adapter = new ContactListViewAdapter(this);
        contactList.setAdapter(adapter);

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ContactListViewAdapter.AccountRow accountRow = (ContactListViewAdapter.AccountRow) view.getTag();
                Account account = accountRow.getAccount();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("accountId",account.getId());
                returnIntent.putExtra("toId", account.getToId());
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Select a friend");

        ContactScanTask contactScanTask = new ContactScanTask();
        contactScanTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_message_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.newMessageCancelMenuItem :
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
