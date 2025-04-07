package com.inmo.arsdksample;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.arglasses.arsdk.ArServiceSession;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlRenderer implements GLSurfaceView.Renderer {
    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeColors;
    private final int POSITION_DATA_SIZE = 3;
    private final int COLOR_DATA_SIZE = 4;
    private final float cSize = 0.117473001f;
    long frameCount = 0;
    long lastTime = 0;
    double step_angle = 1.0;
    double up_d = 0.01;
    private float[] mMVPMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int mProgramHandle;
    ArServiceSession mArSession;
    Context mContext;

    public GlRenderer(ArServiceSession arSession) {
        final float cubePosition[] = {
                // Front face
                -cSize, cSize, cSize,
                -cSize, -cSize, cSize,
                cSize, cSize, cSize,
                -cSize, -cSize, cSize,
                cSize, -cSize, cSize,
                cSize, cSize, cSize,

                // Right face
                cSize, cSize, cSize,
                cSize, -cSize, cSize,
                cSize, cSize, -cSize,
                cSize, -cSize, cSize,
                cSize, -cSize, -cSize,
                cSize, cSize, -cSize,

                // Back face
                cSize, cSize, -cSize,
                cSize, -cSize, -cSize,
                -cSize, cSize, -cSize,
                cSize, -cSize, -cSize,
                -cSize, -cSize, -cSize,
                -cSize, cSize, -cSize,

                // Left face
                -cSize, cSize, -cSize,
                -cSize, -cSize, -cSize,
                -cSize, cSize, cSize,
                -cSize, -cSize, -cSize,
                -cSize, -cSize, cSize,
                -cSize, cSize, cSize,

                // Top face
                -cSize, cSize, -cSize,
                -cSize, cSize, cSize,
                cSize, cSize, -cSize,
                -cSize, cSize, cSize,
                cSize, cSize, cSize,
                cSize, cSize, -cSize,

                // Bottom face
                cSize, -cSize, -cSize,
                cSize, -cSize, cSize,
                -cSize, -cSize, -cSize,
                cSize, -cSize, cSize,
                -cSize, -cSize, cSize,
                -cSize, -cSize, -cSize,
        };

        final float[] cubeColor = {
                // Front face (red)
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                // Right face (green)
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,

                // Back face (blue)
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                // Left face (yellow)
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,

                // Top face (cyan)
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 1.0f,

                // Bottom face (magenta)
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f
        };

        mCubePositions = ByteBuffer.allocateDirect(cubePosition.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePosition).position(0);
        mCubeColors = ByteBuffer.allocateDirect(cubeColor.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColor).position(0);
        mArSession = arSession;
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // TODO Auto-generated method stub
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        long time = System.currentTimeMillis();

        if (time - lastTime > 1000) {
            lastTime = time;
            Log.i("inmo", "opengl fps:" + frameCount);
            frameCount = 0;
        }
        frameCount++;
        float angleInDegrees = 0.0f;
        step_angle += 1.0;
        up_d += 0.001;
        GLES20.glUseProgram(mProgramHandle);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");

        mViewMatrix = mArSession.getViewMatrix();
//                    Log.d("VIO viewMatrix test", "viewMatrix:" + mViewMatrix[0] + "," + mViewMatrix[1] + "," + mViewMatrix[2] + "," +
//                            mViewMatrix[3] + "," + mViewMatrix[4] + "," + mViewMatrix[5] + "," + mViewMatrix[6] + "," +
//                            mViewMatrix[7] + "," + mViewMatrix[8] + "," + mViewMatrix[9] + "," + mViewMatrix[10] + "," +
//                            mViewMatrix[11] + "," + mViewMatrix[12] + "," + mViewMatrix[13] + "," + mViewMatrix[14]+"," + mViewMatrix[15]);
//    Matrix.setIdentityM(mViewMatrix, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -2.0f - cSize);

        drawCube(mCubePositions, mCubeColors);
    }

    private void drawCube(final FloatBuffer cubePositionsBuffer, final FloatBuffer cubeColorsBuffer) {
        cubePositionsBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, cubePositionsBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        cubeColorsBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, 0, cubeColorsBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // TODO Auto-generated method stub
        GLES20.glViewport(0, 0, width, height);
        Log.i("GLRenderer", "Resolution: " + width + "x" + height);

        mProjectionMatrix = mArSession.getProjectionMatrix(640, 400, 0.2f, 20);
        while (mProjectionMatrix[0] == 0 && mProjectionMatrix[1] == 0 && mProjectionMatrix[2] == 0 && mProjectionMatrix[3] == 0) {
            Log.d("VIO viewMatrix test", "mProjectionMatrix:" + mProjectionMatrix[0] + "," + mProjectionMatrix[1] + "," + mProjectionMatrix[2] + "," +
                    mProjectionMatrix[3] + "," + mProjectionMatrix[4] + "," + mProjectionMatrix[5] + "," + mProjectionMatrix[6] + "," +
                    mProjectionMatrix[7] + "," + mProjectionMatrix[8] + "," + mProjectionMatrix[9] + "," + mProjectionMatrix[10] + "," +
                    mProjectionMatrix[11] + "," + mProjectionMatrix[12] + "," + mProjectionMatrix[13] + "," + mProjectionMatrix[14] + "," +
                    mProjectionMatrix[15]);
            mProjectionMatrix = mArSession.getProjectionMatrix(640, 400, 0.2f, 20);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // TODO Auto-generated method stub
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"        // A constant representing the combined model/view/projection matrix.

                        + "attribute vec4 a_Position;     \n"        // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"        // Per-vertex color information we will pass in.
                        + "varying vec4 v_Color;          \n"        // This will be passed into the fragment shader.

                        + "void main()                    \n"        // The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"        // Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n"     // gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        final String fragmentShader =
                "precision mediump float;       \n"        // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"        // This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"        // The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"        // Pass the color directly through the pipeline.
                        + "}                              \n";

        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        if (vertexShaderHandle != 0) {
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);
            GLES20.glCompileShader(vertexShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0) {
            throw new RuntimeException("failed to creating vertex shader");
        }

        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if (fragmentShaderHandle != 0) {
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
            GLES20.glCompileShader(fragmentShaderHandle);

            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }

        }

        if (fragmentShaderHandle == 0) {
            throw new RuntimeException("failed to create fragment shader");
        }

        mProgramHandle = GLES20.glCreateProgram();
        if (mProgramHandle != 0) {
            GLES20.glAttachShader(mProgramHandle, vertexShaderHandle);
            GLES20.glAttachShader(mProgramHandle, fragmentShaderHandle);

            GLES20.glBindAttribLocation(mProgramHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(mProgramHandle, 1, "a_Color");

            GLES20.glLinkProgram(mProgramHandle);

            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(mProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(mProgramHandle);
                mProgramHandle = 0;
            }
        }

        if (mProgramHandle == 0) {
            throw new RuntimeException("failed to create program");
        }

    }

}