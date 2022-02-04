package com.example.algamoney.api.repository.filter;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LancamentoFilter {

	private String descricao;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataVencimentoDe;
	
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate dataVencimentoAte;
}
