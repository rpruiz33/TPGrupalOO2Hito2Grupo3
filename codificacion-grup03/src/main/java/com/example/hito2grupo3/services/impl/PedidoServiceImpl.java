package com.example.hito2grupo3.services.impl;

import com.example.hito2grupo3.dto.ItemPedidoDTO;
import com.example.hito2grupo3.dto.PedidoAltaDTO;
import com.example.hito2grupo3.dto.PedidoResponseDTO;
import com.example.hito2grupo3.dto.PlatoDTO;
import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.entities.ItemPedido;
import com.example.hito2grupo3.entities.Pedido;
import com.example.hito2grupo3.entities.Plato;
import com.example.hito2grupo3.entities.Staff;
import com.example.hito2grupo3.entities.UnidadVenta;
import com.example.hito2grupo3.repositories.FestivalRepository;
import com.example.hito2grupo3.repositories.PedidoRepository;
import com.example.hito2grupo3.repositories.PlatoRepository;
import com.example.hito2grupo3.repositories.StaffRepository;
import com.example.hito2grupo3.repositories.UnidadVentaRepository;
import com.example.hito2grupo3.services.IPedidoService;
import com.example.hito2grupo3.dto.PedidoResponseDTO;
import java.util.NoSuchElementException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PedidoServiceImpl implements IPedidoService {

        private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Argentina/Buenos_Aires");

        private final PedidoRepository pedidoRepository;
        private final FestivalRepository festivalRepository;
        private final UnidadVentaRepository unidadVentaRepository;
        private final PlatoRepository platoRepository;
        private final StaffRepository staffRepository;

        public PedidoServiceImpl(
                        PedidoRepository pedidoRepository,
                        FestivalRepository festivalRepository,
                        UnidadVentaRepository unidadVentaRepository,
                        PlatoRepository platoRepository,
                        StaffRepository staffRepository) {

                this.pedidoRepository = pedidoRepository;
                this.festivalRepository = festivalRepository;
                this.unidadVentaRepository = unidadVentaRepository;
                this.platoRepository = platoRepository;
                this.staffRepository = staffRepository;
        }

        // ==========================================
        // OBTENER PLATOS DE UNA UNIDAD DE VENTA
        // ==========================================

        @Override
        @Transactional(readOnly = true)
        public List<Plato> obtenerPlatosPorUnidad(Long unidadVentaId) {

                UnidadVenta unidad = unidadVentaRepository
                                .buscarPorIdConPlatos(unidadVentaId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No existe la unidad de venta seleccionada."));

                return unidad.getPlatosOfrecidos().stream()
                                .filter(Plato::isActivo)
                                .toList();
        }

        // ==========================================
        // REGISTRAR PEDIDO
        // ==========================================

        @Override
        @Transactional
        public Pedido registrarPedido(PedidoAltaDTO datos, Long staffId) {
Staff staff = obtenerStaffHabilitado(staffId);
                if (datos == null
                                || datos.getFecha() == null
                                || datos.getFestivalId() == null
                                || datos.getUnidadVentaId() == null) {

                        throw new IllegalArgumentException(
                                        "Complete la fecha, el festival y la unidad de venta.");
                }

                if (datos.getItems() == null || datos.getItems().isEmpty()) {
                        throw new IllegalArgumentException(
                                        "El pedido debe contener al menos un plato.");
                }
                boolean responsable = esResponsable(staff);
                boolean cajero = esCajero(staff);

                if (!responsable && !cajero) {
                        throw new IllegalArgumentException(
                                        "No tenés autorización para registrar pedidos.");
                }

                LocalDate hoy = LocalDate.now(ZONA_HORARIA);

                if (!datos.getFecha().equals(hoy)) {
                        throw new IllegalArgumentException(
                                        "La fecha de la venta debe ser la fecha actual.");
                }

                Festival festival = festivalRepository
                                .findById(datos.getFestivalId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "El festival seleccionado no existe."));

                UnidadVenta unidad = unidadVentaRepository
                                .buscarPorIdConPlatos(datos.getUnidadVentaId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "La unidad de venta seleccionada no existe."));

                // Verificar que el festival esté vigente.
                if (festival.getFechaInicio() == null
                                || festival.getFechaFin() == null
                                || hoy.isBefore(festival.getFechaInicio())
                                || hoy.isAfter(festival.getFechaFin())) {

                        throw new IllegalArgumentException(
                                        "El festival no está activo en la fecha actual.");
                }

                // Verificar que la unidad esté habilitada para el festival.
                boolean unidadHabilitada = festival.getUnidadesVentaHabilitadas() != null
                                && festival.getUnidadesVentaHabilitadas().stream()
                                                .anyMatch(u -> Objects.equals(u.getId(), unidad.getId()));

                if (!unidadHabilitada) {
                        throw new IllegalArgumentException(
                                        "La unidad de venta no está habilitada para ese festival.");
                }

                // Verificar asignación del staff a la unidad.
                boolean unidadAsignada = unidad.getStaffAsignado() != null
                                && unidad.getStaffAsignado().stream()
                                                .anyMatch(asignado -> Objects.equals(
                                                                asignado.getId(),
                                                                staff.getId()));

                // Verificar si el staff es responsable de la unidad.
                boolean esResponsableDeUnidad = unidad.getResponsable() != null
                                && Objects.equals(
                                                unidad.getResponsable().getId(),
                                                staff.getId());

                if (responsable) {
                        if (!unidadAsignada && !esResponsableDeUnidad) {
                                throw new IllegalArgumentException(
                                                "No tenés autorización para registrar pedidos en esta unidad.");
                        }
                } else if (cajero && !unidadAsignada) {
                        throw new IllegalArgumentException(
                                        "No tenés asignada esta unidad de venta.");
                }

                Pedido pedido = new Pedido();

                pedido.setFestival(festival);
                pedido.setUnidadVenta(unidad);
                pedido.setFechaTransaccion(datos.getFecha());

                // Datos de registro y auditoría.
                pedido.setEstado("A");
                pedido.setFechaRegistro(LocalDateTime.now(ZONA_HORARIA));
                pedido.setRegistradoPor(staff);

                Map<Long, Integer> cantidadPorPlato = new LinkedHashMap<>();

                for (ItemPedidoDTO itemDTO : datos.getItems()) {

                        if (itemDTO == null
                                        || itemDTO.getPlatoId() == null
                                        || itemDTO.getCantidad() == null
                                        || itemDTO.getCantidad() < 1) {
                                throw new IllegalArgumentException(
                                                "Cada plato debe tener un identificador y una cantidad válida.");
                        }

                        int total = cantidadPorPlato.merge(
                                        itemDTO.getPlatoId(), itemDTO.getCantidad(), Integer::sum);

                        if (total > 100) {
                                throw new IllegalArgumentException(
                                                "No podés superar las 100 unidades del mismo plato.");
                        }
                }

                for (Map.Entry<Long, Integer> entrada : cantidadPorPlato.entrySet()) {

                        Long platoId = entrada.getKey();
                        Integer cantidad = entrada.getValue();

                        Plato plato = platoRepository
                                        .findById(platoId)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Uno de los platos seleccionados no existe."));

                        if (!plato.isActivo()) {
                                throw new IllegalArgumentException(
                                                "El plato " + plato.getNombre()
                                                                + " está inactivo y no puede incluirse en un pedido.");
                        }

                        boolean platoHabilitado = unidad.getPlatosOfrecidos()
                                        .stream()
                                        .anyMatch(p -> Objects.equals(p.getId(), plato.getId()));

                        if (!platoHabilitado) {
                                throw new IllegalArgumentException(
                                                "El plato " + plato.getNombre()
                                                                + " no está asociado a la unidad de venta.");
                        }

                        ItemPedido item = new ItemPedido();
                        item.setPedido(pedido);
                        item.setPlato(plato);
                        item.setCantidad(cantidad);
                        item.setPrecioUnitario(plato.getPrecio());

                        pedido.agregarItem(item);
                }

                // Guardar el pedido con todos sus ítems.
                return pedidoRepository.save(pedido);
        }

        // ==========================================
        // HISTORIAL DE PEDIDOS
        // ==========================================

        @Override
        @Transactional(readOnly = true)
        public List<Pedido> obtenerHistorialPorStaff(Long staffId) {

                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró el empleado autenticado."));

                boolean responsable = esResponsable(staff);
                boolean cajero = esCajero(staff);

                if (!responsable && !cajero) {
                        throw new IllegalArgumentException(
                                        "No tenés autorización para consultar pedidos.");
                }

                List<UnidadVenta> unidades;

                if (responsable) {
                        // El responsable ve unidades asignadas o bajo su responsabilidad.
                        unidades = unidadVentaRepository
                                        .buscarPorStaffAsignadoOResponsable(staff.getId());
                } else {
                        // El cajero ve solamente sus unidades asignadas.
                        unidades = unidadVentaRepository
                                        .buscarPorStaffAsignado(staff.getId());
                }

                List<Pedido> historial = new ArrayList<>();

                LocalDate hoy = LocalDate.now(ZONA_HORARIA);

                for (UnidadVenta unidad : unidades) {

                        List<Pedido> pedidosUnidad = pedidoRepository.obtenerHistorialPorUnidad(unidad.getId());

                        if (responsable) {
                                // El responsable puede consultar todos los festivales.
                                historial.addAll(pedidosUnidad);

                        } else {
                                // El cajero solamente ve pedidos de festivales vigentes.
                                pedidosUnidad.stream()
                                                .filter(pedido -> pedido.getFestival() != null
                                                                && pedido.getFestival().getFechaInicio() != null
                                                                && pedido.getFestival().getFechaFin() != null
                                                                && !hoy.isBefore(
                                                                                pedido.getFestival().getFechaInicio())
                                                                && !hoy.isAfter(
                                                                                pedido.getFestival().getFechaFin()))
                                                .forEach(historial::add);
                        }
                }

                // Ordenar desde el más reciente al más antiguo.
                historial.sort(
                                Comparator.comparing(
                                                Pedido::getFechaRegistro,
                                                Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(
                                                                Pedido::getId,
                                                                Comparator.reverseOrder()));

                return historial;
        }

        // ==========================================
        // ANULACIÓN INTERNA DE UN PEDIDO
        // ==========================================

        @Override
        @Transactional
        public void anularPedido(Long pedidoId, Long staffId, String motivo) {

                if (motivo == null || motivo.trim().isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Debe ingresar el motivo de la anulación.");
                }

                if (motivo.trim().length() > 500) {
                        throw new IllegalArgumentException(
                                        "El motivo no puede superar los 500 caracteres.");
                }

                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró el empleado autenticado."));

                boolean responsable = esResponsable(staff);
                boolean cajero = esCajero(staff);

                if (!responsable && !cajero) {
                        throw new IllegalArgumentException(
                                        "No tenés autorización para anular pedidos.");
                }

                Pedido pedido = pedidoRepository.findById(pedidoId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró el pedido seleccionado."));

                UnidadVenta unidad = pedido.getUnidadVenta();
                Festival festival = pedido.getFestival();

                // Verificar que exista la unidad.
                if (unidad == null) {
                        throw new IllegalArgumentException(
                                        "No se encontró la unidad de venta del pedido.");
                }

                // Verificar que el festival esté vigente.
                LocalDate hoy = LocalDate.now(ZONA_HORARIA);

                if (festival == null
                                || festival.getFechaInicio() == null
                                || festival.getFechaFin() == null
                                || hoy.isBefore(festival.getFechaInicio())
                                || hoy.isAfter(festival.getFechaFin())) {

                        throw new IllegalArgumentException(
                                        "No se pueden anular pedidos de un festival que no está vigente.");
                }

                // Verificar que la unidad esté habilitada para ese festival.
                boolean unidadHabilitada = festival.getUnidadesVentaHabilitadas() != null
                                && festival.getUnidadesVentaHabilitadas().stream()
                                                .anyMatch(u -> Objects.equals(u.getId(), unidad.getId()));

                if (!unidadHabilitada) {
                        throw new IllegalArgumentException(
                                        "La unidad de venta no está habilitada para este festival.");
                }

                // Verificar si el staff está asignado a la unidad.
                boolean unidadAsignada = unidad.getStaffAsignado() != null
                                && unidad.getStaffAsignado().stream()
                                                .anyMatch(asignado -> Objects.equals(
                                                                asignado.getId(),
                                                                staff.getId()));

                // Verificar si el staff es responsable de la unidad.
                boolean esResponsableDeUnidad = unidad.getResponsable() != null
                                && Objects.equals(
                                                unidad.getResponsable().getId(),
                                                staff.getId());

                if (responsable) {
                        if (!unidadAsignada && !esResponsableDeUnidad) {
                                throw new IllegalArgumentException(
                                                "No tenés autorización para anular pedidos de esta unidad.");
                        }
                } else if (cajero && !unidadAsignada) {
                        throw new IllegalArgumentException(
                                        "No tenés autorización para anular pedidos de esta unidad.");
                }

                // Solo se pueden anular pedidos activos.
                if (!"A".equals(pedido.getEstado())) {
                        throw new IllegalArgumentException(
                                        "El pedido ya está anulado y no se puede volver a anular.");
                }

                // Registrar la anulación y sus datos de auditoría.
                pedido.setEstado("R");
                pedido.setFechaAnulacion(LocalDateTime.now(ZONA_HORARIA));
                pedido.setMotivoAnulacion(motivo.trim());
                pedido.setAnuladoPor(staff);

                pedidoRepository.save(pedido);
        }

        // ==========================================
        // OBTENER PLATO ESTRELLA
        // ==========================================

        @Override
        @Transactional(readOnly = true)
        public PlatoDTO obtenerPlatoEstrella(
                        Long unidadVentaId,
                        Long festivalId) {

                List<PlatoDTO> platos = pedidoRepository.obtenerPlatosEstrella(
                                unidadVentaId,
                                festivalId);

                if (platos.isEmpty()) {
                        return null;
                }

                return platos.get(0);
        }

        // ==========================================
        // OBTENER DETALLE DE PEDIDO
        // ==========================================

        @Override
        @Transactional(readOnly = true)
        public Pedido obtenerPedidoPorStaff(Long pedidoId, Long staffId) {

                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró el empleado autenticado."));

                boolean responsable = esResponsable(staff);
                boolean cajero = esCajero(staff);

                if (!responsable && !cajero) {
                        throw new IllegalArgumentException(
                                        "No tenés autorización para consultar pedidos.");
                }

                Pedido pedido = pedidoRepository.obtenerDetallePorId(pedidoId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "No se encontró el pedido seleccionado."));

                UnidadVenta unidad = pedido.getUnidadVenta();

                if (unidad == null) {
                        throw new IllegalArgumentException(
                                        "No se encontró la unidad de venta del pedido.");
                }

                // Verificar si el staff está asignado a la unidad.
                boolean unidadAsignada = unidad.getStaffAsignado() != null
                                && unidad.getStaffAsignado().stream()
                                                .anyMatch(asignado -> Objects.equals(
                                                                asignado.getId(),
                                                                staff.getId()));

                // Verificar si el staff es responsable de la unidad.
                boolean esResponsableDeUnidad = unidad.getResponsable() != null
                                && Objects.equals(
                                                unidad.getResponsable().getId(),
                                                staff.getId());

                if (responsable) {
                        if (!unidadAsignada && !esResponsableDeUnidad) {
                                throw new IllegalArgumentException(
                                                "No tenés autorización para consultar este pedido.");
                        }

                } else if (cajero) {

                        if (!unidadAsignada) {
                                throw new IllegalArgumentException(
                                                "No tenés autorización para consultar este pedido.");
                        }

                        // El cajero solo puede consultar pedidos de festivales vigentes.
                        Festival festival = pedido.getFestival();
                        LocalDate hoy = LocalDate.now(ZONA_HORARIA);

                        if (festival == null
                                        || festival.getFechaInicio() == null
                                        || festival.getFechaFin() == null
                                        || hoy.isBefore(festival.getFechaInicio())
                                        || hoy.isAfter(festival.getFechaFin())) {

                                throw new IllegalArgumentException(
                                                "No tenés autorización para consultar pedidos de festivales no vigentes.");
                        }
                }

                return pedido;
        }

        // ==========================================
        // MÉTODOS AUXILIARES DE ROLES
        // ==========================================
        private Staff obtenerStaffHabilitado(Long staffId) {

                if (staffId == null) {
                        throw new IllegalArgumentException("Debe indicar el empleado (staffId).");
                }

                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new NoSuchElementException(
                                                "No existe el empleado con id " + staffId + "."));

                boolean usuarioActivo = staff.getUsuario() != null
                                && staff.getUsuario().isActivo();

                if (!staff.isActivo() || !usuarioActivo) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "El usuario no está habilitado.");
                }

                return staff;
        }

        private String obtenerRol(Staff staff) {

                if (staff.getUsuario() == null
                                || staff.getUsuario().getRol() == null) {

                        throw new IllegalArgumentException(
                                        "El usuario no tiene un rol asignado.");
                }

                return staff.getUsuario().getRol().getNombre()
                                .trim().toUpperCase();
        }

        private boolean esResponsable(Staff staff) {
                return "RESPONSABLE".equals(obtenerRol(staff));
        }

        private boolean esCajero(Staff staff) {
                return "CAJERO".equals(obtenerRol(staff));
        }

        @Override
        @Transactional(readOnly = true)
        public Staff obtenerStaffPorEmail(String email) {
                if (email == null) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debés iniciar sesión.");
                }
                Staff staff = staffRepository.findByUsuario_Email(email)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "El usuario autenticado no tiene un empleado asociado."));
                verificarRolPedidos(staff);
                return staff;
        }

        private void verificarRolPedidos(Staff staff) {
                String rol = obtenerRol(staff);
                if (!"RESPONSABLE".equals(rol) && !"CAJERO".equals(rol)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No tenés autorización para gestionar pedidos.");
                }
        }

        private boolean estaVigente(Festival festival, LocalDate fecha) {
                return festival != null
                                && festival.getFechaInicio() != null
                                && festival.getFechaFin() != null
                                && !fecha.isBefore(festival.getFechaInicio())
                                && !fecha.isAfter(festival.getFechaFin());
        }

        private boolean tieneAccesoUnidad(UnidadVenta unidad, Staff staff) {
                String rol = obtenerRol(staff);
                boolean asignado = unidad.getStaffAsignado() != null
                                && unidad.getStaffAsignado().stream()
                                                .anyMatch(s -> Objects.equals(s.getId(), staff.getId()));

                if ("RESPONSABLE".equals(rol)) {
                        boolean esResponsableDeUnidad = unidad.getResponsable() != null
                                        && Objects.equals(unidad.getResponsable().getId(), staff.getId());
                        return asignado || esResponsableDeUnidad;
                }

                return "CAJERO".equals(rol) && asignado;
        }

        @Override
        @Transactional(readOnly = true)
        public void verificarAccesoUnidad(Long unidadId, Long staffId) {
                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Staff no encontrado"));
                String rol = obtenerRol(staff);

                List<UnidadVenta> unidades;
                if ("RESPONSABLE".equals(rol)) {
                        unidades = unidadVentaRepository.buscarPorStaffAsignadoOResponsable(staff.getId());
                } else if ("CAJERO".equals(rol)) {
                        unidades = unidadVentaRepository.buscarPorStaffAsignado(staff.getId());
                } else {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No tenés autorización para acceder a pedidos.");
                }

                boolean tieneAcceso = unidades.stream()
                                .anyMatch(unidad -> Objects.equals(unidad.getId(), unidadId));

                if (!tieneAcceso) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No tenés acceso a esta unidad de venta.");
                }
        }

        @Override
        @Transactional(readOnly = true)
        public List<Festival> obtenerFestivalesDisponiblesParaAlta(Long staffId) {
                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Staff no encontrado"));
                LocalDate fechaActual = LocalDate.now(ZONA_HORARIA);

                return festivalRepository.findAllByOrderByFechaInicioAsc()
                                .stream()
                                .filter(festival -> estaVigente(festival, fechaActual))
                                .filter(festival -> festivalRepository.obtenerUnidadesPorFestival(festival.getId())
                                                .stream()
                                                .anyMatch(unidad -> tieneAccesoUnidad(unidad, staff)))
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<UnidadVenta> obtenerUnidadesDisponiblesPorFestival(Long festivalId, Long staffId) {
                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Staff no encontrado"));
                verificarRolPedidos(staff);

                LocalDate fechaActual = LocalDate.now(ZONA_HORARIA);
                Festival festival = festivalRepository.findById(festivalId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "No existe el festival seleccionado."));

                if (!estaVigente(festival, fechaActual)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "El festival no está vigente.");
                }

                return festivalRepository.obtenerUnidadesPorFestival(festivalId)
                                .stream()
                                .filter(unidad -> tieneAccesoUnidad(unidad, staff))
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public PlatoDTO obtenerPlatoEstrellaValidado(Long unidadVentaId, Long festivalId, Long staffId) {
                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Staff no encontrado"));
                verificarRolPedidos(staff);

                LocalDate fechaActual = LocalDate.now(ZONA_HORARIA);
                Festival festival = festivalRepository.findById(festivalId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "No existe el festival seleccionado."));

                if (!estaVigente(festival, fechaActual)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "El festival no está vigente.");
                }

                boolean unidadDelFestival = festivalRepository.obtenerUnidadesPorFestival(festivalId)
                                .stream()
                                .anyMatch(unidad -> Objects.equals(unidad.getId(), unidadVentaId));

                if (!unidadDelFestival) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "La unidad de venta no pertenece a este festival.");
                }

                verificarAccesoUnidad(unidadVentaId, staffId);
                return obtenerPlatoEstrella(unidadVentaId, festivalId);
        }

        @Override
        @Transactional(readOnly = true)
        public Map<String, Object> obtenerDatosHistorial(Long staffId) {
                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Staff no encontrado"));
                String rol = obtenerRol(staff);

                List<UnidadVenta> unidadesVenta;
                if ("RESPONSABLE".equals(rol)) {
                        unidadesVenta = unidadVentaRepository.buscarPorStaffAsignadoOResponsable(staff.getId());
                } else if ("CAJERO".equals(rol)) {
                        unidadesVenta = unidadVentaRepository.buscarPorStaffAsignado(staff.getId());
                } else {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No tenés autorización para consultar pedidos.");
                }

                List<Pedido> pedidos = obtenerHistorialPorStaff(staff.getId());
                Map<Long, Staff> empleadosPorId = new LinkedHashMap<>();
                for (Pedido pedido : pedidos) {
                        Staff registrador = pedido.getRegistradoPor();
                        if (registrador != null) {
                                empleadosPorId.putIfAbsent(registrador.getId(), registrador);
                        }
                }

                List<Staff> empleados = new ArrayList<>(empleadosPorId.values());
                empleados.sort(Comparator
                                .comparing(Staff::getApellido, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                                .thenComparing(Staff::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

                Map<String, Object> modelMap = new HashMap<>();
                modelMap.put("pedidos", pedidos);
                modelMap.put("unidadesVenta", unidadesVenta);
                modelMap.put("empleados", empleados);
                modelMap.put("rol", rol);
                return modelMap;
        }

        @Override
        @Transactional(readOnly = true)
        public List<Festival> obtenerFestivalesPorUnidadYStaff(Long unidadId, Long staffId) {
                Staff staff = staffRepository.findById(staffId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Staff no encontrado"));
                String rol = obtenerRol(staff);

                if (!"RESPONSABLE".equals(rol) && !"CAJERO".equals(rol)) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "No tenés autorización para consultar festivales.");
                }

                verificarAccesoUnidad(unidadId, staffId);
                LocalDate hoy = LocalDate.now(ZONA_HORARIA);

                return festivalRepository.buscarTodosPorUnidadVenta(unidadId)
                                .stream()
                                .filter(festival -> "RESPONSABLE".equals(rol) || estaVigente(festival, hoy))
                                .toList();
        }

        // ==========================================
        // HISTORIAL FILTRADO (API REST - Hito 3)
        // ==========================================

        @Override
        @Transactional(readOnly = true)
        public List<PedidoResponseDTO> buscarHistorialFiltrado(
                        Long festivalId,
                        Long unidadVentaId,
                        Long platoId,
                        LocalDate fechaDesde,
                        LocalDate fechaHasta,
                        String estado) {

                if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
                        throw new IllegalArgumentException(
                                        "La fecha desde no puede ser posterior a la fecha hasta.");
                }

                String estadoNormalizado = null;
                if (estado != null && !estado.isBlank()) {
                        estadoNormalizado = estado.trim().toUpperCase();
                        if (!estadoNormalizado.equals("A") && !estadoNormalizado.equals("R")) {
                                throw new IllegalArgumentException(
                                                "El estado debe ser A (activo) o R (anulado).");
                        }
                }

                // Si se filtra por un id inexistente se informa 404 en lugar de una lista
                // vacía.
                if (festivalId != null && !festivalRepository.existsById(festivalId)) {
                        throw new NoSuchElementException("No existe el festival con id " + festivalId + ".");
                }
                if (unidadVentaId != null && !unidadVentaRepository.existsById(unidadVentaId)) {
                        throw new NoSuchElementException("No existe la unidad de venta con id " + unidadVentaId + ".");
                }
                if (platoId != null && !platoRepository.existsById(platoId)) {
                        throw new NoSuchElementException("No existe el plato con id " + platoId + ".");
                }

                return pedidoRepository.buscarHistorialFiltrado(
                                festivalId, unidadVentaId, platoId,
                                fechaDesde, fechaHasta, estadoNormalizado)
                                .stream()
                                .map(PedidoResponseDTO::desde)
                                .toList();
        }

        // ==========================================
        // HISTORIAL DEL EMPLEADO Y UNIDAD (API REST)
        // ==========================================

        @Override
        @Transactional(readOnly = true)
        public List<PedidoResponseDTO> buscarHistorialDelStaff(
                        Long staffId,
                        Long pedidoId,
                        Long unidadVentaId,
                        Long festivalId,
                        Long empleadoId,
                        String estado,
                        LocalDate fechaDesde,
                        LocalDate fechaHasta) {


                Staff staff = obtenerStaffHabilitado(staffId);
                verificarRolPedidos(staff);

                List<UnidadVenta> unidades = esResponsable(staff)
                                ? unidadVentaRepository.buscarPorStaffAsignadoOResponsable(staff.getId())
                                : unidadVentaRepository.buscarPorStaffAsignado(staff.getId());

                if (unidades.isEmpty()) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "El empleado no está asociado a ninguna unidad de venta.");
                }

                if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
                        throw new IllegalArgumentException(
                                        "La fecha desde no puede ser posterior a la fecha hasta.");
                }

                String estadoNormalizado = null;
                if (estado != null && !estado.isBlank()) {
                        estadoNormalizado = estado.trim().toUpperCase();
                        if (!estadoNormalizado.equals("A") && !estadoNormalizado.equals("R")) {
                                throw new IllegalArgumentException(
                                                "El estado debe ser A (activo) o R (anulado).");
                        }
                }

                if (unidadVentaId != null) {
                        if (!unidadVentaRepository.existsById(unidadVentaId)) {
                                throw new NoSuchElementException(
                                                "No existe la unidad de venta con id " + unidadVentaId + ".");
                        }
                        boolean asociado = unidades.stream()
                                        .anyMatch(u -> Objects.equals(u.getId(), unidadVentaId));
                        if (!asociado) {
                                throw new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "No tenés acceso a esta unidad de venta.");
                        }
                }
                if (festivalId != null && !festivalRepository.existsById(festivalId)) {
                        throw new NoSuchElementException("No existe el festival con id " + festivalId + ".");
                }
                if (empleadoId != null && !staffRepository.existsById(empleadoId)) {
                        throw new NoSuchElementException("No existe el empleado con id " + empleadoId + ".");
                }

                // Misma logica que por la web, unidades del cajero y solo festivales vigentes
                List<Pedido> historial = obtenerHistorialPorStaff(staff.getId());

                // Si se pide un pedido debe existir (404) y ser visible para el empleado (403)
                if (pedidoId != null) {
                        Pedido pedido = pedidoRepository.findById(pedidoId)
                                        .orElseThrow(() -> new NoSuchElementException(
                                                        "No existe el pedido N.º " + pedidoId + "."));

                        boolean visible = historial.stream()
                                        .anyMatch(p -> Objects.equals(p.getId(), pedidoId));

                        if (!visible) {
                                boolean esDeSuUnidad = pedido.getUnidadVenta() != null
                                                && unidades.stream().anyMatch(u -> Objects.equals(
                                                                u.getId(), pedido.getUnidadVenta().getId()));

                                throw new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                esDeSuUnidad
                                                                ? "El pedido N.º " + pedidoId
                                                                                + " pertenece a un festival que no está vigente."
                                                                : "El pedido N.º " + pedidoId
                                                                                + " pertenece a otra unidad de venta.");
                        }
                }

                final String estadoFiltro = estadoNormalizado;

                return historial
                                .stream()
                                .filter(p -> pedidoId == null
                                                || Objects.equals(p.getId(), pedidoId))
                                .filter(p -> unidadVentaId == null
                                                || (p.getUnidadVenta() != null
                                                                && Objects.equals(p.getUnidadVenta().getId(), unidadVentaId)))
                                .filter(p -> festivalId == null
                                                || (p.getFestival() != null
                                                                && Objects.equals(p.getFestival().getId(), festivalId)))
                                .filter(p -> empleadoId == null
                                                || (p.getRegistradoPor() != null
                                                                && Objects.equals(p.getRegistradoPor().getId(), empleadoId)))
                                .filter(p -> estadoFiltro == null
                                                || estadoFiltro.equalsIgnoreCase(p.getEstado()))
                                .filter(p -> fechaDesde == null
                                                || (p.getFechaRegistro() != null
                                                                && !p.getFechaRegistro().toLocalDate().isBefore(fechaDesde)))
                                .filter(p -> fechaHasta == null
                                                || (p.getFechaRegistro() != null
                                                                && !p.getFechaRegistro().toLocalDate().isAfter(fechaHasta)))
                                .map(PedidoResponseDTO::desde)
                                .toList();
        }
}