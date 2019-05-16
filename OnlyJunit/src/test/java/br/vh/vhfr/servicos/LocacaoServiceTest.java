package br.vh.vhfr.servicos;



import static br.vh.vhfr.matchers.MatchersProprios.caiEm;
import static br.vh.vhfr.matchers.MatchersProprios.caiNumaSegunda;
import static br.vh.vhfr.utils.DataUtils.isMesmaData;
import static br.vh.vhfr.utils.DataUtils.obterDataComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import br.vh.vhfr.entidades.Filme;
import br.vh.vhfr.entidades.Locacao;
import br.vh.vhfr.entidades.Usuario;
import br.vh.vhfr.exceptions.FilmeSemEstoqueException;
import br.vh.vhfr.exceptions.LocadoraException;
import br.vh.vhfr.matchers.DiaSemanaMatcher;
import br.vh.vhfr.matchers.MatchersProprios;
import br.vh.vhfr.servicos.LocacaoService;
import br.vh.vhfr.utils.DataUtils;

public class LocacaoServiceTest {

	private LocacaoService service;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup(){
		service = new LocacaoService();
	}
	
	@Test
	public void deveAlugarFilme() throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
			
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
	}
	
	@Test(expected = FilmeSemEstoqueException.class)
	public void naoDeveAlugarFilmeSemEstoque() throws Exception{
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 0, 4.0));
		
		//acao
		service.alugarFilme(usuario, filmes);
	}
	
	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException{
		//cenario
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));
		
		//acao
		try {
			service.alugarFilme(null, filmes);
			Assert.fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuario vazio"));
		}
	}

	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException{
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		
		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio");
		
		//acao
		service.alugarFilme(usuario, null);
	}
	
	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException{
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		//cenario
		Usuario usuario = new Usuario("Usuario 1");
		List<Filme> filmes = Arrays.asList(new Filme("Filme 1", 1, 5.0));
		
		//acao
		Locacao retorno = service.alugarFilme(usuario, filmes);
		
		//verificacao
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
		
	}
}
