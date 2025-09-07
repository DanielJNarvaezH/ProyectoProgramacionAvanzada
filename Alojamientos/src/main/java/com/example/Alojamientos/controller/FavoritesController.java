package com.example.Alojamientos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favoritos", description = "Marcar o remover alojamiento favorito")
public class FavoritesController {

    @PostMapping
    @Operation(summary = "Marcar alojamiento favorito", responses = {@ApiResponse(responseCode="201", description="Agregado")})
    public ResponseEntity<String> add(@RequestBody Map<String,Long> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Agregado a favoritos (mock)");
    }

    @DeleteMapping
    @Operation(summary = "Remover favorito", responses = {@ApiResponse(responseCode="204", description="Eliminado")})
    public ResponseEntity<Void> remove(@RequestParam Long accommodationId) {
        return ResponseEntity.noContent().build();
    }
}

