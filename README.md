# Simulación de ciudad 3D con vehículo

Proyecto de Programación Gráfica en Java con LWJGL (OpenGL 3.3): una ciudad con calles, edificios, parques, semáforos y tráfico autónomo, por la que se conduce un auto.

## Diseño de la ciudad

La ciudad se representa con una matriz de 11x11 celdas, cada una de 10 unidades. Cada valor de la matriz representa un tipo de terreno:

| Valor | Terreno |
| --- | --- |
| `0` | Calles |
| `1` | Manzanas con edificios |
| `2` | Parques |

La distribución de las calles y manzanas permite conectar los distintos sectores de la ciudad. El escenario cuenta con:

- 21 manzanas de edificios de diferentes alturas y colores.
- 4 parques con árboles y bancos.
- 9 farolas ubicadas en las aceras de las manzanas, repartidas por la ciudad.
- 16 semáforos junto a las intersecciones interiores.
- Pasos peatonales junto a las manzanas.
- Un punto de destino para orientar el recorrido del auto.

Además, la ciudad dispone de ambientes diurno y nocturno y de una cámara aérea que permite observar la distribución general del escenario.

## Controles

| Teclas | Funcionalidad |
| --- | --- |
| `W`, `A`, `S`, `D` o `↑` `↓` `←` `→` | Controlar el movimiento y dirección del auto. |
| `Espacio` | Frenar el auto. |
| `C` | Alternar cámara del auto y de la ciudad. |
| `N` | Alternar entre ambiente diurno y nocturno. |
| `F` | Encender o apagar los faros del auto. |
| `M` | Mostrar u ocultar el minimapa. |
| `R` | Reiniciar el auto, el tráfico y la meta. |
| `Esc` | Salir de la simulación. |

## Funciones implementadas

- **Distribución urbana:** la ciudad se construye en `escena()` recorriendo la matriz `MAPA`. Primero se dibuja el suelo con el tamaño que tiene `LIMITE`; después, según el valor de cada celda, se decide qué se dibuja: si es `0`, una calle con sus marcas viales; si es `1` o `2`, una acera y sobre ella un edificio con ventanas o un parque con árboles y banco. Al terminar el recorrido se dibujan los elementos que no dependen de una celda, como las farolas y los semáforos. Los pasos peatonales no se dibujan celda por celda.

- **Iluminación:** la tecla `N` alterna `modoNoche`. Según su valor, `prepararLuces()` envía al shader la luz ambiental, la direccional y el color del cielo. De noche se activan las luces de las ventanas y de las farolas, que el shader trata como luces puntuales cuya intensidad baja con la distancia e iluminan el piso y los edificios cercanos.

  La tecla `F` enciende la luz de los faros del auto principal, que iluminan un cono hacia adelante con un alcance limitado. La luz la calcula el fragment shader con las normales de cada superficie. Los vehículos autónomos solo tienen los focos visibles y no iluminan el entorno.

- **Decoración:** dibujo de los pasos peatonales, los árboles, los asientos y las ventanas de los edificios.

- **Minimapa:** se dibuja después de la escena, en una esquina, con `scissor` para no borrar la vista principal. Su tamaño es el 30% del lado menor de la ventana, así se mantiene el encuadre al redimensionar. Muestra la ciudad, los autos (el principal y los autónomos) y el destino, con el norte arriba. Para ocultarlo solo hay que presionar `M`.

- **Reinicio:** devuelve todos los elementos a su lugar de origen: el destino vuelve a su color magenta, el auto regresa a su posición inicial y se restablecen la orientación, la velocidad y los vehículos autónomos.

- **Autos autónomos:** tres vehículos que siguen una lista de intersecciones por el centro de las calles, a un lado del eje. Giran de forma progresiva y frenan en las curvas. Como solo recorren calles, no atraviesan edificios ni salen del mapa.

- **Punto de destino:** un marcador magenta que aparece en una intersección del mapa. Al acercarse el auto cambia de color, de magenta a verde, en la ciudad y en el minimapa. Al presionar `R` vuelve al color magenta.

- **Semáforos:** siguen el ciclo verde, amarillo y rojo controlado por `tiempoMundo`, con desfases distintos en cada cruce.

## Limitaciones

- Los autos autónomos no chocan entre sí ni con el auto principal, y no respetan los semáforos.
- Los semáforos son solo visuales.
- Los focos de los autos autónomos son visibles, pero no iluminan el entorno: el shader solo calcula dos focos, que son los del auto principal.
- Hay una única meta.
- La velocidad se muestra solo en el título de la ventana.
- El árbol, el banco, la farola y el semáforo no tienen colisión propia porque están dentro de las aceras, que el auto no puede pisar.
- Algunas medidas están fijas en el código, como `LIMITE = 55`, el `57.0` del minimapa y la distancia de la cámara aérea. Si se cambia el tamaño del mapa hay que actualizarlas a mano.
- El proyecto incluye solo las librerías nativas de Windows.

## Instrucciones de ejecución

### Requisitos

- Java 17 y Maven.
- Tarjeta gráfica compatible con OpenGL 3.3.

### Con IntelliJ IDEA

1. Abrir el proyecto.
2. Esperar a que Maven descargue todas las dependencias (LWJGL 3.3.3).
3. Abrir `Vehiculo.java` y ejecutar su método `main`.

### Con VS Code

1. Entrar a la carpeta del proyecto.
2. Ejecutar con:

   ```bash
   mvn compile exec:exec
   ```

> **Nota:** en ambos casos se debe ejecutar la clase `Vehiculo`, que extiende `App` y añade el auto, los autos autónomos y la meta. Ejecutar solo `App` muestra la ciudad sin vehículos.
