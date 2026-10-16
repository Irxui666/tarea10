package com.example.Actividad01.service.impl;

import com.example.Actividad01.dto.DetalleVentaRequestDTO;
import com.example.Actividad01.dto.DetalleVentaResponseDTO;
import com.example.Actividad01.dto.VentaRequestDTO;
import com.example.Actividad01.dto.VentaResponseDTO;
import com.example.Actividad01.entity.Cliente;
import com.example.Actividad01.entity.DetalleVenta;
import com.example.Actividad01.entity.Producto;
import com.example.Actividad01.entity.Venta;
import com.example.Actividad01.enums.EstadoVenta;
import com.example.Actividad01.exception.RecursosNoEncontradoException;
import com.example.Actividad01.exception.ReglaNegocioException;
import com.example.Actividad01.repository.ClienteRepository;
import com.example.Actividad01.repository.ProductoRepository;
import com.example.Actividad01.repository.VentaRepository;
import com.example.Actividad01.service.service.VentaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    private static final Set<String> CAMPOS_ORDENABLES = Set.of("id", "fecha", "total", "estado");
    private static final String ORDEN_POR_DEFECTO = "fecha";

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository) {

        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public VentaResponseDTO registrar(VentaRequestDTO request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RecursosNoEncontradoException("Cliente no encontrado con id: " + request.getClienteId()));

        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new ReglaNegocioException("No se puede registrar una venta para un cliente inactivo");
        }
        Venta venta = new Venta();

        venta.setCliente(cliente);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.REGISTRADA);

        BigDecimal total = BigDecimal.ZERO;

        if (request.getDetalles().stream().map(DetalleVentaRequestDTO::getProductoId).distinct().count() != request.getDetalles().size()) {
            throw new ReglaNegocioException("Un producto no puede repetirse en el detalle");
        }
        for (DetalleVentaRequestDTO item : request.getDetalles().stream()
                .sorted(java.util.Comparator.comparing(DetalleVentaRequestDTO::getProductoId)).toList()) {
            Producto producto = productoRepository.findById(item.getProductoId()).orElseThrow(() ->
                    new RecursosNoEncontradoException("Producto no encontrado con id: " + item.getProductoId()));

            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new ReglaNegocioException("El producto " + producto.getNombre() + " se encuentra inactivo");
            }

            if (productoRepository.descontarStock(producto.getId(), item.getCantidad()) != 1) {
                throw new ReglaNegocioException("Stock insuficiente o producto inactivo: " + producto.getNombre()
                        + ". Actualice los productos e intente nuevamente.");
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));

            DetalleVenta detalle = new DetalleVenta();

            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalle.setSubtotal(subtotal);

            venta.agregarDetalle(detalle);

            total = total.add(subtotal);

        }

        venta.setTotal(total);

        Venta guardada = ventaRepository.save(venta);

        return convertirResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO buscar(Long id) {
        Venta venta = ventaRepository.findById(id).orElseThrow(() ->
                new RecursosNoEncontradoException("Venta no encontrada con id: " + id));
        return convertirResponse(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listar() {
        return ventaRepository.findAll().stream().map(this::convertirResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> buscarVentas(
            Long clienteId,
            EstadoVenta estado,
            LocalDate desde,
            LocalDate hasta,
            String ordenarPor,
            String direccion) {

        long inicio = System.currentTimeMillis();

        log.info("Inicio buscar ventas | clienteId={} | estado={} | "
                        + "desde={} | hasta={} | ordenarPor={} | direccion={}",
                clienteId, estado, desde, hasta, ordenarPor, direccion);

        if (desde != null
                && hasta != null
                && desde.isAfter(hasta)) {

            throw new ReglaNegocioException(
                    "El rango de fechas es inválido: 'desde' ("
                            + desde
                            + ") es posterior a 'hasta' ("
                            + hasta + ")");
        }

        Sort sort = construirSort(ordenarPor, direccion);

        LocalDateTime desdeHora = (desde == null)
                ? null
                : desde.atStartOfDay();

        LocalDateTime hastaHora = (hasta == null)
                ? null
                : hasta.atTime(LocalTime.MAX);

        List<VentaResponseDTO> resultado =
                ventaRepository
                        .buscar(clienteId, estado, desdeHora, hastaHora, sort)
                        .stream()
                        .map(this::convertirResponse)
                        .toList();

        log.info("Fin buscar ventas | clienteId={} | estado={} | "
                        + "desde={} | hasta={} | orden={} {} | "
                        + "filas={} | duracionMs={}",
                clienteId, estado, desde, hasta, ordenarPor, direccion,
                resultado.size(),
                System.currentTimeMillis() - inicio);

        return resultado;
    }

    @Override
    @Transactional
    public VentaResponseDTO anular(Long id) {
        Venta venta = ventaRepository.bloquearPorId(id)
                .orElseThrow(() -> new RecursosNoEncontradoException(
                        "Venta no encontrada con el id: " + id));
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new ReglaNegocioException(
                    "La venta ya se encuentra anulada"
            );
        }
        venta.setEstado(EstadoVenta.ANULADA);
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            productoRepository.devolverStock(producto.getId(), detalle.getCantidad());
        }
        Venta ventaAnulada = ventaRepository.save(venta);
        return convertirResponse(ventaAnulada);
    }

    @Override
    @Transactional(readOnly = true)
    public com.example.Actividad01.dto.PaginaResponseDTO<VentaResponseDTO> buscarPagina(
            Long clienteId, EstadoVenta estado, LocalDate desde, LocalDate hasta,
            String ordenarPor, String direccion, int pagina, int tamanio) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha desde no puede ser posterior a hasta");
        }
        if (pagina < 0 || tamanio < 1 || tamanio > 100) {
            throw new ReglaNegocioException("Página o tamaño inválido; el tamaño debe estar entre 1 y 100");
        }
        var pageable = org.springframework.data.domain.PageRequest.of(pagina, tamanio,
                construirSort(ordenarPor, direccion).and(Sort.by("id").descending()));
        var resultado = ventaRepository.buscarPagina(clienteId, estado,
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.atTime(LocalTime.MAX), pageable);
        return com.example.Actividad01.dto.PaginaResponseDTO.from(resultado.map(this::convertirResponse));
    }

    private Sort construirSort(String ordenarPor, String direccion) {

        String campo = (ordenarPor == null || ordenarPor.isBlank())
                ? ORDEN_POR_DEFECTO
                : ordenarPor.trim();

        if (!CAMPOS_ORDENABLES.contains(campo)) {

            throw new ReglaNegocioException(
                    "El campo de ordenamiento '"
                            + campo
                            + "' no está permitido. Campos válidos: "
                            + CAMPOS_ORDENABLES);
        }

        String sentido = (direccion == null || direccion.isBlank())
                ? "desc"
                : direccion.trim();

        if (!sentido.equalsIgnoreCase("asc")
                && !sentido.equalsIgnoreCase("desc")) {

            throw new ReglaNegocioException(
                    "La dirección de ordenamiento '"
                            + sentido
                            + "' no está permitida. Valores válidos: asc, desc");
        }

        return sentido.equalsIgnoreCase("asc")
                ? Sort.by(campo).ascending()
                : Sort.by(campo).descending();
    }

    private VentaResponseDTO convertirResponse(Venta venta) {

        List<DetalleVentaResponseDTO> detalles =
                venta.getDetalles()
                        .stream()
                        .map(detalle ->
                                new DetalleVentaResponseDTO(
                                        detalle.getProducto().getId(),
                                        detalle.getProducto().getNombre(),
                                        detalle.getCantidad(),
                                        detalle.getPrecio(),
                                        detalle.getSubtotal()
                                )
                        ).toList();

        String clienteNombre = venta.getCliente().getNombres() + " " + venta.getCliente().getApellidos();

        return new VentaResponseDTO(
                venta.getId(),
                venta.getFecha(),
                venta.getCliente().getId(),
                clienteNombre,
                venta.getEstado().name(),
                venta.getTotal(),
                detalles
        );
    }
}
