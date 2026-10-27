# Ashborn Rise — Fase B (código)

RPG por turnos en Java + Swing, sin librerías externas. Implementa el diseño de la Fase A (diagrama de clases + 3 diagramas de secuencia).

## Cómo correrlo

- **IntelliJ / Eclipse / NetBeans:** crear un proyecto Java, marcar `src` como carpeta de código fuente y ejecutar `ashbornrise.Main`. Java 17 o superior.
- **Consola:**
  ```
  javac -encoding UTF-8 -d out $(find src -name "*.java")
  java -cp out ashbornrise.Main
  ```
  (En Windows: `dir /s /b src\*.java > fuentes.txt` y después `javac -encoding UTF-8 -d out @fuentes.txt`.)

La partida se guarda en `savegame.json`, en la carpeta desde donde se ejecuta el juego.

Los sprites están dibujados en código (`vista/Sprites.java`). Para reemplazar uno por una imagen, poné un PNG con el nombre de la clase en `sprites/` (por ejemplo `sprites/Mago.png`) en la carpeta desde donde se ejecuta el juego.

## Estructura (MVC)

```
ashbornrise/
├── Main.java                 arma y conecta Vista, Controladores y Persistencia
├── modelo/                   lógica del juego (no conoce a la vista)
│   ├── Combatiente, Batalla, GestorTurnos, ComparadorPorVelocidad, Equipo, Inventario,
│   │                         Partida, Recompensa
│   ├── FabricaJuego          party inicial, ítems, enemigos y botín de cada batalla
│   ├── EstadoBatalla, TipoRecurso
│   ├── personajes/           Personaje, Guerrero, Mago, Arquero, Curador, TipoElemento
│   ├── enemigos/             Enemigo, GoblinMerodeador, EsqueletoGuerrero, ChamanOscuro,
│   │                         AranaVenenosa, SenorDeLasSombras
│   ├── habilidades/          Habilidad, HabilidadOfensiva, HabilidadCuracion, HabilidadEstado, TipoAlcance
│   ├── estados/              EstadoEfecto, Veneno, Escudo, Aturdimiento, TipoEstado
│   ├── items/                Item, Consumible, TipoEfectoConsumible
│   └── acciones/             Accion, AccionAtacar, AccionDefender, AccionHabilidad, AccionItem
├── persistencia/             IPersistencia, PersistenciaArchivoJSON
├── controlador/              ControladorJuego, ControladorBatalla,
│                             IVentanaJuego e IVistaBatalla (lo que los controladores necesitan de la vista),
│                             ContinuarTurnos (el Runnable que la vista ejecuta al terminar una animación)
└── vista/                    VentanaPrincipal, VistaInicio, VistaJuego (estado del equipo),
                              VistaBatalla, VistaResultados, Estilo,
                              Sprites (pixel art de cada combatiente, dibujado en código)
    └── arena/                arena 3D de la batalla, hecha a mano sin librerías:
                              PanelArena (escena y cámara), Motor3D (rasterizador por software),
                              ModeloVoxel (cada sprite convertido en un modelo de vóxeles),
                              Escenario (coliseo, público, antorchas), Coreografia (animaciones),
                              Actor3D, Particula, TextoFlotante, Linea (línea de tiempo)
diagramas/                    diagrama de clases, diagrama del paquete arena y 3 de secuencia
                              (.mmd editable + .png + .svg)
```

La Vista solo **lee** el modelo para dibujarlo. Todo lo que **cambia** el modelo pasa por un controlador.

## Arena 3D

La batalla se ve en un coliseo 3D. Cada personaje es su sprite convertido en vóxeles (un bloque por píxel, más grueso en el centro del cuerpo), así se ve en 3D pero respeta el dibujo. Cada ataque, habilidad, ítem, defensa, veneno, aturdimiento y derrota tiene su animación. Los enemigos se eligen tocándolos en la arena.

Para animar, `Batalla` recuerda la última acción (`getUltimoActor()`, `getUltimaAccion()`) y `ControladorBatalla` hace jugar a los enemigos **de a uno**: ejecuta un turno, la vista lo anima y al terminar ejecuta el `Runnable` que recibió (`ContinuarTurnos`), que le avisa al controlador que siga con el próximo. La vista muestra los cambios (daño, estados) recién en el momento del impacto de la animación.

## Cómo funciona un turno

1. `Batalla.iniciar()` arma la cola de la ronda con `GestorTurnos`, que ordena con `ComparadorPorVelocidad` (de mayor a menor velocidad; los empates se sortean).
2. Al comienzo del turno de cada combatiente se llama a `iniciarTurno()`: se aplican sus estados (el Veneno hace daño, el Aturdimiento le hace perder el turno) y el Maná se regenera.
3. Si le toca a un **personaje**, la vista espera que el jugador elija. El controlador arma la `Accion` y llama a `batalla.ejecutarTurno(actor, accion)`.
4. Si le toca a un **enemigo**, `ControladorBatalla` llama a `batalla.ejecutarTurnoEnemigo()` y el enemigo elige con `decidirAccion(batalla)`.
5. Después de cada acción, `verificarFin()`. Cuando gana el jugador, `otorgarRecompensas()` crea la `Recompensa`, reparte la experiencia y guarda oro e ítems.

## GRASP: dónde está cada principio en el código

| Principio | Dónde |
|---|---|
| **Experto** | `Combatiente.atacar/recibirDanio` (cada uno calcula con sus datos), `EstadoEfecto` (su efecto y su duración), `Personaje.puedeUsar`, `Batalla.resolverObjetivos` (conoce a todos los combatientes) |
| **Creador** | `Batalla` crea la `Recompensa`; `Partida.crearSiguienteBatalla()` crea la `Batalla`; `HabilidadEstado` crea el `EstadoEfecto` que aplica |
| **Controlador** | `ControladorJuego` y `ControladorBatalla` reciben todos los eventos de la interfaz |
| **Polimorfismo** | `Enemigo.decidirAccion`, `Habilidad.ejecutar`, `Accion.ejecutar`, `Item.usar`, `Personaje.aplicarMejorasDeNivel`, `Arquero.calcularDanio` (crítico), `Curador.getPoderCuracion`, `Guerrero.reaccionarAAtaque` (contraataque), `EstadoEfecto.modificarDanioRecibido / impideActuar` (sin `instanceof`) |
| **Fabricación Pura** | `GestorTurnos`, `Recompensa`, `PersistenciaArchivoJSON`, `FabricaJuego` |
| **Variación Protegida** | `IPersistencia` (se puede cambiar el formato sin tocar nada más), `IVistaBatalla` e `IVentanaJuego` (se puede cambiar la vista sin tocar los controladores), `ComparadorPorVelocidad` (otro criterio de orden sin tocar `GestorTurnos`), `Habilidad` y `Accion` (se agregan nuevas sin tocar `Batalla`) |
| **Indirección** | Los controladores entre Vista y Modelo; `ContinuarTurnos` entre la vista y `ControladorBatalla` |
| **Bajo Acoplamiento (MVC)** | Los controladores no importan nada de Swing: dependen de `IVistaBatalla` e `IVentanaJuego` |
| **Bajo acoplamiento** | `Batalla.robarOro()` / `consumirItem()`: los enemigos y acciones no tocan el `Inventario` directamente |

## Decisiones tomadas (para la defensa)

- **Las habilidades concretas son objetos, no clases.** "Bola de Fuego" es `new HabilidadOfensiva("Bola de Fuego", 30, MANA, VARIOS_ENEMIGOS, 1.2, 3)`. Cada clase de personaje crea las suyas en su constructor.
- **Las habilidades las pueden usar personajes y enemigos.** `Combatiente.usarHabilidad()` ejecuta la habilidad sin costo (así la usa el Chamán Oscuro con su "Curación Oscura"). `Personaje` lo redefine para descontar primero el maná o la energía.
- **Energía:** +8 al atacar, +5 al recibir un golpe, +10 al defender. Arranca a la mitad. **Maná:** se regenera cada turno y se llena después de cada victoria.
- **Experiencia:** subir del nivel N al N+1 cuesta `50 × N`. La experiencia de la batalla se reparte entre los personajes que siguen en pie.
- **Entre batallas:** se quitan los estados y los caídos vuelven con el 25% de la vida. Al perder se puede reintentar (el equipo se recupera entero) o cargar la última partida.
- **Guardado:** se guarda el estado básico (progreso, oro, ítems y, por personaje: clase, nombre, nivel, experiencia, vida y recurso). Al cargar, el personaje se crea en nivel 1 y se sube hasta el nivel guardado, así quedan las mismas estadísticas sin tener que guardarlas. El JSON se escribe y se lee a mano (sin librerías): un dato por línea.
- **Jefe final:** al bajar del 50% de vida entra en fase 2, su ataque sube un 40%, va contra el aliado más débil y sus golpes pueden aturdir.

## Fórmula de daño (para agregar al documento de diseño)

1. **Daño bruto** (`calcularDanio()`): es el Ataque de quien golpea. El Arquero lo redefine: con un 15% de probabilidad el daño bruto se duplica (golpe crítico).
2. **Ataque básico:** daño bruto = `calcularDanio()`.
   **Habilidad ofensiva:** daño bruto = `calcularDanio() × multiplicadorDanio` (redondeado), aplicado a cada objetivo.
3. **Defensa del objetivo** (`recibirDanio()`): daño = daño bruto − Defensa / 2 (división entera), **mínimo 1**.
4. **Defender:** si el objetivo eligió Defender en su último turno, el daño se divide por 2 (mínimo 1).
5. **Escudo:** si el objetivo tiene Escudo, absorbe daño hasta agotar sus puntos. El resto se descuenta de la vida (nunca baja de 0).
6. El **Veneno** no pasa por la defensa: resta su daño directo al comienzo del turno del afectado.

*Ejemplo:* Bram (Ataque 18) golpea a un Goblin (Defensa 6): 18 − 3 = **15** de daño. Con Golpe Devastador (×1,8): 32 − 3 = **29**.

## Devolución del profesor (Fase A, nota 8/10): qué se corrigió

| Observación | Cómo quedó |
|---|---|
| Falta la fórmula de daño y cómo interviene la Defensa | Implementada en `Combatiente.calcularDanio()` y `recibirDanio()`. El texto para el documento está en la sección anterior. |
| `Habilidad.ejecutar()` recibe `Personaje`, conviene `Combatiente` | Ahora es `ejecutar(origen: Combatiente, objetivos: List<Combatiente>)`. `usarHabilidad()` y `getPoderCuracion()` pasaron a `Combatiente`. El Chamán Oscuro usa una `HabilidadCuracion` real, así que el cambio se aprovecha. Se eliminó `AccionCurarAliado`. |
| `Personaje.usarItem(i)` no recibe objetivo | Ahora es `usarItem(i: Item, objetivo: Personaje)`. El objetivo lo elige el jugador en `VistaBatalla` (tocando un aliado) o en `VistaJuego` (lista desplegable). Llega por `ControladorBatalla.usarItem(item, objetivo)` → `AccionItem` → `Personaje.usarItem` → `Item.usar(objetivo)`. |
| `decidirAccion()` recibe `EstadoBatalla`, que no está modelado | Ahora recibe `Batalla` (necesita ver a los aliados vivos para elegir objetivo). `EstadoBatalla` sigue existiendo como enum del estado de la batalla y está en el diagrama. |
| Faltan `Accion`, `EstadoBatalla`, `TipoAlcance`, `TipoEfectoConsumible` | Están en el código y en el diagrama nuevo, con todas sus subclases y valores. |
| Secuencia 1 y 2: faltan `VistaBatalla`, `ControladorBatalla`, `AccionAtacar`, `AccionHabilidad`, `calcularDanio()`, `actualizarVista()`, `mostrarError()` | El diagrama de clases nuevo incluye controladores, vistas y acciones con esas operaciones. Los diagramas de secuencia se rehicieron con los mensajes reales del código. |

Además, en el diagrama de clases nuevo:
- Los atributos de las subclases (`faseActual`, `robaOro`, `probabilidadCritico`, etc.) pasan de `+` a `-` (privados), como están en el código.
- Se agregaron `ControladorJuego`, `VentanaPrincipal`, `VistaInicio`, `VistaJuego`, `VistaResultados`, `Estilo`, `Main`, `FabricaJuego`, `TipoAlcance` y el enum `Modo` de la vista.
- En el diagrama de secuencia de guardado se sacó `obtenerEstado()`, que no existe en el código: `PersistenciaArchivoJSON` usa los getters de `Partida`.
- No se dibujan los getters simples, los métodos privados de las vistas ni los componentes de Swing (botones, etiquetas).

Los archivos `.mmd` de la carpeta `diagramas` se pueden editar y volver a exportar en https://mermaid.live.
