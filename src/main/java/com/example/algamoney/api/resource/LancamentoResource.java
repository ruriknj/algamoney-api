package com.example.algamoney.api.resource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.exceptionhandler.AlgamoneyExceptionHandler.Erro;
import com.example.algamoney.api.exceptionhandler.AlgamoneyExceptionHandler.InvalidRequestParameterNameException;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.repository.LancamentoRepository;
import com.example.algamoney.api.repository.filter.LancamentoFilter;
import com.example.algamoney.api.repository.projection.ResumoLancamento;
import com.example.algamoney.api.service.LancamentoService;
import com.example.algamoney.api.service.exception.PessoaInexistenteOuInativaException;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoResource {

	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private LancamentoService lancamentoService;

	@Autowired
	private ApplicationEventPublisher publisher; // dispara um evento no spring

	@Autowired
	private MessageSource messageSource;

	//usando Criteria-api
	@GetMapping
	public Page<Lancamento> pesquisar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		return lancamentoRepository.filtrar(lancamentoFilter, pageable);

	}
	
	@GetMapping(params = "resumo")
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		return lancamentoRepository.resumir(lancamentoFilter, pageable);
		
	}
	//usando query nativa
	 @GetMapping("/filtro")
	   // @PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and #oauth2.hasScope('read')")
	 public Page<Lancamento> pesquisar(@RequestParam String descricao, String dataInicio, String dataFim, Pageable pageable) 
	    {
	        descricao = "%"+descricao+"%";
	        return lancamentoRepository.customQueryPageableDate(descricao, dataInicio, dataFim, pageable);
	    }

	//usando JPQL
	@GetMapping("/pesquisa")
	public List<Lancamento> pesquisa(LancamentoFilter lancamentoFilter) {
		return lancamentoRepository.filtrarComJpql(lancamentoFilter);
	}
	
	@GetMapping("/listartudo")
	public List<Lancamento> buscar(HttpServletRequest request) {
		
		Map<String, String[]> parameters = request.getParameterMap();
		
		List<String> paramerosValidos = Arrays.asList("descricao", "dataVencimentoDe", "dataVencimentoAte");
		
		parameters.keySet().forEach(nomeParametro -> {
			if (!paramerosValidos.contains(nomeParametro)) {
				throw new InvalidRequestParameterNameException("Parametro " +  nomeParametro + " não é reconhecido pelo recurso.");
			}
		});
		String[] parametroDescricao = parameters.get("descricao");
		String[] parametroDataVencimentoDe = parameters.get("dataVencimentoDe");
		String[] parametroDataVencimentoAte = parameters.get("dataVencimentoAte");
		
		LancamentoFilter lancamentoFilter = new LancamentoFilter();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		if (parametroDescricao != null) {
			lancamentoFilter.setDescricao(parametroDescricao[0]);
		}
		
		if (parametroDescricao != null) {
			lancamentoFilter.setDataVencimentoDe(LocalDate.parse(parametroDataVencimentoDe[0], formatter));
		}
		
		if (parametroDescricao != null) {
			lancamentoFilter.setDataVencimentoAte(LocalDate.parse(parametroDataVencimentoAte[0], formatter));
		}
		
		return lancamentoRepository.filtrarComJpql(lancamentoFilter);
	}

	@GetMapping("{/codigo}")
	public ResponseEntity<?> buscarPeloCodigo(@PathVariable Long codigo) {
		var lancamentos = lancamentoRepository.findById(codigo);
		return !lancamentos.isEmpty() ? ResponseEntity.ok(lancamentos) : ResponseEntity.noContent().build();

	}

	@PostMapping
	private ResponseEntity<?> criar(@Validated @RequestBody Lancamento lancamento, HttpServletResponse response)
			throws PessoaInexistenteOuInativaException {
		var lancamentoSalva = lancamentoService.salvar(lancamento);
		publisher.publishEvent(new RecursoCriadoEvent(publisher, response, lancamentoSalva.getCodigo()));
		return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoSalva);
	}

	@DeleteMapping("/{codigo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void Remover(@PathVariable Long codigo) {
		lancamentoRepository.deleteById(codigo);
	}

	@GetMapping("/{codigo}")
	public ResponseEntity<?> buascarPeloCodigo(@PathVariable Long codigo) {
		var lancamento = lancamentoRepository.findById(codigo);
		return lancamento.isPresent() ? ResponseEntity.ok(lancamento)
				: ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@ExceptionHandler({ PessoaInexistenteOuInativaException.class })
	public ResponseEntity<Object> handlePessoaInexistenteOuInativaException(PessoaInexistenteOuInativaException ex) {
		String messagemUsuario = messageSource.getMessage("pessoa.inexistente-ou-inativa", null,
				LocaleContextHolder.getLocale());
		String mensagemDesenvolvedor = ExceptionUtils.getRootCauseMessage(ex);
		List<Erro> erros = Arrays.asList(new Erro(messagemUsuario, mensagemDesenvolvedor));
		return ResponseEntity.badRequest().body(erros);

	}
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
