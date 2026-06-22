package com.example.locadora.controller;

import com.example.locadora.dto.DevolucaoRequest;
import com.example.locadora.dto.LocacaoRequest;
import com.example.locadora.dto.LocacaoResponse;
import com.example.locadora.service.LocacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locacoes")
public class LocacaoController {

    private final LocacaoService locacaoService;

    public LocacaoController(LocacaoService locacaoService) {
        this.locacaoService = locacaoService;
    }

    @PostMapping
    public ResponseEntity<LocacaoResponse> registrar(@Valid @RequestBody LocacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locacaoService.registrar(request));
    }

    @GetMapping
    public ResponseEntity<List<LocacaoResponse>> listar() {
        return ResponseEntity.ok(locacaoService.listar());
    }

    @PutMapping("/{id}/devolucao")
    public ResponseEntity<LocacaoResponse> devolver(@PathVariable Long id, @Valid @RequestBody DevolucaoRequest request) {
        return ResponseEntity.ok(locacaoService.registrarDevolucao(id, request));
    }
}
