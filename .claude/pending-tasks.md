# Pending Task — Frontend Damm Logistics

**Fecha de creación:** 2026-05-09
**Estado:** Pendiente de ejecutar en próxima sesión con contexto fresco

---

## Instrucción original del usuario (literal)

> Quiero que hagas un nuevo front end en un nuevo repositorio en local y que tenga las tres visiones necesarias y que tengan las ventanas necesarias que te he pasado anteriormente. En la visión del packer ha de haber la visión del pedido que está haciendo, el palet en el cual está y el producto que está haciendo, también que tenga un botón de confirmación de acción hecha y que tenga después en qué parte del palet lo ha de poner y cómo y se pueda visualizar. Cuando tenga el palet lleno que sepa en qué parte del camión ha de ponerlo, también que se adapte cada imagen de cómo poner el palet a cada tipo de tamaño de camión 3 pallets, 6 y 8. Finalmente que confirme del pedido hecho y pase al siguiente.
>
> Con el conductor una pantalla que ponga la ruta en línea y te ponga las paradas e información crucial, también que tenga cuando pares la información del cliente de dónde descargar y qué tipos de productos coger para dárselo al cliente, también que tenga una pantalla que confirme el usuario que el albarán ha sido entregado, de qué forma ha sido pagado y si han habido incidencias y enviarlo al back. También quiero la siguiente pantalla que sepa los productos a recoger y en qué sitio del camión poner y que confirme que la localización está hecha y que pase a la siguiente.
>
> También quiero un botón de incidencias que en todo momento puedas enviar al back la incidencia que ha pasado (definidas en el back) y que se creen logs al respecto.
>
> Quiero finalmente que adaptes el formato web para una pantalla de móvil estándar tipo Xiaomi 13T o iPhone 13.
>
> También quiero que si has de hacer cambios en el back para adaptar al front lo hagas con criterio.
>
> Finalmente dime sin codificar una forma de mejorar el algoritmo de optimización de todos los tipos del picking, puesta en el camión y conducción y descarga. Que envíes un plan de cómo hacerlo y me digas si es posible con el proyecto actual y hagas un plan para ti mismo entenderlo si decido hacerlo.
>
> Quiero que la interfaz sea pixel perfect, atrayente y con una paleta de colores atrayente y relacionada con los colores Damm.

---

## Desglose en tareas

### 1. Setup del nuevo repositorio frontend
- [ ] Crear nuevo repo local (sugerencia: `damm-logistics-frontend/` al lado del backend)
- [ ] Stack: **React 18 + Vite + TypeScript + Tailwind CSS** (mobile-first)
- [ ] Librerías clave:
  - React Router v6 (navegación)
  - Axios o fetch wrapper para API
  - Zustand o Context para estado global (auth, ruta activa)
  - React Hook Form + Zod (formularios y validación)
  - Lucide React (iconos)
  - Framer Motion (transiciones suaves)
  - QR scanner (`html5-qrcode`) para empaquetador
- [ ] Configurar interceptor axios para JWT
- [ ] Configurar viewport: `375x812` (iPhone 13) y `393x873` (Xiaomi 13T)

### 2. Paleta Damm (referencia)
- **Rojo Estrella Damm:** `#E30613` (primario)
- **Negro:** `#000000`
- **Dorado/Amarillo:** `#F5C518` (acento, retornables)
- **Blanco roto:** `#FAFAFA` (fondo)
- **Verde éxito:** `#16A34A` (entrega ok)
- **Naranja warning:** `#F59E0B` (incidencias)
- Tipografía: Inter o similar sans-serif moderna

### 3. Vista LOGIN (compartida)
- [ ] Pantalla con logo Damm, fondo rojo
- [ ] Campos username + password
- [ ] Botón "Iniciar sesión" → `POST /api/auth/login`
- [ ] Redirige según rol: ADMIN → `/admin`, EMPAQUETADOR → `/packer`, CONDUCTOR → `/driver`

### 4. Vista EMPAQUETADOR (`/packer`)
Pantallas en orden (basadas en imagen 2 con notas manuscritas):

**P1 - Lista de tareas pendientes**
- [ ] `GET /api/packing/tasks` → lista de rutas a empaquetar
- [ ] Card por ruta con: código ruta, cliente destino, número palets, peso

**P2 - Pantalla principal de picking (se repite por cada producto)**
- [ ] Header: progreso (`Ruta X/30, Palet X/6, Producto X/7`)
- [ ] Imagen producto + SKU + nombre + cantidad a recoger
- [ ] Ubicación en almacén (warehouseLocation del DTO)
- [ ] Botón **"Confirmar acción hecha"** ✓
- [ ] Foto del palet/QR del palet destino

**P3 - Estructura del palet (visualización 3D)**
- [ ] Render del palet con capas de cajas según `PalletItem.layerNumber`
- [ ] Indicar dónde colocar el producto actual (resaltar capa/posición)
- [ ] Vista isométrica simple (CSS 3D transforms o Three.js si necesario)

**P4 - Ubicación del palet en el camión**
- [ ] Cuando palet esté lleno, mostrar layout del camión
- [ ] Adaptar a 3 tipos: **3, 6 u 8 palets**
- [ ] Resaltar con animación dónde poner el palet (basado en `Pallet.positionXCm/positionYCm`)
- [ ] Botón "Palet colocado" → `POST /api/packing/pallets/{id}/loaded`

**P5 - Confirmación pedido completo**
- [ ] Cuando todos los palets de la ruta estén cargados → "Pedido X completado"
- [ ] Pasa al siguiente pedido automáticamente

### 5. Vista CONDUCTOR (`/driver`)
Pantallas en orden (basadas en imagen 1):

**D1 - Pantalla principal con ruta en línea**
- [ ] `GET /api/driver/route/{driverId}` → ruta activa
- [ ] Vista timeline vertical con todas las paradas
- [ ] Info crucial visible: nombre cliente, dirección, hora estimada, peso a entregar
- [ ] Resaltar parada actual
- [ ] Mapa embed (Google Maps o Leaflet) opcional con la ruta

**D2 - Detalle de parada**
- [ ] Click en parada → muestra info completa del cliente
- [ ] Lista de productos a descargar (con cantidades)
- [ ] Productos retornables a recoger
- [ ] Botón "He llegado"

**D3 - Productos a descargar del camión**
- [ ] Vista del camión con palets resaltados que toca descargar
- [ ] Indicar posición (X,Y) en el camión
- [ ] Confirmar que ha cogido el palet correcto → `POST /api/packing/pallets/{id}/unloaded`

**D4 - Confirmación de entrega + albarán**
- [ ] Lista de productos entregados (poder ajustar cantidades si no se entrega todo)
- [ ] Cantidad de retornables (envases vacíos) recogidos
- [ ] **Método de pago**: CASH / CARD (toggle/radio buttons)
- [ ] Importe cobrado (input numérico)
- [ ] Firma del cliente (canvas signature pad)
- [ ] Checkbox "¿Hay incidencia?" → si sí, abrir modal de incidencia
- [ ] Botón "Confirmar entrega" → `POST /api/driver/deliver/{driverId}` con `DeliveryConfirmationRequest`
- [ ] Pasa a la siguiente parada

### 6. Botón INCIDENCIAS (flotante, siempre visible)
- [ ] FAB rojo flotante en esquina inferior derecha
- [ ] Tap → modal con dropdown de tipos (CLIENT_ABSENT, WRONG_PRODUCT, DAMAGED_PRODUCT, ACCESS_BLOCKED, PAYMENT_REFUSED, PARTIAL_DELIVERY, RETURNABLES_MISMATCH, OTHER)
- [ ] Campo descripción + foto opcional
- [ ] Envío al back → crear endpoint `POST /api/incidences` (cambio en back necesario, ver sección 8)
- [ ] Toast confirmación + log local

### 7. Vista ADMIN (`/admin`) - opcional pero útil
- [ ] Dashboard: `GET /api/admin/dashboard`
- [ ] Listado rutas del día con estado
- [ ] Optimizar nueva ruta: `POST /api/routes/optimize`
- [ ] Gestión incidencias pendientes
- [ ] Gestión clientes/productos/camiones

### 8. Cambios necesarios en el BACKEND (con criterio)
- [ ] **Nuevo endpoint:** `POST /api/incidences` (estándalone, no atado a delivery) para el botón flotante
- [ ] **Nuevo endpoint:** `POST /api/driver/arrive/{stopId}` para marcar llegada a parada (actualmente solo se hace en deliver)
- [ ] **Nuevo endpoint:** `GET /api/trucks/{id}/layout` que devuelva el layout del camión adaptado (3/6/8 palets) para que el front pinte correctamente
- [ ] **Añadir campo `truckSize`** en Truck (SMALL_3, MEDIUM_6, LARGE_8) o derivar de dimensiones
- [ ] **Endpoint upload de fotos:** `POST /api/deliveries/{id}/photo` (multipart) — actualmente solo se guarda URL
- [ ] **CORS:** Ya está configurado para localhost:3000/5173 ✓
- [ ] **WebSocket** (opcional): `/ws/route-progress` para que admin vea en tiempo real

### 9. Mobile-first y pixel perfect
- [ ] Todas las vistas optimizadas para 375-393px de ancho
- [ ] Bottom navigation bar para empaquetador y conductor
- [ ] Safe area insets (iPhone notch)
- [ ] PWA opcional (instalable, offline básico)
- [ ] Componentes con áreas táctiles >= 44px (HIG Apple)
- [ ] Animaciones 60fps
- [ ] Loading skeletons en lugar de spinners

---

## Plan de mejora del algoritmo de optimización (sin código)

### Estado actual del proyecto
**Algoritmos actuales:**
1. K-Means para clustering geográfico
2. Nearest Neighbor + 2-opt para TSP
3. First Fit Decreasing para bin packing de palets
4. Heurística simple de balanceo de peso (swap iterativo)
5. Fórmula lineal de consumo combustible

**Limitaciones actuales:**
- K-Means no respeta capacidad del camión (puede crear clusters demasiado pesados)
- 2-opt es óptimo local, no global
- Bin packing 1D (solo peso/altura), no considera dimensiones reales 3D
- El balance de peso es post-hoc (después de empaquetar)
- No considera ventanas horarias de entrega
- No considera tráfico real ni restricciones urbanas (zonas peatonales, etc.)
- Picking en almacén ignora la ubicación física de productos (warehouse layout)

### Plan de mejora propuesto

**Fase 1 — Mejora del routing (4-6 días)**
1. Cambiar de TSP a **VRP (Vehicle Routing Problem) con capacidades y ventanas horarias**
   - Usar **Google OR-Tools Java** (open source, gratis)
   - Soporta: capacidad, ventanas horarias, múltiples vehículos, time-dependent travel times
   - Retorna asignación de pedidos→camiones + orden óptimo
2. Integrar **Google Distance Matrix API real** (ya está el GoogleMapsService preparado, solo falta API key)
3. Considerar **tráfico en tiempo real** con `departure_time=now` en la API

**Fase 2 — Mejora del pallet packing (3-4 días)**
1. Implementar **3D Bin Packing real** con algoritmo **EB-AFIT** o **Maximal Rectangles**
   - Considera dimensiones reales (ancho, fondo, alto) no solo peso
   - Maximiza ocupación del palet
2. **Stack stability**: productos pesados abajo, frágiles arriba
3. **LIFO loading**: integrar con orden de paradas para que la primera entrega esté accesible

**Fase 3 — Mejora del camión (2-3 días)**
1. Optimización integrada **palet-en-camión + balance peso** simultánea
   - Programación por restricciones (CP-SAT de OR-Tools)
   - Restricciones: peso máx eje, CoG dentro de tolerancia, palets de misma parada juntos
2. Visualización 3D real del camión con Three.js (front)

**Fase 4 — Mejora del picking (3-4 días)**
1. Añadir entidad **WarehouseLocation** (pasillo, estantería, balda)
2. **Algoritmo de picking optimizado** tipo **S-shape** o **Largest Gap** (estándar en logística)
3. Calcular ruta de picking más corta dentro del almacén
4. Frontend muestra mapa del almacén con ruta resaltada

**Fase 5 — Mejora de descarga (2 días)**
1. Pre-calcular orden de descarga en el cliente (productos pesados/voluminosos primero)
2. Indicar al conductor el orden óptimo de descarga
3. Tracking de tiempo medio de descarga por cliente para mejorar estimaciones

**Fase 6 — Machine Learning (opcional, futuro)**
1. Histórico de rutas reales vs estimadas → entrenar modelo de tiempo real
2. Predicción de demanda por cliente (cuándo volverá a pedir)
3. Detección de anomalías en pedidos (cantidades atípicas)

### ¿Es posible con el proyecto actual?
**Sí**, totalmente. La arquitectura está preparada:
- Servicios bien separados (cada algoritmo en su Service)
- Solo hay que sustituir las implementaciones de `RouteService` y `LoadService`
- OR-Tools tiene bindings Java que se añaden al `build.gradle`
- El modelo JPA ya tiene los campos necesarios (lat/lng, dimensiones, peso)

### Plan para que YO lo entienda en una próxima sesión
Si decides hacerlo, lee en este orden:
1. Este fichero (visión general)
2. `RouteService.java` y `LoadService.java` (qué hay actualmente)
3. Documentación de OR-Tools VRP: https://developers.google.com/optimization/routing/vrp
4. Las imágenes manuscritas del usuario (flujos UX)
5. Empezar por Fase 1 (routing) que es la que más impacto tiene

---

## Notas importantes
- El backend ya está completo y funcionando
- Branch actual: `claude/pedantic-mestorf-b3cfb7`
- Worktree: `D:\AA_Desarollo backend\hackathon_damm\spring-hackaton-template\.claude\worktrees\pedantic-mestorf-b3cfb7`
- Sample data en `DataInitializer.java` (4 users, 8 clientes BCN/Mollet, 7 productos, 2 camiones, 8 pedidos)
- Credenciales de prueba: admin/admin123, empaquetador1/pack123, conductor1/drive123, conductor2/drive123
- Backend en `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console`
