package com.salesforce.glassdemo.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.android.glass.app.Card;

public class NoDocumentationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Card card = new Card(this);
        card.setText("There is no documentation associated with this step.");

        setContentView(card.getView());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
