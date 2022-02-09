package com.example.algamoney.api.resource;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.model.Categoria;
import com.example.algamoney.api.repository.CategoriaRepository;

@RestController
@RequestMapping("/categorias")
public class CategoriaResource {

	@Autowired
	private CategoriaRepository categoriaRepository;
	
	@Autowired
	private ApplicationEventPublisher publisher; //dispara um evento no spring

	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_CATEGORIA') and hasAuthority('SCOPE_read')" )
	public ResponseEntity<?> listar() {
		var categorias = categoriaRepository.findAll();
		return !categorias.isEmpty() ? ResponseEntity.ok(categorias) : ResponseEntity.noContent().build();

	}

	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_CATEGORIA') and hasAuthority('SCOPE_write')")
	public ResponseEntity<Categoria> criar(@Valid @RequestBody Categoria categoria, HttpServletResponse response) {
		Categoria categoriaSalva = categoriaRepository.save(categoria);

		publisher.publishEvent(new RecursoCriadoEvent(publisher, response, categoriaSalva.getCodigo() ));

		return ResponseEntity.status(HttpStatus.CREATED).body(categoriaSalva);
	}
	
	@DeleteMapping("/{codigo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void Remover(@PathVariable Long codigo) {
		categoriaRepository.deleteById(codigo);
	}

//	@GetMapping("/{codigo}")
//	public ResponseEntity<?> buascarPeloCodigo(@PathVariable Long codigo) {
//				var categoria = categoriaRepository.findById(codigo);
//				return categoria.isPresent() ?
//				ResponseEntity.ok(categoria  ) :
//				ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//	}	

//	@GetMapping("/{codigo}")
//	public ResponseEntity<Categoria> buscarPeloCodigo(@PathVariable Long codigo) {
//		var categoria = this.categoriaRepository.findById(codigo);
//		return categoria.isPresent() ? 
//		        ResponseEntity.ok(categoria.get()) : ResponseEntity.notFound().build();		
//	}

//	@GetMapping("/{codigo}")
//	public ResponseEntity<Categoria> buscarPeloCodigo(@PathVariable Long codigo) {
//	return this.categoriaRepository.findById(codigo)
//	  .map(categoria -> ResponseEntity.ok(categoria))
//	  .orElse(ResponseEntity.notFound().build());
//	}

//	@GetMapping("/{codigo}")
//	public ResponseEntity<?> findByCodigoCategoria(@PathVariable Long codigo) {
//	var categoryRecuperada = categoriaRepository.findById(codigo);
//	return categoryRecuperada.isPresent() ?
//	ResponseEntity.status(HttpStatus.OK).body(categoryRecuperada.get()) :
//	ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//	}

	@GetMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_CATEGORIA') and hasAuthority('SCOPE_read')")
	public ResponseEntity<Categoria> buscarPeloId(@PathVariable Long codigo) {
		return categoriaRepository.findById(codigo).map(categoria -> ResponseEntity.ok().body(categoria))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
