package com.primerparcial;

public class AutoIA {
  private static final float CARRIL= 2.0f;
  private static final float RADIO_LLEGADA = 3.0f;
  private static final float GIRO_MAX = 2.6f;
  private static final float PI = (float) Math.PI;

  final float r, g, b;
  float x,z,angulo,velocidad;

  private final int[][] ruta;
  private final float velocidadCrucero;
  private int destino;

  AutoIA(int[][] ruta,float velocidadCrucero,float r,float g, float b){
      this.ruta=ruta;
      this.velocidadCrucero=velocidadCrucero;
      this.r=r;
      this.g=g;
      this.b=b;
      this.reiniciar();
  }

  static float centro(int indice){
    return - App.LIMITE + App.CELDA*(indice+0.5f);
  }

  private float[] objetivo(int d){
      int n = ruta.length;
      int p = (d-1+n)%n;
      float nx = centro(ruta[d][1]);
      float nz = centro(ruta[d][0]);
      float px = centro(ruta[p][1]);
      float pz = centro(ruta[p][0]);
      float fx = nx-px;
      float fz = nz-pz;
      float largo = (float) Math.sqrt(fx*fx + fz*fz);
      fx/=largo;
      fz/=largo;
      return new float[]{nx+(-fz)*CARRIL,nz+fx*CARRIL};
  }

  public void reiniciar(){
      float[] t = objetivo(1);
      x = centro(ruta[0][1]) + (t[0]-centro(ruta[1][1]));
      z = centro(ruta[0][0]) +(t[1]-centro(ruta[1][0]));
      destino = 1;
      velocidad = 0;
      angulo = (float) Math.atan2(-(t[0]-x),-(t[1]-z));
  }

    void actualizar(float dt) {
        float[] t = objetivo(destino);
        float dx = t[0] - x, dz = t[1] - z;
        if (Math.sqrt(dx * dx + dz * dz) < RADIO_LLEGADA) {
            destino = (destino + 1) % ruta.length;
            t = objetivo(destino);
            dx = t[0] - x;
            dz = t[1] - z;
        }
        // misma convención que Vehiculo: frente = (-sin(angulo), -cos(angulo))
        float deseado = (float) Math.atan2(-dx, -dz);
        float dif = deseado - angulo;
        while (dif > PI) dif -= 2 * PI;
        while (dif < -PI) dif += 2 * PI;
        float maximo = GIRO_MAX * dt;
        angulo += Math.max(-maximo, Math.min(maximo, dif));

        float velObjetivo = Math.abs(dif) > 0.5f ? velocidadCrucero * 0.55f : velocidadCrucero;
        velocidad += (velObjetivo - velocidad) * Math.min(1f, 3f * dt);

        x += -(float) Math.sin(angulo) * velocidad * dt;
        z += -(float) Math.cos(angulo) * velocidad * dt;
    }
}
