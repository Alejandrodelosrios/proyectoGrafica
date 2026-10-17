package com.primerparcial;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class App {
    protected Long window;
    private int programa;
    private int vbo;
    private int vao;
    protected   int ancho = 1100;
    protected   int alto = 760;
    private float orbita= 0.6f;
    protected boolean modoNoche=false;
    private final Map<String, Integer> uniforms = new HashMap<>();
    protected boolean mostrarMapa = true;

    protected static final float LIMITE = 55;
    protected static float CELDA = 10;
    protected static final int[][] MAPA = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 2, 0, 1, 0, 1, 0, 1, 0, 2, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 2, 0, 1, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

    protected static final float ALTURA_FAROLA = 6f;
    private static final float OFFSET_FAROLA = 4.6f;
    private static final int[][] LOTES_FAROLA = {
            {1, 1}, {1, 5}, {1, 9},
            {5, 1}, {5, 5}, {5, 9},
            {9, 1}, {9, 5}, {9, 9},
    };
    protected final float[][] farolas = calcularFarolas();

    protected boolean metaCompletada =false;
    protected float destinoX =centro(8);
    protected float destinoZ =centro(2);

    protected float tiempoMundo = 0;
    private static final float T_VERDE = 5f;
    private static final float T_AMARILLO = 1.5f;
    private static final float T_ROJO = 6.5f;
    private static final float CICLO = T_VERDE + T_AMARILLO + T_ROJO;


    // funciones auxiliares
    protected float centro(int indice){
        return -LIMITE+CELDA*(indice+0.5f);
    }

    private float[][] calcularFarolas(){
        int mitad = MAPA.length / 2;
        float[][] resultado = new float[LOTES_FAROLA.length][];
        for (int i = 0; i < LOTES_FAROLA.length; i++) {
            int fila = LOTES_FAROLA[i][0];
            int columna = LOTES_FAROLA[i][1];
            float signoX = columna <= mitad ? 1 : -1;
            float signoZ = fila <= mitad ? 1 : -1;
            resultado[i] = new float[]{
                    centro(columna) + signoX * OFFSET_FAROLA,
                    centro(fila) + signoZ * OFFSET_FAROLA
            };
        }
        return resultado;
    }

    protected void tecla(int key){
        if(key==GLFW_KEY_ESCAPE){
            int respuesta = JOptionPane.showConfirmDialog(
                    null,
                    "esta seguro que quiere salir",
                    "Confirmar salida",
                    JOptionPane.YES_NO_OPTION);

            if (respuesta == JOptionPane.YES_NO_OPTION) {
                glfwSetWindowShouldClose(this.window, true);
            }
        }
        if(key == GLFW_KEY_N){
            this.modoNoche=!modoNoche;
        }
        if(key == GLFW_KEY_M){
            this.mostrarMapa=!mostrarMapa;
        }
    }

    private int compilar(int tipo,String fuente){
        int shader = glCreateShader(tipo);
        glShaderSource(shader,fuente);
        glCompileShader(shader);
        if(glGetShaderi(shader,GL_COMPILE_STATUS)==GL_FALSE){
            String error = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException(error);
        }
        return shader;
    }

    private void crearPrograma(){
        int shaderVertices = compilar(GL_VERTEX_SHADER,vertexShader());
        int shaderFragmentos =0;
        try{
            shaderFragmentos = compilar(GL_FRAGMENT_SHADER,fragmentShader());
            this.programa = glCreateProgram();
            glAttachShader(this.programa,shaderVertices);
            glAttachShader(this.programa,shaderFragmentos);
            glLinkProgram(this.programa);
            if(glGetProgrami(this.programa,GL_LINK_STATUS)== GL_FALSE){
                throw new IllegalStateException(glGetProgramInfoLog(this.programa));
            }
        }finally{
            glDeleteShader(shaderVertices);
            if(shaderFragmentos !=0){
                glDeleteShader(shaderFragmentos);
            }
        }
    }


    private void crearCubo(){
        float[] vertices = { // Cada línea describe un vértice: X, Y, Z, normalX, normalY, normalZ.
                -0.5f, -0.5f, 0.5f, 0, 0, 1, // Cara frontal: esquina inferior izquierda, normal hacia +Z.
                0.5f, -0.5f, 0.5f, 0, 0, 1, // Cara frontal: esquina inferior derecha.
                0.5f, 0.5f, 0.5f, 0, 0, 1, // Cara frontal: esquina superior derecha; termina el primer triángulo.
                0.5f, 0.5f, 0.5f, 0, 0, 1, // Cara frontal: repite la esquina para el segundo triángulo.
                -0.5f, 0.5f, 0.5f, 0, 0, 1, // Cara frontal: esquina superior izquierda.
                -0.5f, -0.5f, 0.5f, 0, 0, 1, // Cara frontal: cierra el segundo triángulo.

                -0.5f, -0.5f, -0.5f, 0, 0, -1, // Cara trasera: primer vértice, normal hacia -Z.
                -0.5f, 0.5f, -0.5f, 0, 0, -1, // Cara trasera: segundo vértice.
                0.5f, 0.5f, -0.5f, 0, 0, -1, // Cara trasera: termina el primer triángulo.
                0.5f, 0.5f, -0.5f, 0, 0, -1, // Cara trasera: inicia el segundo triángulo.
                0.5f, -0.5f, -0.5f, 0, 0, -1, // Cara trasera: esquina inferior derecha.
                -0.5f, -0.5f, -0.5f, 0, 0, -1, // Cara trasera: cierra la cara.

                -0.5f, 0.5f, 0.5f, -1, 0, 0, // Cara izquierda: primer vértice, normal hacia -X.
                -0.5f, 0.5f, -0.5f, -1, 0, 0, // Cara izquierda: segundo vértice.
                -0.5f, -0.5f, -0.5f, -1, 0, 0, // Cara izquierda: termina el primer triángulo.
                -0.5f, -0.5f, -0.5f, -1, 0, 0, // Cara izquierda: inicia el segundo triángulo.
                -0.5f, -0.5f, 0.5f, -1, 0, 0, // Cara izquierda: esquina inferior frontal.
                -0.5f, 0.5f, 0.5f, -1, 0, 0, // Cara izquierda: cierra la cara.

                0.5f, 0.5f, 0.5f, 1, 0, 0, // Cara derecha: primer vértice, normal hacia +X.
                0.5f, -0.5f, 0.5f, 1, 0, 0, // Cara derecha: segundo vértice.
                0.5f, -0.5f, -0.5f, 1, 0, 0, // Cara derecha: termina el primer triángulo.
                0.5f, -0.5f, -0.5f, 1, 0, 0, // Cara derecha: inicia el segundo triángulo.
                0.5f, 0.5f, -0.5f, 1, 0, 0, // Cara derecha: esquina superior trasera.
                0.5f, 0.5f, 0.5f, 1, 0, 0, // Cara derecha: cierra la cara.

                -0.5f, 0.5f, -0.5f, 0, 1, 0, // Cara superior: primer vértice, normal hacia +Y.
                -0.5f, 0.5f, 0.5f, 0, 1, 0, // Cara superior: segundo vértice.
                0.5f, 0.5f, 0.5f, 0, 1, 0, // Cara superior: termina el primer triángulo.
                0.5f, 0.5f, 0.5f, 0, 1, 0, // Cara superior: inicia el segundo triángulo.
                0.5f, 0.5f, -0.5f, 0, 1, 0, // Cara superior: esquina trasera derecha.
                -0.5f, 0.5f, -0.5f, 0, 1, 0, // Cara superior: cierra la cara.

                -0.5f, -0.5f, -0.5f, 0, -1, 0, // Cara inferior: primer vértice, normal hacia -Y.
                0.5f, -0.5f, -0.5f, 0, -1, 0, // Cara inferior: segundo vértice.
                0.5f, -0.5f, 0.5f, 0, -1, 0, // Cara inferior: termina el primer triángulo.
                0.5f, -0.5f, 0.5f, 0, -1, 0, // Cara inferior: inicia el segundo triángulo.
                -0.5f, -0.5f, 0.5f, 0, -1, 0, // Cara inferior: esquina frontal izquierda.
                -0.5f, -0.5f, -0.5f, 0, -1, 0 // Cara inferior: cierra los 36 vértices del cubo.
        };
        vao = glGenVertexArrays(); // Reserva el objeto que recordará la disposición de los atributos.
        glBindVertexArray(vao); // Activa ese VAO para configurarlo.
        vbo = glGenBuffers(); // Reserva el buffer que almacenará los vértices.
        glBindBuffer(GL_ARRAY_BUFFER, vbo); // Selecciona el VBO como destino de datos de vértices.
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW); // Copia los datos a la GPU; el cubo no cambiará.
        int bytesPorVertice = 6 * Float.BYTES; // Cada vértice contiene seis float de cuatro bytes.
        glVertexAttribPointer(0, 3, GL_FLOAT, false, bytesPorVertice, 0L); // Describe XYZ desde el primer byte del vértice.
        glEnableVertexAttribArray(0); // Habilita la posición que recibe aPos.
        glVertexAttribPointer(1, 3, GL_FLOAT, false, bytesPorVertice, 3L * Float.BYTES); // Describe la normal después de XYZ.
        glEnableVertexAttribArray(1); // Habilita la normal que recibe aNormal.
        glBindBuffer(GL_ARRAY_BUFFER, 0); // Deja de seleccionar el VBO una vez configurado.
        glBindVertexArray(0); // Finaliza la configuración del VAO.

    }

    protected boolean pulsada(int key){
        return glfwGetKey(this.window,key) == GLFW_PRESS;
    }

    protected void actualizar(float deltaTime){
        if(pulsada(GLFW_KEY_LEFT)){
            this.orbita-=deltaTime;
        }
        if(pulsada(GLFW_KEY_RIGHT)){
            this.orbita+=deltaTime;
        }
    }

    protected int uniform(String nombre){
        if(!uniforms.containsKey(nombre)){
            int ubicacion = glGetUniformLocation(this.programa,nombre);
            uniforms.put(nombre,ubicacion);
        }
        return uniforms.get(nombre);
    }

    protected void entero(String nombre , int valor){
        glUniform1i(uniform(nombre),valor);
    }

    protected void vector(String nombre,float x,float y,float z){
        glUniform3f(uniform(nombre),x,y,z);
    }

    protected void decimal(String nombre,float valor){
        glUniform1f(uniform(nombre),valor);
    }

    protected void configurarCamara(){
        float camaraX = (float) Math.sin(orbita)*110;
        float camaraZ = (float) Math.cos(orbita)*110;
        vector("uOjo",camaraX,88f,camaraZ);
        vector("uObjetivo",0,0,0);
        decimal("uAspecto",(float) ancho/alto);
    }

    protected void prepararLuces(){
        if(this.modoNoche){
            vector("uAmbiente",0.14f,0.16f,0.28f);
            vector("uDirLuz",-0.3f,0.8f,-0.4f);
            vector("uColorLuz",0.10f,0.13f,0.25f);
        }else{
            vector("uAmbiente",0.40f,0.40f,0.45f);
            vector("uDirLuz",0.4f,0.8f,0.3f);
            vector("uColorLuz",0.70f,0.65f,0.55f);
        }
        decimal("uEmision",0);
        decimal("uFarolasOn",modoNoche?1:0);
        entero("uNumFarolas",farolas.length);
        for(int i=0; i<farolas.length;i++){
            vector("uFarolas["+i+"]",farolas[i][0],ALTURA_FAROLA,farolas[i][1]);
        }
        decimal("uFarosOn",0);
    }

    protected void cajaGirada(float x,float y , float z, float sx,float sy , float sz,float r , float g, float b, float angulo){
        vector("uPos",x,y,z);
        vector("uEscala",sx,sy,sz);
        vector("uColor",r,g,b);
        decimal("uGiro",angulo);
        glDrawArrays(GL_TRIANGLES,0,36);
    }

    private void caja(float x,float y , float z, float sx,float sy , float sz,float r , float g, float b){
        this.cajaGirada(x,y,z,sx,sy,sz,r,g,b,0);
    }

    protected void escena() {
        caja(0, -0.25f, 0, LIMITE*2, 0.5f, LIMITE*2, 0.26f, 0.29f, 0.34f);
        for (int fila = 0; fila < MAPA.length; fila++) {
            for (int columna = 0; columna < MAPA[fila].length; columna++) {
                float x = centro(columna);
                float z = centro(fila);
                int tipo = MAPA[fila][columna];
                if (tipo == 0) {
                    dibujarMarcasCalle(fila,columna,x,z);
                } else {
                    caja(x,0.15f,z,10,0.3f,10,0.60f,0.64f,0.66f);
                    if(tipo==1){
                        float altura = 5+(fila*3+columna*7)%9;
                        float rojo =0.28f + columna*0.045f;
                        float azul =0.48f + fila*0.025f;
                        caja(x,altura/2+0.3f,z,7,altura,7,rojo,0.40f,azul);
                        caja(x,altura + 0.45f,z,7.3f,0.3f,7.3f,0.20f,0.26f,0.32f);
                        dibujarVentanas(fila,columna,x,z,altura);
                    }else{
                        caja(x,0.32f,z,9,0.1f,9,0.20f,0.45f,0.28f);
                        dibujarParque(fila,columna,x,z);
                    }
                }
            }
        }
        for(float[] f:farolas){
            dibujarFarola(f[0],f[1]);
        }
        decimal("uEmision", 1);
        if(metaCompletada){
            caja(destinoX, 4, destinoZ, 1, 8, 1, 0.1f, 1.0f, 0.3f);
        }else{
            caja(destinoX, 4, destinoZ, 1, 8, 1, 1.0f, 0.2f, 0.9f);
        }
        decimal("uEmision", 0);
        dibujarCebras();
        dibujarSemaforos();
    }

    private void dibujarFrame() {
        glViewport(0,0,ancho,alto);
        if(modoNoche){
            glClearColor(0.02f, 0.03f, 0.08f, 1);
        }else{
            glClearColor(0.55f, 0.75f, 0.92f, 1);
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(this.programa);
        glBindVertexArray(this.vao);
        entero("uMapa",0);
        configurarCamara();
        prepararLuces();
        escena();
        if(mostrarMapa){
            dibujarMiniMapa();
        }
        if(glGetError()!= GL_NO_ERROR){
            throw new IllegalStateException("Error OpemGL al dibujar");
        }
    }

    private void dibujarFarola(float x, float z){
        // poste
        caja(x, ALTURA_FAROLA / 2f, z, 0.3f, ALTURA_FAROLA, 0.3f, 0.25f, 0.25f, 0.28f);
        // bombilla: emisiva, se ve brillante sin depender de la luz
        decimal("uEmision", 1);
        if (modoNoche) {
            caja(x, ALTURA_FAROLA + 0.25f, z, 0.9f, 0.5f, 0.9f, 1.0f, 0.85f, 0.50f);
        } else {
            caja(x, ALTURA_FAROLA + 0.25f, z, 0.9f, 0.5f, 0.9f, 0.70f, 0.70f, 0.65f);
        }
        decimal("uEmision", 0);
    }

    protected String vertexShader(){
        return """
                #version 330 core // Selecciona GLSL 3.30 para el contexto OpenGL 3.3.
                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec3 aNormal; 
                uniform vec3 uPos;
                uniform vec3 uEscala; 
                uniform vec3 uOjo;
                uniform vec3 uObjetivo;
                uniform float uGiro;
                uniform float uAspecto;
                uniform int uMapa;
                out vec3 vMundo;
                out vec3 vNormal;
                void main() { 
                   float coseno = cos(uGiro);  
                   float seno = sin(uGiro);
                   mat3 giro = mat3( 
                     coseno, 0.0, -seno,
                     0.0, 1.0, 0.0,
                     seno, 0.0, coseno 
                   );
                   vMundo = giro * (aPos * uEscala) + uPos;
                   vNormal = normalize(giro * (aNormal / uEscala)); 
                   
                   if (uMapa == 1) { 
                     float pantallaX = vMundo.x / 57.0; 
                     float pantallaY = -vMundo.z / 57.0; 
                     float profundidad = -vMundo.y / 100.0;                                      
                     gl_Position = vec4(pantallaX, pantallaY, profundidad, 1.0); 
                   } else {
                      vec3 frente = normalize(uObjetivo - uOjo);
                      vec3 derecha = normalize(cross(frente, vec3(0.0, 1.0, 0.0))); 
                      vec3 arriba = cross(derecha, frente); 
                      vec3 diferencia = vMundo - uOjo; 
                      float vistaX = dot(diferencia, derecha); 
                      float vistaY = dot(diferencia, arriba); 
                      float vistaZ = -dot(diferencia, frente); 
                      float factor = 1.0 / tan(radians(55.0) * 0.5); 
                      float cerca = 0.1;
                      float lejos = 250.0; 
                      float clipX = vistaX * factor / uAspecto;
                      float clipY = vistaY * factor; 
                      float clipZ = (lejos + cerca) / (cerca - lejos) * vistaZ; 
                      clipZ += 2.0 * lejos * cerca / (cerca - lejos); 
                      gl_Position = vec4(clipX, clipY, clipZ, -vistaZ); 
                   }
                }
                """;
    }

    private String fragmentShader(){
        return """
                #version 330 core
                in vec3 vMundo;
                in vec3 vNormal;
                uniform vec3 uColor;
                uniform int uMapa;
                uniform float uEmision;
                uniform vec3 uAmbiente;
                uniform vec3 uDirLuz;
                uniform vec3 uColorLuz;
                uniform float uFarolasOn;
                uniform int uNumFarolas;
                uniform vec3 uFarolas[16];
                uniform float uFarosOn;
                uniform vec3 uFaroPos[2];
                uniform vec3 uFaroDir;
                out vec4 color;
                void main() {
                  if (uMapa == 1 || uEmision > 0.5) {
                    color = vec4(uColor, 1.0);
                    return;
                  }
                  vec3 n = normalize(vNormal);
                  float difusa = max(dot(n, normalize(uDirLuz)), 0.0);
                  vec3 luz = uAmbiente + uColorLuz * difusa;
                
                  // Farolas: luces puntuales con atenuación por distancia
                  for (int i = 0; i < uNumFarolas; i++) {
                    vec3 haciaLuz = uFarolas[i] - vMundo;
                    float d = length(haciaLuz);
                    float dif = max(dot(n, haciaLuz / d), 0.0);
                    float atenuacion = 1.0 / (1.0 + 0.03 * d + 0.004 * d * d);
                    luz += vec3(1.0, 0.80, 0.50) * 3.0 * dif * atenuacion * uFarolasOn;
                  }
                  
                  // Faros del auto: focos con cono y alcance limitado
                  vec3 dirFaro = normalize(uFaroDir);
                  for (int i = 0; i < 2; i++) {
                   vec3 haciaLuz = uFaroPos[i] - vMundo;
                   float d = length(haciaLuz);
                   vec3 l = haciaLuz / d;
                   float dif = max(dot(n, l), 0.0);
                   float cono = smoothstep(0.88, 0.96, dot(-l, dirFaro));
                   float alcance = 1.0 - smoothstep(22.0, 38.0, d);
                   float atenuacion = 1.0 / (1.0 + 0.04 * d + 0.006 * d * d);
                   luz += vec3(1.0, 0.95, 0.80) * 3.5 * dif * cono * alcance * atenuacion * uFarosOn;
                  }
                
                  luz = min(luz, vec3(1.7));
                  color = vec4(uColor * luz, 1.0);
                }
                """;
    }

    private void dibujarMarcasCalle(int fila , int columna,float x , float z){
        if(fila%2==0 && columna%2==1){
            for (int desplazamiento =-3; desplazamiento<=3;desplazamiento+=3){
             caja(x+desplazamiento,0.025f,z,1.6f,0.03f,0.13f,1,0.84f,0.35f);
            }
        }
        if(columna%2==0 && fila%2==1){
            for (int desplazamiento =-3; desplazamiento<=3;desplazamiento+=3){
                caja(x,0.025f,z+desplazamiento,0.13f,0.03f,1.6f,1,0.84f,0.35f);
            }
        }
    }

    protected void dibujarMiniMapa(){
        int lado = Math.round(Math.min(ancho, alto) * 0.30f);
        if (lado < 20) return;
        int margen = 12;
        int x0 = ancho - lado - margen;
        int y0 = alto - lado - margen;

        glEnable(GL_SCISSOR_TEST);
        glScissor(x0-3,y0-3,lado+6,lado+6);
        glClearColor(0.9f, 0.9f, 0.9f, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glScissor(x0,y0,lado,lado);
        glClearColor(0.05f, 0.07f, 0.10f, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(x0,y0,lado,lado);

        entero("uMapa",1);
        decimal("uEmision",0);
        dibujarCiudadMapa();
        dibujarMarcadoresMapa();
        entero("uMapa",0);

        glDisable(GL_SCISSOR_TEST);
        glViewport(0,0,ancho,alto);
    }

    protected void dibujarCiudadMapa(){
        caja(0, 0, 0, LIMITE * 2, 0.2f, LIMITE * 2, 0.20f, 0.22f, 0.27f);
        for(int fila=0;fila< MAPA.length;fila++){
            for(int columna=0;columna<MAPA[fila].length;columna++) {
                int tipo = MAPA[fila][columna];
                if (tipo == 0) {
                    continue;
                }
                float x = centro(columna);
                float z = centro(fila);
                if (tipo == 1) {
                 caja(x, 1, z, 8, 2, 8, 0.55f, 0.58f, 0.65f);
                } else {
                  caja(x, 1, z, 8, 2, 8, 0.20f, 0.60f, 0.30f);
                }
            }
        }
    }

    protected void dibujarMarcadoresMapa(){
        if(metaCompletada){
            caja(destinoX, 18, destinoZ, 6, 1, 6, 0.1f, 1.0f, 0.3f);
        }else{
            caja(destinoX, 18, destinoZ, 6, 1, 6, 1.0f, 0.2f, 0.9f);
        }
    }

    private void dibujarSemaforos(){
        for (int fila = 2; fila < MAPA.length - 1; fila += 2) {
            for (int columna = 2; columna < MAPA[fila].length - 1; columna += 2) {
                float signo = (fila==8 && columna==8) ? -1f : 1f;
                float x = centro(columna) + signo*5.4f;
                float z = centro(fila) + signo * 5.4f;
                float fase = (tiempoMundo + (fila + columna) * 1.3f) % CICLO;
                dibujarSemaforo(x, z, fase);
            }
        }
    }

    private void dibujarSemaforo(float x, float z, float fase){
        // 0 = rojo, 1 = amarillo, 2 = verde
        int estado = fase < T_VERDE ? 2 : (fase < T_VERDE + T_AMARILLO ? 1 : 0);
        caja(x, 2.2f, z, 0.25f, 4.4f, 0.25f, 0.20f, 0.20f, 0.22f);   // poste
        caja(x, 5.0f, z, 0.8f, 2.2f, 0.8f, 0.10f, 0.10f, 0.12f);     // caja
        float[][] colores = {{1.0f, 0.10f, 0.10f}, {1.0f, 0.85f, 0.10f}, {0.10f, 1.0f, 0.20f}};
        float[] alturas = {5.8f, 5.0f, 4.2f};
        decimal("uEmision", 1);
        for (int i = 0; i < 3; i++) {
            float k = (i == estado) ? 1.0f : 0.12f;
            caja(x, alturas[i], z, 0.95f, 0.5f, 0.95f,
                    colores[i][0] * k, colores[i][1] * k, colores[i][2] * k);
        }
        decimal("uEmision", 0);
    }

    private void dibujarCebras(){
        // calles horizontales: franjas largas en X, repartidas en Z
        for (int fila = 2; fila < MAPA.length - 1; fila += 2) {
            for (int columna = 1; columna < MAPA[fila].length; columna += 2) {
                float x = centro(columna) + 3.4f;
                float z = centro(fila);
                for (int i = -2; i <= 2; i++) {
                    caja(x, 0.05f, z + i * 1.6f, 3.2f, 0.06f, 0.7f, 0.92f, 0.92f, 0.92f);
                }
            }
        }
        // calles verticales: franjas largas en Z, repartidas en X
        for (int columna = 2; columna < MAPA[0].length - 1; columna += 2) {
            for (int fila = 1; fila < MAPA.length; fila += 2) {
                float x = centro(columna);
                float z = centro(fila) + 3.4f;
                for (int i = -2; i <= 2; i++) {
                    caja(x + i * 1.6f, 0.05f, z, 0.7f, 0.06f, 3.2f, 0.92f, 0.92f, 0.92f);
                }
            }
        }
    }

    private void dibujarVentanas(int fila, int columna, float x, float z, float altura){
        int piso = 0;
        for (float y = 2.0f; y < altura - 0.8f; y += 2.2f, piso++) {
            for (int c = -1; c <= 1; c++) {
                for (int cara = 0; cara < 4; cara++) {
                    int semilla = fila * 7 + columna * 3 + piso * 5 + (c + 1) * 11 + cara * 13;
                    boolean emisiva = modoNoche && (semilla % 4 != 0);
                    decimal("uEmision", emisiva ? 1 : 0);
                    float r = emisiva ? 1.0f : 0.30f;
                    float g = emisiva ? 0.82f : 0.42f;
                    float b = emisiva ? 0.45f : 0.55f;
                    float yMundo = 0.3f + y;
                    float d = c * 2.0f;
                    switch (cara) {
                        case 0: caja(x + d, yMundo, z + 3.53f, 1.2f, 1.0f, 0.1f, r, g, b); break;
                        case 1: caja(x + d, yMundo, z - 3.53f, 1.2f, 1.0f, 0.1f, r, g, b); break;
                        case 2: caja(x + 3.53f, yMundo, z + d, 0.1f, 1.0f, 1.2f, r, g, b); break;
                        default: caja(x - 3.53f, yMundo, z + d, 0.1f, 1.0f, 1.2f, r, g, b); break;
                    }
                }
            }
        }
        decimal("uEmision", 0);
    }

    private void dibujarParque(int fila, int columna, float x, float z){
        float base = 0.37f;   // superficie del césped
        float[][] arboles = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int i = 0; i < arboles.length; i++) {
            float tx = x + arboles[i][0];
            float tz = z + arboles[i][1];
            float verde = 0.38f + ((fila + columna + i) % 3) * 0.08f;
            caja(tx, base + 0.9f, tz, 0.4f, 1.8f, 0.4f, 0.35f, 0.22f, 0.10f);          // tronco
            caja(tx, base + 2.5f, tz, 2.4f, 1.6f, 2.4f, 0.10f, verde, 0.14f);          // copa baja
            caja(tx, base + 3.8f, tz, 1.5f, 1.2f, 1.5f, 0.12f, verde + 0.08f, 0.16f);  // copa alta
        }
        dibujarBanco(x, z - 1.5f, -1);   // respaldo hacia afuera
        dibujarBanco(x, z + 1.5f, 1);
    }

    private void dibujarBanco(float x, float z, int lado){
        float base = 0.37f;
        caja(x, base + 0.50f, z, 1.6f, 0.12f, 0.5f, 0.55f, 0.35f, 0.18f);                  // asiento
        caja(x, base + 0.85f, z + lado * 0.22f, 1.6f, 0.5f, 0.08f, 0.55f, 0.35f, 0.18f);   // respaldo
        caja(x - 0.65f, base + 0.22f, z, 0.1f, 0.44f, 0.4f, 0.20f, 0.20f, 0.22f);          // patas
        caja(x + 0.65f, base + 0.22f, z, 0.1f, 0.44f, 0.4f, 0.20f, 0.20f, 0.22f);
    }
// funciones principales del run
    private void iniciar() {
        if (!glfwInit()) {
            throw new IllegalStateException("no se pudo iniciarlizar la GLFW");
        }
        glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        this.window = GLFW.glfwCreateWindow(ancho, alto, "Primer Parcial", 0, 0);
        if (this.window == 0) {
            throw new IllegalStateException("No se pudo crear la ventana de OpenGL");
        }
        glfwMakeContextCurrent(this.window);
        glfwSwapInterval(1);
        GLFW.glfwShowWindow(this.window);
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glfwSetKeyCallback(this.window, (ventanaEvento, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                tecla(key);
            }
        });
        crearPrograma();
        crearCubo();
    }

    private void loop(){
        double tiempoAnterior = glfwGetTime();
        int cuadros = 0;
        int maxCuadros = Integer.getInteger("demo.frames",0);
        int[] anchoReal = new int[1];
        int[] altoReal  = new int[1];

        while(!glfwWindowShouldClose(this.window)){
            glfwPollEvents();
            double tiempoActual = glfwGetTime();
            float deltaTime = (float) Math.min(tiempoActual-tiempoAnterior,0.05);
            tiempoAnterior = tiempoActual;
            tiempoMundo+=deltaTime;
            actualizar(deltaTime);
            glfwGetFramebufferSize(this.window,anchoReal,altoReal);
            ancho = anchoReal[0];
            alto  = altoReal[0];
            if(ancho > 0 && alto > 0){
                dibujarFrame();
            }
            GLFW.glfwSwapBuffers(this.window);
            cuadros++;
            if(maxCuadros>0 && cuadros >= maxCuadros){
                glfwSetWindowShouldClose(this.window,true);
            }
        }
    }

    private void cleanup(){
        if(this.vao!=0) {
            glDeleteVertexArrays(this.vao);
        }
        if(this.vbo !=0) {
            glDeleteBuffers(this.vbo);
        }
        if(this.programa != 0) {
            glDeleteProgram(this.programa);
        }
        if(this.window!=0) {
            Callbacks.glfwFreeCallbacks(this.window);
            glfwDestroyWindow(this.window);
        }
        glfwTerminate();
    }

    public void run() {
        GLFWErrorCallback errores = GLFWErrorCallback.createPrint(System.err);
        errores.set();
        try {
            iniciar();
            loop();
        }finally {
            cleanup();
            glfwSetErrorCallback(null);
            errores.free();
        }
    }

    public static void main(String[] args) {
      new App().run();
        }
    }