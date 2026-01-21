CU-01 Registrar comisión

TC-01-01 Registro Exitoso
Pre: Cliente logueado, formulario de creación disponible.
Pasos: 1) Ir a “Crear comisión”. 2) Completar todos los campos obligatorios. 3) Confirmar.
Resultado esperado: Comisión creada en estado “Pendiente”.
TC-01-02 Campos Vacíos
Pre: Formulario abierto.
Pasos: 1) Dejar campos obligatorios vacíos. 2) Intentar guardar.
Resultado esperado: Sistema muestra alerta de campos obligatorios.
TC-01-03 Tiempo de Carga
Pre: Conexión estable.
Pasos: 1) Completar datos. 2) Medir tiempo hasta confirmar.
Resultado esperado: Registro completado en < 3 segundos.
TC-01-04 Validar Participante
Pre: Datos del cliente listos.
Pasos: 1) Ingresar datos de participantes. 2) Validar con DB.
Resultado esperado: Participantes registrados correctamente.
TC-01-05 Error de Conexión
Pre: Simular caída de DB.
Pasos: 1) Intentar registrar comisión.
Resultado esperado: Mensaje de error “Intente nuevamente más tarde”.
CU-02 Actualizar estado de comisión

TC-02-01 Transición Lógica
Pre: Comisión asignada.
Pasos: 1) Seleccionar comisión. 2) Cambiar estado a “EN_PROGRESO”. 3) Guardar.
Resultado esperado: Estado actualizado y log de auditoría generado.
TC-02-02 Estado Terminal
Pre: Comisión cancelada.
Pasos: 1) Intentar editar estado.
Resultado esperado: Edición bloqueada por estado terminal.
TC-02-03 Notificación
Pre: Cliente con sesión activa.
Pasos: 1) Cambiar estado a “EN_REVISIÓN”.
Resultado esperado: Cliente recibe notificación automática.
TC-02-04 Habilitar Entrega
Pre: Comisión en curso.
Pasos: 1) Cambiar estado a “EN_REVISIÓN”.
Resultado esperado: Se habilita adjuntar entregables.
TC-02-05 Velocidad de Respuesta
Pre: Actor logueado.
Pasos: 1) Confirmar cambio de estado.
Resultado esperado: Actualización reflejada en < 2 segundos.
CU-03 Aprobar o cancelar comisión

TC-03-01 Aprobación Directa
Pre: Comisión “Pendiente”.
Pasos: 1) Aprobar comisión. 2) Confirmar.
Resultado esperado: Estado cambia a “Aprobada”.
TC-03-02 Rechazo con Motivo
Pre: Comisión “Pendiente”.
Pasos: 1) Cancelar. 2) Ingresar razón.
Resultado esperado: Estado “Cancelada” y motivo registrado.
TC-03-03 Concurrencia
Pre: Dos usuarios activos.
Pasos: 1) Usuario A aprueba. 2) Usuario B cancela simultáneamente.
Resultado esperado: Mensaje de bloqueo por concurrencia.
TC-03-04 Historial de Actor
Pre: Acción realizada.
Pasos: 1) Consultar historial.
Resultado esperado: Se muestra actor y timestamp.
TC-03-05 Validar Pendiente
Pre: Comisión “Aprobada”.
Pasos: 1) Forzar aprobación.
Resultado esperado: Sistema rechaza acción.
CU-04 Consultar y filtrar comisiones

TC-04-01 Detalle Completo
Pre: Lista cargada.
Pasos: 1) Ver detalle.
Resultado esperado: Muestra plazos y requerimientos.
TC-04-02 Filtro de Estado
Pre: Existen comisiones.
Pasos: 1) Filtrar canceladas.
Resultado esperado: Solo registros cancelados.
TC-04-03 Sin Resultados
Pre: Sin coincidencias para filtro.
Pasos: 1) Aplicar filtro sin match.
Resultado esperado: Mensaje de lista vacía.
TC-04-04 Privacidad de Datos
Pre: Artista logueado.
Pasos: 1) Ver lista.
Resultado esperado: No se muestran comisiones ajenas.
TC-04-05 Acceso Rápido
Pre: Sesión iniciada.
Pasos: 1) Seleccionar comisión desde link directo/panel.
Resultado esperado: Redirección correcta.
CU-05 Registrar motivo de cancelación

TC-05-01 Texto Requerido
Pre: Formulario de rechazo abierto.
Pasos: 1) Intentar cancelar sin escribir motivo.
Resultado esperado: Formulario impide envío y marca error.
TC-05-02 Persistencia de Razón
Pre: Registro enviado.
Pasos: 1) Ver historial de cancelación.
Resultado esperado: Motivo guardado con fecha y usuario.
TC-05-03 Notificación al Cliente
Pre: Cliente notificado configurado.
Pasos: 1) Ver notificación como cliente.
Resultado esperado: Mensaje incluye el motivo de cancelación.
TC-05-04 Capacidad de Texto
Pre: Formulario de cancelación.
Pasos: 1) Ingresar explicación larga. 2) Guardar.
Resultado esperado: Sistema almacena el texto completo sin truncar.
TC-05-05 Abortar Cancelación
Pre: Formulario abierto.
Pasos: 1) Clic en botón “Cerrar” o “Volver”.
Resultado esperado: La comisión sigue “Pendiente” sin cambios.