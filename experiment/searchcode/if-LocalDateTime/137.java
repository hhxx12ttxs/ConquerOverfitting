package no.nki.stas.ws.client.utils;

import java.util.Date;

import no.nki.stas.dao.domain.GInnOrdre;
import no.nki.stas.ws.common.service.StasGInnService;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class Client {

	private static Logger logger = LoggerFactory.getLogger(Client.class);

	private static ClassPathXmlApplicationContext context;
	
	public static void main(String args[]) throws Exception {
		context = new ClassPathXmlApplicationContext(new String[] { "user-account-client.xml", "client-web-services.xml" });
		
		runStasGInnService(args);
	}
	
	private static void runStasGInnService(String args[]) {
		StasGInnService stasGInnService = (StasGInnService) context.getBean("stasGInnService");

		int ordrenr = 0;
		if (args.length > 0) {
			ordrenr = Integer.valueOf(args[0]);
		}
		int kun_kundenr = 0;
		String fornavn = "Stephane";
		String etternavn = "Eybert";
		String foedselsdato = "13061966";
		String kjoenn = "M";
		String epost = "mittiprovence@yahoo.se";
		String telefon = "1234567890";
		String mobil = "45524762";
		String adresse1 = "Min adress";
		String adresse2 = "Del 2 av adressen";
		String postnr = "0545";
		String poststed = "Oslo";
		String land = "NO";
		String betalingsmaate = "other";
		String kommentar = "";
		String ssn = "";
		LocalDateTime creationDateTime = new LocalDateTime();
		String opprettet = creationDateTime.toString("dd.MM.yyyy HH:mm:ss");
//		Date behandlet = creationDateTime.toDateTime().toDate();
		
		GInnOrdre gInnOrdre = new GInnOrdre();
		gInnOrdre.setOrdrenr(ordrenr);
		gInnOrdre.setOpprettet(opprettet);
		gInnOrdre.setKun_kundenr(kun_kundenr);
		gInnOrdre.setFornavn(fornavn);
		gInnOrdre.setEtternavn(etternavn);
		gInnOrdre.setFoedselsdato(foedselsdato);
		gInnOrdre.setKjoenn(kjoenn);
		gInnOrdre.setEpost(epost);
		gInnOrdre.setTelefon(telefon);
		gInnOrdre.setMobil(mobil);
		gInnOrdre.setAdresse1(adresse1);
		gInnOrdre.setAdresse2(adresse2);
		gInnOrdre.setPostnr(postnr);
		gInnOrdre.setPoststed(poststed);
		gInnOrdre.setLand(land);
		gInnOrdre.setBetalingsmaate(betalingsmaate);
		gInnOrdre.setKommentar(kommentar);
		gInnOrdre.setSsn(ssn);
//		gInnOrdre.setBehandlet(behandlet);
		
		stasGInnService.gInnOrdreInsert(gInnOrdre);
	}

}

