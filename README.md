
## Respuestas Ejercicio SnakeRace

1. Uso de Hilos en el Código

El juego usa hilos para que cada serpiente se mueva de manera autónoma e independiente. Esto se puede observar en el uso de hilos en las siguientes clases:

### Clase Snake:

Implementa Runnable, lo que permite que cada serpiente se ejecute en un hilo separado.

En el método run(), hay un bucle while (!snakeEnd) que mantiene el hilo activo mientras la serpiente no haya chocado.

Dentro de run(), se llama a snakeCalc() para calcular el siguiente movimiento y luego se usa Thread.sleep(500) para hacer pausas entre movimientos.

### Clase SnakeApp 

Aquí se crean y manejan los hilos.

Se inicializan las serpientes y se crean los hilos con new Thread(snakes[i]).

Se usa thread[i].start() para iniciar cada hilo y permitir que las serpientes se muevan al mismo tiempo.

### Sincronización en el Tablero (Board y Cell)

Para evitar que varias serpientes ocupen la misma celda a la vez, en Cell se usa synchronized en algunos métodos como freeCell().

2. Prueba error: 

![Prueba de error, segunda pregunta](error.png)

3. 

Para solucionar las condiciones de carrera que planteamos anteriormente, hicimos unos cambios en el codigo anteriormente presentado: 

'En Board' 

 ```yaml 

private synchronized void GenerateTurboBoosts() 
		
private synchronized void GenerateBoard() 
		
private synchronized void GenerateBarriers() 
		
private synchronized void GenerateFood() 

 ```

En estos metodos no tenían ninguna protección para evitar condiciones de carrera. Esto significaba que múltiples hilos podían modificar los arreglos turbo_boosts, jump_pads, barriers, y food de manera simultánea.
En la actualizacion, hemos agregado "synchronized" a estos métodos, lo que asegura que solo un hilo pueda ejecutar cada uno de estos métodos en un momento dado. 
Esto elimina la posibilidad de que varios hilos intenten modificar las mismas celdas en el tablero al mismo tiempo, evitando problemas como la sobrescritura o el acceso a datos corruptos.


'En Cell' 

Añadimos nuevamente (synchronized) en todos los métodos que usen interacciones simultaneas (setBarrier, hasElements,setFull,setJump_pad,setFood,setTurbo_boost) para garantizar que el acceso a estos métodos se gestione correctamente 
en entornos multihilo, evitando asi condiciones de carrera.

'En Snake' 

En esta clase, cambiamos el metodo snakeCalc, asi: 

 ```yaml 
  private synchronized void snakeCalc() {
        head = snakeBody.peekFirst();

        newCell = head;

        newCell = changeDirection(newCell);

        randomMovement(newCell);

        checkIfFood(newCell);
        checkIfJumpPad(newCell);
        checkIfTurboBoost(newCell);
        checkIfBarrier(newCell);

        synchronized(snakeBody) {
            snakeBody.push(newCell);

            if (growing <= 0) {
                newCell = snakeBody.peekLast();
                snakeBody.remove(snakeBody.peekLast());
                Board.gameboard[newCell.getX()][newCell.getY()].freeCell();
            } else if (growing != 0) {
                growing--;
            }
        }
    }
```

Agregamos esta linea (synchronized(snakeBody)), esto permite que como el objeto snakeBody, que contiene el cuerpo de la serpiente, es una lista compartida entre los hilos. 
Si dos hilos intentaran modificar esta lista al mismo tiempo, podría causar condiciones de carrera.
Al usar synchronized(snakeBody), se asegura que solo un hilo acceda al cuerpo de la serpiente a la vez. Esto ayuda a mantener la integridad de los datos, asegurando que las modificaciones en snakeBody se realicen de forma segura y sin interferencias de otros hilos.
