package com.inmo.arsdksample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ValentineAssistantActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private static final String TAG = "ValentineAssistant";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final String OPENAI_API_KEY = "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";

    private TextureView mTextureView;
    private Camera mCamera;
    private TextView mResultTextView;
    private boolean mIsAnalyzing = false;
    
    // INMO Air2 constants
    private static final int INMO_AIR2_CAMERA_ID = 0; // Front camera for INMO glasses
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valentine_assistant);
        
        mTextureView = findViewById(R.id.texture_view);
        mResultTextView = findViewById(R.id.result_text_view);
        
        mTextureView.setSurfaceTextureListener(this);
        
        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        
        // Set initial instruction text
        mResultTextView.setText("Press RING OK button to analyze who you're looking at");
    }
    
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openCamera(surface);
    }
    
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Handle surface changes if needed
    }
    
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        releaseCamera();
        return true;
    }
    
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // This is called every time the SurfaceTexture is updated
    }
    
    private void openCamera(SurfaceTexture surface) {
        try {
            mCamera = Camera.open(INMO_AIR2_CAMERA_ID);
            
            // Set camera parameters optimized for INMO AIR2
            Camera.Parameters parameters = mCamera.getParameters();
            
            // Find optimal preview size
            Camera.Size optimalSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), 1280, 720);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            
            // Set focus mode
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            
            mCamera.setParameters(parameters);
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Error opening camera: " + e.getMessage());
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    
    // Handle INMO RING2 button presses
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // INMO Ring OK button - KeyCode 66 (KEYCODE_ENTER)
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (!mIsAnalyzing) {
                captureAndAnalyzeImage();
            }
            return true;
        }
        
        // INMO Ring Back button - KeyCode 4 (KEYCODE_BACK)
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    private void captureAndAnalyzeImage() {
        if (mCamera == null) {
            return;
        }
        
        mIsAnalyzing = true;
        mResultTextView.setText("Analyzing...");
        
        // Take picture
        mCamera.takePicture(null, null, (data, camera) -> {
            // Convert byte array to bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            
            // Save the image
            String imagePath = saveImageToStorage(bitmap);
            
            // Restart preview
            mCamera.startPreview();
            
            // Analyze the image
            new AnalyzeImageTask().execute(imagePath);
        });
    }
    
    private String saveImageToStorage(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, fileName);
        
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return null;
        }
    }
    
    private class AnalyzeImageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String imagePath = params[0];
            if (imagePath == null) {
                return "Unable to analyze image";
            }
            
            try {
                // Load the image
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                
                // Convert to base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                
                // Create OpenAI API request
                return sendImageToOpenAI(base64Image);
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing image: " + e.getMessage());
                return "Move closer so I can get a better read!";
            }
        }
        
        @Override
        protected void onPostExecute(String result) {
            mResultTextView.setText(result);
            mIsAnalyzing = false;
        }
    }
    
    private String sendImageToOpenAI(String base64Image) throws IOException, JSONException {
        // Create OkHttpClient with longer timeout
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
                
        MediaType mediaType = MediaType.parse("application/json");
        
        // Create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4-turbo");
        
        JSONArray messages = new JSONArray();
        
        // System message
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are my AI dating assistant that will be integrated into my INMO AIR2 smart glasses. " +
                "I am currently single and looking for a date. Your job will be to help me find a girlfriend. As I walk around, " +
                "the camera from my smart glasses will pick up whatever I am seeing around me and will detect people. " +
                "If I am interested in a person, these smart glasses will take a frame (picture) of the girl that I am interested in. " +
                "You will be presented with this image. You will respond in short and concise phrases that will be read aloud to me " +
                "through my smart glasses to help me get this girl to be interested in me. " +
                "\n\nYour job at first will be to identify their emotion. Are they stressed because they are studying? Are they disgusted? " +
                "Are they locked on their phone? You will identify how they are feeling based on their BODY LANGUAGE. You will then state " +
                "at the beginning of the phrase **'Subject looks [INSERT EMOTION], try saying:'** followed by a smooth pickup line. " +
                "\n\nThe pickup line that follows the emotion identifier will be centered around **asking them to be my valentine**. " +
                "- Be as smooth as possible. " +
                "- Use wordplay, pop culture references, or even slightly NSFW humor if needed. " +
                "- Make sure the pickup line ends with **\"Will you be my valentine?\"** " +
                "- Keep it short and natural—if it's too long, it will be awkward to say out loud. " +
                "\n\nIf you **cannot identify their emotion**, **do not send error messages**. Instead, respond with something like: " +
                "- \"Move closer so I can get a better read!\" " +
                "\n\nIMPORTANT: INMO AIR2 has a small display area. Keep your response EXTREMELY BRIEF - under 100 characters if possible. " +
                "\n\nONLY respond with **one** formatted phrase and nothing else.");
        messages.put(systemMessage);
        
        // User message with image
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        
        JSONArray content = new JSONArray();
        
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", "Here is an image for analysis:");
        content.put(textContent);
        
        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        
        JSONObject imageUrl = new JSONObject();
        imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
        imageContent.put("image_url", imageUrl);
        
        content.put(imageContent);
        userMessage.put("content", content);
        
        messages.put(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 150);
        
        // Create request
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .post(RequestBody.create(requestBody.toString(), mediaType))
                .build();
                
        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            JSONArray choices = jsonResponse.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            
            return message.getString("content");
        } catch (Exception e) {
            Log.e(TAG, "Error with OpenAI API: " + e.getMessage());
            return "Move closer so I can get a better read!";
        }
    }
    
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int targetWidth, int targetHeight) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) targetWidth / targetHeight;
        
        if (sizes == null) return null;
        
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        
        // If no optimal size found with the target aspect ratio, find the largest available size
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        
        return optimalSize;
    }
}