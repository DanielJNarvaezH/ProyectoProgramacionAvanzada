package com.example.Alojamientos.controller;

import com.example.Alojamientos.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/comentarios")
@Tag(name = "Comentarios", description = "Publicar y listar comentarios")
public class ComentarioController {

    @PostMapping
    @Operation(summary = "Publicar comentario (solo si reserva completada)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = ComentarioCreateDTO.class),
                    examples = @ExampleObject(value = "{\"reservationId\": 10, \"rating\": 5, \"text\": \"Excelente estancia\"}"))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Comentario publicado"),
                    @ApiResponse(responseCode = "400", description = "Validación")
            }
    )
    public ResponseEntity<String> post(@RequestBody ComentarioCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Comentario publicado (mock)");
    }

    @GetMapping
    @Operation(summary = "Listar comentarios de un alojamiento")
    public ResponseEntity<List<ComentarioCreateDTO>> list(@RequestParam(value="accommodationId", required = false) Long accommodationId,
                                                          @RequestParam(value="page", defaultValue="0") int page,
                                                          @RequestParam(value="size", defaultValue="10") int size) {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
