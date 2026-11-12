
package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class ThreeDViewing {

    private long window;

    private int width = 1100;
    private int height = 760;

    private Mesh3D sphere;
    private Mesh3D plane;

    private ShaderProgram sceneShader;
    private ShaderProgram depthShader;

    private Texture2D texture;
    private ShadowMapFBO shadow;

    // Pengaturan fitur
    private boolean blinn = true;
    private boolean flat = false;
    private boolean depthTest = true;
    private boolean culling = false;
    private boolean shadows = true;
    private boolean nearest = false;
    private boolean textureEnabled = true;

    // Posisi sumber cahaya
    private final Vector3f lightPos =
            new Vector3f(-3, 6, 2);

    // Matriks transformasi
    private final Matrix4f projection =
            new Matrix4f();

    private final Matrix4f view =
            new Matrix4f();

    private final Matrix4f lightSpace =
            new Matrix4f();

    private float angle = 0;

    private final Set<Integer> keys =
            new HashSet<>();

    public static void main(String[] args) {

        new ThreeDViewing().run();
    }

    private void run() {

        try {
            init();
            loop();
        } finally {
            cleanup();
        }
    }

    // =========================================
    // INITIALIZATION
    // =========================================

    private void init() {

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {

            throw new IllegalStateException(
                    "GLFW gagal diinisialisasi!"
            );
        }

        glfwWindowHint(
                GLFW_CONTEXT_VERSION_MAJOR, 3
        );

        glfwWindowHint(
                GLFW_CONTEXT_VERSION_MINOR, 3
        );

        glfwWindowHint(
                GLFW_OPENGL_PROFILE,
                GLFW_OPENGL_CORE_PROFILE
        );

        window = glfwCreateWindow(
                width,
                height,
                "3D Visual Realism Suite",
                0,
                0
        );

        if (window == 0) {

            throw new RuntimeException(
                    "Window gagal dibuat!"
            );
        }

        glfwMakeContextCurrent(window);

        glfwSwapInterval(1);

        glfwShowWindow(window);

        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);

        glClearColor(
                0.08f,
                0.10f,
                0.15f,
                1.0f
        );

        // Window resize callback
        glfwSetFramebufferSizeCallback(
                window,
                (w, newWidth, newHeight) -> {

                    width = Math.max(newWidth, 1);
                    height = Math.max(newHeight, 1);

                    glViewport(
                            0, 0,
                            width, height
                    );

                    projection.setPerspective(
                            (float) java.lang.Math.toRadians(45),
                            (float) width / height,
                            0.1f,
                            100f
                    );
                }
        );

        // Keyboard callback
        glfwSetKeyCallback(
                window,
                (w, key, scancode, action, mods) -> {

                    if (action == GLFW_PRESS) {
                        keys.add(key);
                    }

                    if (action == GLFW_RELEASE) {
                        keys.remove(key);
                    }
                }
        );

        // Membuat objek 3D
        sphere = Mesh3D.createSphere(48, 64);

        plane = Mesh3D.createPlane();

        // Membuat shader
        sceneShader = new ShaderProgram(
                read("shaders/realism.vert"),
                read("shaders/realism.frag")
        );

        depthShader = new ShaderProgram(
                read("shaders/depth.vert"),
                read("shaders/depth.frag")
        );

        // Memuat tekstur
        texture = new Texture2D(
                resourcePath("textures/checker.png")
        );

        // Membuat shadow framebuffer
        shadow = new ShadowMapFBO();

        projection.setPerspective(
                (float) java.lang.Math.toRadians(45),
                (float) width / height,
                0.1f,
                100f
        );

        printControls();
    }

    // =========================================
    // RESOURCE LOADER
    // =========================================

    private String resourcePath(String path) {

        try {

            return Paths.get(
                    getClass()
                    .getClassLoader()
                    .getResource(path)
                    .toURI()
            ).toString();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Gagal membaca resource: " + path,
                    e
            );
        }
    }

    private String read(String path) {

        try (
            InputStream input =
                getClass()
                .getClassLoader()
                .getResourceAsStream(path)
        ) {

            if (input == null) {

                throw new RuntimeException(
                        "File tidak ditemukan: " + path
                );
            }

            return new String(
                    input.readAllBytes()
            );

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    // =========================================
    // KEYBOARD INFORMATION
    // =========================================

    private void printControls() {

        System.out.println(
            """
            ======================================
                 3D VISUAL REALISM SUITE
            ======================================

            B : Phong / Blinn-Phong
            F : Flat / Smooth Shading
            Z : Z-Buffer ON / OFF
            C : Back-Face Culling ON / OFF
            T : Texture ON / OFF
            N : Nearest / Bilinear Mipmapped
            S : Shadow Mapping ON / OFF

            ESC : Keluar dari aplikasi

            ======================================
            """
        );
    }

    // =========================================
    // MAIN RENDER LOOP
    // =========================================

    private void loop() {

        long lastTime = System.nanoTime();

        int frames = 0;

        while (!glfwWindowShouldClose(window)) {

            // ---------------------------------
            // 1. KEYBOARD INPUT
            // ---------------------------------

            pollToggle(
                    GLFW_KEY_B,
                    () -> blinn = !blinn
            );

            pollToggle(
                    GLFW_KEY_F,
                    () -> flat = !flat
            );

            pollToggle(
                    GLFW_KEY_Z,
                    () -> depthTest = !depthTest
            );

            pollToggle(
                    GLFW_KEY_C,
                    () -> culling = !culling
            );

            pollToggle(
                    GLFW_KEY_S,
                    () -> shadows = !shadows
            );

            pollToggle(
                    GLFW_KEY_T,
                    () -> textureEnabled = !textureEnabled
            );

pollToggle(
    GLFW_KEY_N,
    () -> {
        nearest = !nearest;

        if (nearest) {
            texture.setNearest();
            System.out.println("MODE: NEAREST");
        } else {
            texture.setBilinearMipmapped();
            System.out.println("MODE: BILINEAR MIPMAPPED");
        }
    }
);

            if (keys.contains(GLFW_KEY_ESCAPE)) {

                glfwSetWindowShouldClose(
                        window, true
                );
            }

            // ---------------------------------
            // 2. OBJECT ROTATION
            // ---------------------------------

            angle += 0.006f;

            Matrix4f model =
                    new Matrix4f()
                    .translate(0, 1.05f, 0)
                    .rotateY(angle)
                    .scale(1.15f);

            Matrix4f planeModel =
                    new Matrix4f();

            // ---------------------------------
            // 3. CAMERA
            // ---------------------------------

            Vector3f cameraPosition =
                    new Vector3f(4.2f, 3.0f, 6.2f);

            view.identity().lookAt(
                    cameraPosition,
                    new Vector3f(0, 0.8f, 0),
                    new Vector3f(0, 1, 0)
            );

            // ---------------------------------
            // 4. LIGHT MATRIX
            // ---------------------------------

            Matrix4f lightView =
                    new Matrix4f().lookAt(
                            lightPos,
                            new Vector3f(0, 0, 0),
                            new Vector3f(0, 1, 0)
                    );

            Matrix4f lightProjection =
                    new Matrix4f().ortho(
                            -8, 8,
                            -8, 8,
                            1, 20
                    );

            lightSpace.set(lightProjection)
                    .mul(lightView);

            // ---------------------------------
            // 5. SHADOW MAPPING - PASS 1
            // ---------------------------------

            if (shadows) {

                // Depth Test wajib aktif
                // selama shadow rendering.

                glEnable(GL_DEPTH_TEST);

                glDepthFunc(GL_LESS);

                shadow.bindForWriting();

                glClear(GL_DEPTH_BUFFER_BIT);

                depthShader.use();

                depthShader.setMat4(
                        "lightSpaceMatrix",
                        lightSpace
                );

                // Render sphere
                depthShader.setMat4(
                        "model",
                        model
                );

                sphere.render();

                // Render ground
                depthShader.setMat4(
                        "model",
                        planeModel
                );

                plane.render();

                shadow.unbind(
                        width,
                        height
                );
            }

            // ---------------------------------
            // 6. HIDDEN SURFACE REMOVAL
            // ---------------------------------

            HSRManager.setupDepthTest(depthTest);

            HSRManager.setupBackFaceCulling(culling);

            glClear(
                    GL_COLOR_BUFFER_BIT |
                    (depthTest
                    ? GL_DEPTH_BUFFER_BIT : 0)
            );

            // ---------------------------------
            // 7. SHADOW MAPPING - PASS 2
            // ---------------------------------

            sceneShader.use();

            sceneShader.setMat4(
                    "view",
                    view
            );

            sceneShader.setMat4(
                    "projection",
                    projection
            );

            sceneShader.setMat4(
                    "lightSpaceMatrix",
                    lightSpace
            );

            // ---------------------------------
            // 8. LIGHTING SETTINGS
            // ---------------------------------

            sceneShader.setVec3(
                    "lightPos",
                    lightPos
            );

            sceneShader.setVec3(
                    "viewPos",
                    cameraPosition
            );

            sceneShader.setVec3(
                    "lightColor",
                    new Vector3f(1, 1, 1)
            );

            // ---------------------------------
            // 9. SHADING SETTINGS
            // ---------------------------------

            sceneShader.setInt(
                    "useBlinnPhong",
                    blinn ? 1 : 0
            );

            sceneShader.setInt(
                    "flatShading",
                    flat ? 1 : 0
            );

            // ---------------------------------
            // 10. TEXTURE SETTINGS
            // ---------------------------------

            sceneShader.setInt(
                    "useTexture",
                    textureEnabled ? 1 : 0
            );

            sceneShader.setInt(
                    "diffuseTexture",
                    0
            );

            sceneShader.setInt(
                    "shadowMap",
                    1
            );

            // ---------------------------------
            // 11. SHADOW SETTINGS
            // ---------------------------------

            sceneShader.setInt(
                    "useShadow",
                    shadows ? 1 : 0
            );

            // Texture unit 0
            texture.bind(0);
            
            if (nearest) {
    texture.setNearest();
} else {
    texture.setBilinearMipmapped();
}

            // Texture unit 1
            glActiveTexture(GL_TEXTURE1);

            glBindTexture(
                    GL_TEXTURE_2D,
                    shadow.getDepthTextureID()
            );

            // ---------------------------------
            // 12. RENDER SPHERE
            // ---------------------------------

            sceneShader.setMat4(
                    "model",
                    model
            );

            sceneShader.setVec3(
                    "objectColor",
                    new Vector3f(
                            0.25f,
                            0.65f,
                            0.9f
                    )
            );

            sphere.render();

            // ---------------------------------
            // 13. RENDER GROUND
            // ---------------------------------

            sceneShader.setMat4(
                    "model",
                    planeModel
            );

            sceneShader.setVec3(
                    "objectColor",
                    new Vector3f(
                            0.65f,
                            0.68f,
                            0.72f
                    )
            );

            sceneShader.setInt(
                    "useTexture",
                    0
            );

            plane.render();

            // ---------------------------------
            // 14. DISPLAY
            // ---------------------------------

            glfwSwapBuffers(window);

            glfwPollEvents();

            // ---------------------------------
            // 15. FPS COUNTER
            // ---------------------------------

            frames++;

            long now = System.nanoTime();

            if (now - lastTime >= 1_000_000_000L) {

glfwSetWindowTitle(
    window,
    String.format(
        "3D Visual Realism Suite | FPS: %d | %s | %s | Texture: %s",
        frames,
        blinn ? "Blinn-Phong" : "Phong",
        flat ? "Flat" : "Smooth",
        nearest ? "NEAREST" : "BILINEAR MIPMAPPED"
    )
);
                frames = 0;

                lastTime = now;
            }
        }
    }

    // =========================================
    // KEYBOARD TOGGLE
    // =========================================

private void pollToggle(
        int key,
        Runnable action
) {
    if (keys.remove(key)) {
        System.out.println("TOMBOL TERDETEKSI: " + key);
        action.run();
    }
}
    // =========================================
    // CLEANUP
    // =========================================

    private void cleanup() {

        if (sphere != null) {
            sphere.delete();
        }

        if (plane != null) {
            plane.delete();
        }

        if (texture != null) {
            texture.delete();
        }

        if (shadow != null) {
            shadow.delete();
        }

        if (sceneShader != null) {
            sceneShader.delete();
        }

        if (depthShader != null) {
            depthShader.delete();
        }

        if (window != 0) {
            glfwDestroyWindow(window);
        }

        glfwTerminate();
    }
}
