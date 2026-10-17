package com.primerparcial;

import static org.lwjgl.glfw.GLFW.*;
public class Vehiculo extends App{

    private float autoX = -50;
    private float autoZ =  50;
    private float angulo = 0;
    private float velocidad = 0;
    private float desgaste = 0;
    private static final float VELOCIDAD_MAXIMA = 16f;
    private static final float DESGASTE_POR_METRO = 0.0015f;
    private static final float REDUCCION_MAXIMA = 0.6f;
    private float tallerX = centro(9);
    private float tallerZ = centro(8);
    private boolean camaraArea = false;
    private static final float RADIO_AUTO = 1.65f;
    private static final float RADIO_TALLER = 4f;
    private boolean farosOn =false;
    private final java.util.List<AutoIA> trafico = crearTrafico();

    @Override
    protected void tecla(int key){
        super.tecla(key);
        if(key==GLFW_KEY_C){
            this.camaraArea = !this.camaraArea;
        }
        if(key ==GLFW_KEY_R){
           reiniciar();
        }
        if(key == GLFW_KEY_F){
           this.farosOn = !farosOn;
        }
    }
    private void reiniciar(){
        super.metaCompletada =false;
        this.autoX =-50;
        this.autoZ = 50;
        this.angulo=0;
        this.velocidad=0;
        this.desgaste=0;
        for (AutoIA ia : trafico) {
            ia.reiniciar();
        }
    }

    @Override
    protected void actualizar(float deltaTime){
        for(AutoIA ia: trafico){
         ia.actualizar(deltaTime);
        }

        float acelerador = 0;

        if(pulsada(GLFW_KEY_W) || pulsada(GLFW_KEY_UP)){
            acelerador+=1;
        }
        if(pulsada(GLFW_KEY_S) || pulsada(GLFW_KEY_DOWN)) {
            acelerador-=1;
        }

        this.velocidad += acelerador*9*deltaTime;
        float resistencia = 0.7f;

        if(pulsada(GLFW_KEY_SPACE)){
            resistencia =7;
        }
        float factorFrenado = (float) Math.exp(-resistencia*deltaTime);
        velocidad*=factorFrenado;
        float velocidadMaxima = VELOCIDAD_MAXIMA*(1-REDUCCION_MAXIMA*desgaste);
        velocidad= Math.max(-6,Math.min(velocidadMaxima,velocidad));
        float direccion =0;

        if(pulsada(GLFW_KEY_A) || pulsada(GLFW_KEY_LEFT)){
            direccion +=1;
        }
        if(pulsada(GLFW_KEY_D) || pulsada(GLFW_KEY_RIGHT)){
            direccion -=1;
        }
        angulo+= direccion*velocidad*0.11f*deltaTime;
        float frenteX = -(float) Math.sin(angulo);
        float frenteZ = -(float) Math.cos(angulo);
        float siguienteX = autoX + frenteX * velocidad * deltaTime;
        float siguienteZ = autoZ + frenteZ * velocidad * deltaTime;

        if(puedeCircular(siguienteX,siguienteZ)){
          autoX = siguienteX;
          autoZ = siguienteZ;
          desgaste = Math.min(1,desgaste+Math.abs(velocidad)*deltaTime*DESGASTE_POR_METRO);
        }else{
            velocidad =0;
        }
        if(!metaCompletada){
            float dx = autoX - destinoX;
            float dz = autoZ - destinoZ;
            if(dx*dx + dz*dz < 36){
                metaCompletada = true;
            }
        }
        float tx=autoX-tallerX;
        float tz=autoZ-tallerZ;
        if(tx*tx + tz*tz < RADIO_TALLER*RADIO_TALLER && Math.abs(velocidad)<2){
            desgaste= Math.max(0,desgaste-0.4f*deltaTime);
        }
        actualizarTitulo();
    }

    private void actualizarTitulo(){
        int kilometrosPorHora = Math.round(Math.abs(velocidad)*3.6f);
        String titulo = getClass().getSimpleName();
        titulo+=" |WASD/teclas: conducir | Espacio: freno";
        titulo+="| c: camara | r reiniciar |";
        titulo+= kilometrosPorHora+" Km/h";
        titulo+= estadoExtras();
        glfwSetWindowTitle(window,titulo);
    }

    protected String estadoExtras(){
        String devolver =modoNoche ? " | Noche (N)" : " | Día (N)";
        devolver+= farosOn ?" | Faros ON (F)": "| Faros OFF (F)";
        devolver += "| Desgaste: "+ Math.round(desgaste*100) + "%";
        return devolver;
    }
    private boolean puedeCircular(float sx,float sz){
        float limitePermitido = LIMITE -RADIO_AUTO;
        if(Math.abs(sx)>limitePermitido || Math.abs(sz)>limitePermitido){
            return false;
        }
        for(int fila=0; fila<MAPA.length; fila++){
            for(int columna=0;columna<MAPA[fila].length;columna++){
                if(MAPA[fila][columna]==0){
                    continue;
                }
                float mitad = CELDA/2f;
                float centroX = centro(columna);
                float centroZ = centro(fila);
                float cercaX = Math.max(centroX-5,Math.min(sx,centroX+mitad));
                float cercaZ = Math.max(centroZ-5,Math.min(sz,centroZ+mitad));
                float distanciaX = sx-cercaX;
                float distanciaZ = sz-cercaZ;
                float distanciaCuadrada= distanciaX*distanciaX +distanciaZ*distanciaZ;

                if(distanciaCuadrada<RADIO_AUTO*RADIO_AUTO){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void configurarCamara(){
        if(camaraArea){
            super.configurarCamara();
            return;
        }

        float camaraX = autoX + (float) Math.sin(angulo)*12;
        float camaraZ = autoZ + (float) Math.cos(angulo)*12;

        vector("uOjo",camaraX,9,camaraZ);
        vector("uObjetivo",autoX,0.8f,autoZ);
        decimal("uAspecto",(float) ancho/alto);

    }

    private void dibujarTaller(){
     decimal("uEmision",1);
     cajaGirada(tallerX,0.1f,tallerZ,8,0.2f,8,0.1f,0.7f,1.0f,0);
     decimal("uEmision",0);
    }

    @Override
     protected void escena(){
        super.escena();
        dibujarTaller();
        dibujarAuto();
        for (AutoIA ia : trafico) {
            dibujarVehiculo(ia.x, ia.z, ia.angulo, ia.r, ia.g, ia.b, modoNoche);
        }
    }

    protected void dibujarAuto(){
        dibujarVehiculo(autoX, autoZ, angulo, 0.95f, 0.24f, 0.12f, farosOn);
    }

    protected void dibujarVehiculo(float ax, float az, float ang,float r, float g, float b, boolean faros){
        pieza(ax, az, ang, 0, 0.65f, 0, 1.65f, 0.55f, 2.6f, r, g, b);
        pieza(ax, az, ang, 0, 1.12f, 0.12f, 1.3f, 0.55f, 1.25f, 0.22f, 0.65f, 0.78f);
        float [] ladosRuedas = {-0.88f,0.88f};
        float [] ejeRuedas = {-0.82f,0.82f};

        for(float x: ladosRuedas){
            for(float z : ejeRuedas){
                pieza(ax, az, ang, x, 0.38f, z, 0.24f, 0.58f, 0.6f, 0.055f, 0.65f, 0.08f);
            }
        }

        float [] ladosFaros = {-0.55f,0.55f};
        for(float x: ladosFaros){
            if (faros) decimal("uEmision", 1);
            pieza(ax, az, ang, x, 0.68f, -1.32f, 0.38f, 0.2f, 0.07f, 1, 0.95f, 0.65f);
            decimal("uEmision", 0);
            pieza(ax, az, ang, x, 0.68f, 1.32f, 0.35f, 0.17f, 0.07f, 0.85f, 0.05f, 0.65f);
        }
    }

    private void pieza(float ax, float az, float ang,float x,float y, float z,float sx,float sy, float sz, float r, float g, float b ){
        float coseno = (float) Math.cos(ang);
        float seno = (float) Math.sin(ang);
        float mundoX = ax + coseno * x + seno * z;
        float mundoZ = az - seno * x + coseno * z;
        cajaGirada(mundoX,y,mundoZ,sx,sy,sz,r,g,b,ang);
    }

    @Override
    protected void prepararLuces(){
        super.prepararLuces();
        decimal("uFarosOn",farosOn ? 1:0);
        float coseno = (float) Math.cos(angulo);
        float seno = (float) Math.sin(angulo);
        float[] lados = {-0.55f, 0.55f};
        for (int i = 0; i < 2; i++) {
            float x = lados[i];
            float z = -1.5f;
            float mundoX = autoX + coseno * x + seno * z;
            float mundoZ = autoZ - seno * x + coseno * z;
            vector("uFaroPos[" + i + "]", mundoX, 1.0f, mundoZ);
        }
        vector("uFaroDir",-seno,-0.12f,-coseno);
    }

    @Override
    protected void dibujarMarcadoresMapa(){
        super.dibujarMarcadoresMapa();
        float seno = (float) Math.sin(angulo);
        float coseno = (float) Math.cos(angulo);
        for (AutoIA ia : trafico) {
            cajaGirada(ia.x, 19, ia.z, 3.5f, 1f, 6f, 0.3f, 0.8f, 1.0f, ia.angulo);
        }
        // cuerpo
        cajaGirada(autoX, 20, autoZ, 4f, 1f, 7f, 1.0f, 0.85f, 0.1f, angulo);
        // morro, adelantado en la dirección de avance
        cajaGirada(autoX - 3.5f * seno, 21, autoZ - 3.5f * coseno, 2.4f, 1f, 3f, 1.0f, 0.15f, 0.1f, angulo);
        cajaGirada(tallerX,17,tallerZ,6,1,6,0.1f,0.7f,1.0f,0);
    }

    private java.util.List<AutoIA> crearTrafico(){
        java.util.List<AutoIA> lista = new java.util.ArrayList<>();
        // 1) perímetro de la ciudad
        lista.add(new AutoIA(new int[][]{{0,10},{10,10},{10,0},{0,0}},
                9f, 0.20f, 0.45f, 0.95f));
        // 2) anillo interior, entre las manzanas de los lados
        lista.add(new AutoIA(new int[][]{{2,2},{2,8},{8,8},{8,2}},
                7f, 0.20f, 0.75f, 0.35f));
        // 3) circuito central, en sentido contrario al anillo
        lista.add(new AutoIA(new int[][]{{2,6},{2,4},{8,4},{8,6}},
                8f, 0.95f, 0.85f, 0.90f));
        return lista;
    }

    public static void main(String[] args){
        new Vehiculo().run();
    }
}

