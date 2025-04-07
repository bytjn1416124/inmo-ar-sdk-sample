/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inmo.arsdksample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.arglasses.arsdk.ArServiceSession;

public class MainActivity extends Activity {
    GLView mView;
    ArServiceSession mArSession;
    GlRenderer mRenderer;
    private FrameLayout mGlViewContainer;
    private Button mValentineButton;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        mGlViewContainer = findViewById(R.id.gl_view_container);
        mValentineButton = findViewById(R.id.valentine_button);

        // Initialize AR session
        mArSession = new ArServiceSession(getApplication());
        mArSession.create();
        mRenderer = new GlRenderer(mArSession);
        mView = new GLView(getApplication(), mRenderer);
        
        // Add GL view to container
        mGlViewContainer.addView(mView);

        // Set up button to launch Valentine Assistant
        mValentineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ValentineAssistantActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mView.onPause();
        mArSession.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mView.onResume();
        mArSession.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mArSession.destroy();
    }
}