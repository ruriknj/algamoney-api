package com.example.algamoney.api.repository.lancamento;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;

import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Lancamento_;
import com.example.algamoney.api.repository.filter.LancamentoFilter;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {

	@PersistenceContext
	private EntityManager manager;

	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);

		// criar as restrições
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);

		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		
		adicionarRestricoesDePaginacao(query, pageable);

		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));

	}

	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {

		List<Predicate> predicates = new ArrayList<>();

//		where descrição like '%fdfdffd%'

		if (!ObjectUtils.isEmpty(lancamentoFilter.getDescricao())) {
			predicates.add(builder.like(builder.lower(root.get(Lancamento_.descricao)),
					"%" + lancamentoFilter.getDescricao().toLowerCase() + "%"));
		}

		if (lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento),
					lancamentoFilter.getDataVencimentoAte()));
		}

		if (lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento),
					lancamentoFilter.getDataVencimentoDe()));
		}

		return predicates.toArray(new Predicate[predicates.size()]);
	}

	@Override
	public List<Lancamento> filtrarComJpql(LancamentoFilter lancamentoFilter) {
		StringBuilder queryJpql = new StringBuilder("from Lancamento ");
		String condicao = "where";

		if (!ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoDe())
				&& !ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
			queryJpql.append(condicao).append(" dataVencimento between :dataVencimentoDe and :dataVencimentoAte");
			condicao = " and";
		}

		if (!ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoDe())
				&& ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
			queryJpql.append(condicao).append(" dataVencimento >= :dataVencimentoDe");
			condicao = " and";
		}

		if (ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoDe())
				&& !ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
			queryJpql.append(condicao).append(" dataVencimento <= :dataVencimentoAte");
			condicao = " and";
		}

		if (!ObjectUtils.isEmpty(lancamentoFilter.getDescricao())) {
			queryJpql.append(condicao).append(" descricao LIKE :descricao");
		}

		var createQuery = manager.createQuery(queryJpql.toString(), Lancamento.class);

		if (!ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoDe())
				&& !ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
			createQuery.setParameter("dataVencimentoDe", lancamentoFilter.getDataVencimentoDe());
			createQuery.setParameter("dataVencimentoAte", lancamentoFilter.getDataVencimentoAte());
		}

		if (!ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoDe())
				&& ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
			createQuery.setParameter("dataVencimentoDe", lancamentoFilter.getDataVencimentoDe());
		}

		if (ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoDe())
				&& !ObjectUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
			createQuery.setParameter("dataVencimentoAte", lancamentoFilter.getDataVencimentoAte());
		}

		if (!ObjectUtils.isEmpty(lancamentoFilter.getDescricao())) {
			createQuery.setParameter("descricao", "%" + lancamentoFilter.getDescricao() + "%");
		}

		return createQuery.getResultList();
	}

	private void adicionarRestricoesDePaginacao(TypedQuery<Lancamento> query, Pageable pageable) {

		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = totalRegistrosPorPagina * paginaAtual;
		
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
	}

	private Long total(LancamentoFilter lancamentoFilter) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);

		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		criteria.select(builder.count(root));
		return manager.createQuery(criteria).getSingleResult();
	}

//	
//	 @Query(value="SELECT * FROM algamoneyapi.lancamento WHERE descricao LIKE :descricao AND "
//	            + "data_vencimento between :data_inicio and :data_fim ORDER BY ?#{#pageable}",
//	           countQuery = "SELECT count(*) FROM algamoneyapi.lancamento WHERE descricao LIKE :descricao AND "
//	            + "data_vencimento between :data_inicio and :data_fim ORDER BY ?#{#pageable}"
//	    , nativeQuery = true)
//	@Override
//    public Page<Lancamento> customQueryPageableDate(@Param("descricao") String descricao, 
//            @Param("data_inicio") String dataInicio,@Param("data_fim") String dataFim,
//            Pageable pageable) {
//		 
//		 	return null;
//	}
}
