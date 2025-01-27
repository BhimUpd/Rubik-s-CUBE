package np.nicolai.rubikscube;

import android.opengl.GLES32;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LineRenderer implements GLSurfaceView.Renderer {

    // Triangle vertices
    private float[] lineCoordinates = new float[]{
            -0.5f, 0.0f, 0.0f,  // start vertex
            0.5f, 0.8f, 0.0f    // end vertex
    };

    // Shader code
    private final String vertex_shader_code = String.join("\n",
            "#version 300 es\n",
            "layout (location = 0) in vec3 position;\n",
            "void main() {\n",
            "    gl_Position = vec4(position, 1.0);\n",
            "}"
    );

    private final String fragment_shader_code = String.join("\n",
            "#version 300 es\n",
            "precision mediump float;\n",
            "out vec4 outColor;\n",
            "void main() {\n",
            "    outColor = vec4(1.0, 1.0, 1.0, 1.0);\n", // White color
            "}"
    );

    private FloatBuffer vertexBuffer;
    private int program;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        ByteBuffer bb=ByteBuffer.allocateDirect(lineCoordinates.length*4);//create a FloatBuffer in Java to store vertex data (coordinates) in a format that is compatible with OpenGL ES.
        //This allocates a block of memory in the native heap (outside of Java's garbage-collected heap).

        bb.order(ByteOrder.nativeOrder());//OpenGL ES expects the data to be in native byte order for compatibility.Without this step, the data might be interpreted incorrectly, leading to rendering issues.

        vertexBuffer=bb.asFloatBuffer();//ByteBuffer into a FloatBuffer
        vertexBuffer.put(lineCoordinates);
        vertexBuffer.position(0);//OpenGL reads data starting from the buffer’s current position, so this ensures it starts reading from the beginning of the buffer.


        int vertexShader=loadShader(GLES32.GL_VERTEX_SHADER,vertex_shader_code);
        int fragmentShader=loadShader(GLES32.GL_FRAGMENT_SHADER,fragment_shader_code);

        program=GLES32.glCreateProgram();
        GLES32.glAttachShader(program,vertexShader);
        GLES32.glAttachShader(program,fragmentShader);
        GLES32.glLinkProgram(program);//Linking ensures that: Inputs (e.g., attributes) of one shader match the outputs of the previous shader.
        GLES32.glUseProgram(program);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES32.glViewport(0, 0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT | GLES32.GL_STENCIL_BUFFER_BIT);//clear previous frame's color,depth,stencil
        GLES32.glEnableVertexAttribArray(0);//enables a vertex attribute array for use in the vertex shader. tells OpenGL that the vertex attribute at the specified index (here, 0) should be used during rendering.

        GLES32.glVertexAttribPointer(0, 3, GLES32.GL_FLOAT, false, 0, vertexBuffer);//This function specifies how OpenGL should interpret the vertex data in the buffer for the vertex attribute at location 0
        GLES32.glDrawArrays(GLES32.GL_LINES, 0, 2);//OpenGL uses the data from vertexBuffer  0->starting index and 2->count of vertices

        GLES32.glDisableVertexAttribArray(0);
    }

    private int loadShader(int type, String shaderCode){
        int shader=GLES32.glCreateShader(type);
        GLES32.glShaderSource(shader,shaderCode);// it is used to attach the source code of a shader (written in GLSL) to a specific shader object
        GLES32.glCompileShader(shader);

        int[] compiled=new int[1];//// Array to store the compilation status (1 = success, 0 = failure)
        GLES32.glGetShaderiv(shader,GLES32.GL_COMPILE_STATUS,compiled,0);//0 is starting index in compiled array where result will be stored
        //GLES32.GL_COMPILE_STATUS: The parameter being queried. This specific constant tells OpenGL to return the compile status of the shader.

        if (compiled[0] == 0) {
            GLES32.glDeleteShader(shader);
            throw new RuntimeException("Error compiling shader: " + GLES32.glGetShaderInfoLog(shader));
        }
        return shader;
    }

}
