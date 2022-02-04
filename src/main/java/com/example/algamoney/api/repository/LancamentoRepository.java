package com.example.algamoney.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.repository.lancamento.LancamentoRepositoryQuery;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>, LancamentoRepositoryQuery {
    
    @Query(value="SELECT * FROM algamoneyapi.lancamento WHERE descricao LIKE :descricao AND "
            + "data_vencimento between :data_inicio and :data_fim ORDER BY ?#{#pageable}",
           countQuery = "SELECT count(*) FROM algamoneyapi.lancamento WHERE descricao LIKE :descricao AND "
            + "data_vencimento between :data_inicio and :data_fim ORDER BY ?#{#pageable}"
    , nativeQuery = true)
    public Page<Lancamento> customQueryPageableDate(@Param("descricao") String descricao, 
            @Param("data_inicio") String dataInicio,@Param("data_fim") String dataFim,
            Pageable pageable);
    
}
