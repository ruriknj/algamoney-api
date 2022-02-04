package com.example.algamoney.api.resource;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.PessoaRepository;
import com.example.algamoney.api.service.PessoaService;

@RestController
@RequestMapping("/pessoas")
public class PessoaResource {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private PessoaService pessoaService;

	@Autowired
	private ApplicationEventPublisher publisher; // dispara um evento no spring

	@GetMapping
	public ResponseEntity<?> listar() {
		var pessoas = pessoaRepository.findAll();
		return !pessoas.isEmpty() ? ResponseEntity.ok(pessoas) : ResponseEntity.noContent().build();

	}

	@PostMapping
	private ResponseEntity<?> criar(@Validated @RequestBody Pessoa pessoa, HttpServletResponse response) {
		var pessoasSalva = pessoaRepository.save(pessoa);

		publisher.publishEvent(new RecursoCriadoEvent(this, response, pessoasSalva.getCodigo()));

		return ResponseEntity.status(HttpStatus.CREATED).body(pessoasSalva);
	}

	@GetMapping("/{codigo}")
	public ResponseEntity<?> buascarPeloCodigo(@PathVariable Long codigo) {
		var pessoa = pessoaRepository.findById(codigo);
		return pessoa.isPresent() ? ResponseEntity.ok(pessoa) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

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

//	@GetMapping("/{codigo}")
//	public ResponseEntity<Categoria> buscarPeloId(@PathVariable Long codigo) {
//		return categoriaRepository.findById(codigo).map(categoria -> ResponseEntity.ok().body(categoria))
//				.orElseGet(() -> ResponseEntity.notFound().build());
//	}

	@DeleteMapping("/{codigo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void Remover(@PathVariable Long codigo) {
		pessoaRepository.deleteById(codigo);
	}

	// o método findById retorna um ResponseEntity para indicar sucesso ou não.

//	@PutMapping("/{codigo}")
//	public Pessoa atualizar(@PathVariable Long codigo,@Valid @RequestBody Pessoa pessoa) {
//
//		  Pessoa pessoaSalva = this.pessoaRepository.findById(codigo)
//		      .orElseThrow(() -> new EmptyResultDataAccessException(1));
//
//		  BeanUtils.copyProperties(pessoa, pessoaSalva, "codigo");
//
//		  return this.pessoaRepository.save(pessoaSalva);
//		}

	@PutMapping("/{codigo}")
	public ResponseEntity<Pessoa> atualiaDados(@PathVariable Long codigo, @Valid @RequestBody Pessoa pessoa) {

		var pessoaSalva = pessoaService.atualizar(codigo, pessoa);
		return ResponseEntity.ok(pessoaSalva);
	}
	
	@PutMapping("/{codigo}/ativo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void atualizarPropriedadeAtivo(@PathVariable Long codigo, @RequestBody Boolean ativo) {
		pessoaService.atualizarPropriedadeAtivo(codigo, ativo);
	}

}
