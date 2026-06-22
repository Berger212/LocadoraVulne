package com.example.locadora.controller;

import com.example.locadora.dto.JogoRequest;
import com.example.locadora.dto.JogoResponse;
import com.example.locadora.service.JogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jogos")
public class JogoController {

    private final JogoService jogoService;

    public JogoController(JogoService jogoService) {
        this.jogoService = jogoService;
    }

    @GetMapping
    public ResponseEntity<List<JogoResponse>> listar() {
        return ResponseEntity.ok(jogoService.listar());
    }

    @PostMapping
    public ResponseEntity<JogoResponse> criar(@Valid @RequestBody JogoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jogoService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JogoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody JogoRequest request) {
        return ResponseEntity.ok(jogoService.atualizar(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JogoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(jogoService.buscar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        jogoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
