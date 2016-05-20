/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waksiu.MySpring.DAO;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.waksiu.MySpring.Analyzer.NazwaArtAnalyzer;
import org.waksiu.MySpring.domain.AcMagz;
import org.waksiu.MySpring.domain.BaseResult;
import org.waksiu.MySpring.domain.CenyArtykulyDTO;

/**
 *
 * @author waksiu
 */
@Repository
public class CenyDAO extends HibernateDaoSupport {

    @Autowired
    public void setMyHibernateTemplate(HibernateTemplate hibernateTemplate) {
        setHibernateTemplate(hibernateTemplate);
    }
    private final static Log log = LogFactory.getLog(CenyDAO.class);

    public void createIndex() {
        Session s = getHibernateTemplate().getSessionFactory().getCurrentSession();
        FullTextSession fullTextSession = Search.createFullTextSession(s);
        fullTextSession.purgeAll(CenyArtykulyDTO.class);
//            fullTextSession.flush();

        SQLQuery qClear = getHibernateTemplate().getSessionFactory().getCurrentSession().createSQLQuery("truncate eceny_CenyArtykulyDTO");
        qClear.executeUpdate();

        String SQLString = "select" +
                " if(ww.poziom1 is null,'',ww.poziom1) as poziom1," +
                " if(ww.poziom2 is null,'',ww.poziom2) as poziom2," +
                " if(ww.poziom3 is null,'',ww.poziom3) as poziom3," +
                " if(ww.kod_prod is null,'',ww.kod_prod) as kodProd," +
                " ww.symbol_art as symbolArt," +
                " ww.nazwa_art as nazwaArt," +
                " ww.cena_zakup as cenaZakup," +
                " ww.cena_ref_bon as cenaRefBon," +
                " if(ww.stan is null,0,ww.stan) as stan," +
                " ww.dzial as dzial," +
                " if(eh.cena_zakup is null,0,eh.cena_zakup) as cenaZakupHist," +
                " if(eh.cena_bon_ref is null,0,eh.cena_bon_ref) as cenaRefBonHist," +
                " if(eh.stan is null,0,eh.stan) as stanHist," +
                " if(ea.cena_auto is null,0,ea.cena_auto) as cenaAuto," +
                " if(er.cena_hand is null,0,er.cena_hand) as cenaReczna," +
                " ea.status as statusAuto," +
                " ww.cena2 as cena2," +
                " (select c.pozycja from ceneo_konDump c where c.symbol_art=ww.symbol_art and substr(c.row_dtime,1,10)=curdate() and c.shop_url='www.superelektro.pl' limit 1) as pozycjaCeneo" +
                " from (" +
                " select" +
                " (select text from PODZ_ART where node_key=(select parent from PODZ_ART where node_key=w.parent)) as poziom1," +
                " (select text from PODZ_ART where node_key=w.parent) as poziom2," +
                " w.poziom3," +
                " w.kod_prod," +
                " w.symbol_art," +
                " w.nazwa_art," +
                " w.cena2," +
                " w.cena_zakup," +
                " w.withBonus-if(w.refund is null,0,w.refund) as cena_ref_bon," +
                " w.stan," +
                " w.dzial" +
                " from (" +
                " select" +
                " :dzial as dzial," +
                " pa.TEXT as poziom3," +
                " pa.PARENT," +
                " a.KOD_PROD," +
                " a.symbol_art," +
                " a.NAZWA_ART," +
                " a.cena2," +
                " if(sum(e.real_cena*e.stan)/sum(e.stan) is null,a.CENA_ZAKUP,sum(e.real_cena*e.stan)/sum(e.stan)) as cena_zakup," +
                " sum(e.stan) as stan," +
                " (sum(e.ilosc_ref)/sum(e.kandydatow))*sum(if(e.stan>0,if(x.id_akc=5,0,e.kwota_ref),0)*e.ilosc_ref)/sum(e.ilosc_ref) as refund," +
                " if(e.stan<=0 OR e.stan is null,a.cena_zakup,sum((e.real_cena-(e.real_cena*(e.bonus/100)))*e.stan)/sum(e.stan)) as withBonus" +
                " from ARTYKULY a" +
                " left outer join eceny_podstawa e" +
                " on a.SYMBOL_ART=e.symbol_art and e.symbol_mag in (:symbole_mag) and e.stan>0" +
                " inner join ART_PODZ ap" +
                " on a.symbol_art=ap.SYMBOL_ART and ap.NODE_KEY like 'AR -%' and ap.NODE_KEY not like 'AR -NER%' and ap.NODE_KEY not like 'AR -ATU%' and ap.NODE_KEY not like 'AR -XUS%'" +
                " left outer join PODZ_ART pa" +
                " on ap.NODE_KEY=pa.NODE_KEY" +
                " left outer join" +
                " (select ra.id_akc, rpz.symbol_art, rpz.klucz_prz from refund_pzlist rpz" +
                " left outer join refund_suppliers rs" +
                " on rpz.id_sup=rs.id_sup" +
                " left outer join refund_akcja ra" +
                " on ra.id_akc=rs.id_akc" +
                " left outer join refund_akcja_types rat on ra.typ_akc=rat.id" +
                " where ra.id_akc=5" +
                " ) as x" +
                " on x.symbol_art=a.SYMBOL_ART and x.klucz_prz=e.klucz_prz" +
                " where a.STATUS=1" +
                " group by symbol_art" +
                " ) as w" +
                " ) as ww" +
                " left outer join eceny_history eh" +
                " on ww.symbol_art=eh.symbol_art and ww.dzial=eh.dzial and eh.data=(current_date() - INTERVAL 1 DAY)" +
                " left outer join eceny_auto ea" +
                " on ea.symbol_art=ww.symbol_art and ea.dzial=ww.dzial" +
                " left outer join eceny_hand er" +
                " on er.symbol_art=ww.symbol_art and er.dzial=ww.dzial" +
                " limit 500";

        List<String> symboleMagI = new ArrayList();
        List<String> symboleMagP = new ArrayList();
        List<String> symboleMagD = new ArrayList();
        Criteria c = getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(AcMagz.class);
        List<AcMagz> listAcMagz = c.list();
        for (AcMagz acMagz : listAcMagz) {
            if (acMagz.getDzial() != null) {
                if (acMagz.getDzial().equals("INTERNET") || acMagz.getDzial().equals("HURT")) {
                    symboleMagI.add(acMagz.getSymbolMag());
                }
                if (acMagz.getDzial().equals("INTERNET") || acMagz.getDzial().equals("HURT") || acMagz.getDzial().equals("POD")) {
                    symboleMagP.add(acMagz.getSymbolMag());
                }
                if (acMagz.getDzial().equals("DETAL") || acMagz.getDzial().equals("HURT")) {
                    symboleMagD.add(acMagz.getSymbolMag());
                }
            }
        }

        Query qI = getHibernateTemplate().getSessionFactory().getCurrentSession().createSQLQuery(SQLString).addScalar("poziom1").addScalar("poziom2").addScalar("poziom3").addScalar("kodProd").addScalar("symbolArt").addScalar("nazwaArt").addScalar("cenaZakup").addScalar("cenaRefBon").addScalar("stan").addScalar("dzial").addScalar("cenaZakupHist").addScalar("cenaRefBonHist").addScalar("stanHist").addScalar("cenaAuto").addScalar("cenaReczna").addScalar("statusAuto").addScalar("cena2").addScalar("pozycjaCeneo").setResultTransformer(Transformers.aliasToBean(CenyArtykulyDTO.class));
        qI.setParameterList("symbole_mag", symboleMagI);
        qI.setString("dzial", "INTERNET");
        for (Object o : qI.list()) {
            CenyArtykulyDTO cad = (CenyArtykulyDTO) o;
            getHibernateTemplate().getSessionFactory().getCurrentSession().save(cad);
            fullTextSession.index(cad);
        }
        Query qP = getHibernateTemplate().getSessionFactory().getCurrentSession().createSQLQuery(SQLString).addScalar("poziom1").addScalar("poziom2").addScalar("poziom3").addScalar("kodProd").addScalar("symbolArt").addScalar("nazwaArt").addScalar("cenaZakup").addScalar("cenaRefBon").addScalar("stan").addScalar("dzial").addScalar("cenaZakupHist").addScalar("cenaRefBonHist").addScalar("stanHist").addScalar("cenaAuto").addScalar("cenaReczna").addScalar("statusAuto").addScalar("cena2").addScalar("pozycjaCeneo").setResultTransformer(Transformers.aliasToBean(CenyArtykulyDTO.class));
        qP.setParameterList("symbole_mag", symboleMagP);
        qP.setString("dzial", "POD");
        for (Object o : qP.list()) {
            CenyArtykulyDTO cad = (CenyArtykulyDTO) o;
            getHibernateTemplate().getSessionFactory().getCurrentSession().save(cad);
            fullTextSession.index(cad);
        }
        Query qD = getHibernateTemplate().getSessionFactory().getCurrentSession().createSQLQuery(SQLString).addScalar("poziom1").addScalar("poziom2").addScalar("poziom3").addScalar("kodProd").addScalar("symbolArt").addScalar("nazwaArt").addScalar("cenaZakup").addScalar("cenaRefBon").addScalar("stan").addScalar("dzial").addScalar("cenaZakupHist").addScalar("cenaRefBonHist").addScalar("stanHist").addScalar("cenaAuto").addScalar("cenaReczna").addScalar("statusAuto").addScalar("cena2").addScalar("pozycjaCeneo").setResultTransformer(Transformers.aliasToBean(CenyArtykulyDTO.class));
        qD.setParameterList("symbole_mag", symboleMagD);
        qD.setString("dzial", "DETAL");
        for (Object o : qD.list()) {
            CenyArtykulyDTO cad = (CenyArtykulyDTO) o;
            getHibernateTemplate().getSessionFactory().getCurrentSession().save(cad);
            fullTextSession.index(cad);
        }
    }

    @SuppressWarnings("unchecked")
    public BaseResult<CenyArtykulyDTO> getCeny(String fraza, int firstResult, int maxResult, String orderBy, int orderDir, String exportType) {
        BaseResult<CenyArtykulyDTO> baseResult = new BaseResult<CenyArtykulyDTO>();
        Session s = getHibernateTemplate().getSessionFactory().getCurrentSession();

        if (!fraza.equals("")) {
            FullTextSession fSession = Search.createFullTextSession(s);
            List list = new ArrayList();

//            int sortBy = SortField.STRING;
//            if (orderBy.equals("cenaZakup") || orderBy.equals("qcenE") || orderBy.equals("qcenP")) {
//                sortBy = SortField.DOUBLE;
//            }
//            Sort sort = null;
//            SortField sortField = new SortField(orderBy, sortBy, true);
//            if (orderDir == 2) {
//                sortField = new SortField(orderBy, sortBy, true);
//
//            } else if (orderDir == 1) {
//                sortField = new SortField(orderBy, sortBy, false);
//            }
//            sort = new Sort(sortField);

            QueryParser parserSymbolArt = new QueryParser("symbolArtAnalyzer",
                    new org.waksiu.MySpring.Analyzer.SymbolArtAnalyzer());
            parserSymbolArt.setAllowLeadingWildcard(true);
            QueryParser parserNazwaArt = new QueryParser("nazwaArtAnalyzer",
                    new NazwaArtAnalyzer());
            parserNazwaArt.setAllowLeadingWildcard(true);

            String keywords = "";
            StringTokenizer st = new StringTokenizer(fraza, " ");
            while (st.hasMoreTokens()) {
                keywords += "+" + st.nextToken() + " ";
            }

            BooleanQuery booleanQuery = new BooleanQuery();
            try {
                org.apache.lucene.search.Query query1 = parserSymbolArt.parse(keywords);
                org.apache.lucene.search.Query query2 = parserNazwaArt.parse(keywords);
                booleanQuery.add(query1, BooleanClause.Occur.SHOULD);
                booleanQuery.add(query2, BooleanClause.Occur.SHOULD);

                FullTextQuery ftquery = fSession.createFullTextQuery(booleanQuery, CenyArtykulyDTO.class);//.setSort(sort);
                ftquery.enableFullTextFilter("stanFilter").setParameter( "stan", new Double(1));
                ftquery.enableFullTextFilter("cenaRecznaFilter").setParameter( "cenaReczna", new Double(0));
                ftquery.enableFullTextFilter("statusAutoFilter").setParameter( "statusAuto", new Integer(2));
                log.info("INFO:" + booleanQuery.toString());
                if (exportType == null) {
                    ftquery.setFirstResult(firstResult);
                    ftquery.setMaxResults(maxResult);
                }
//                ftquery.setSort(sort);
                baseResult.setTotalResult(ftquery.getResultSize());
                long startTime = System.currentTimeMillis();
                list = ftquery.list();
                long endTime = System.currentTimeMillis();
                baseResult.setSearchTime(endTime - startTime);
            } catch (ParseException e) {
                throw new RuntimeException("Cannot search with query string", e);
            }
            baseResult.setListResult(list);
            return baseResult;
        } else {
            FullTextSession fSession = Search.createFullTextSession(s);
            List list = new ArrayList();
            BooleanQuery booleanQuery = new BooleanQuery();
            try {
                QueryParser parser = new QueryParser("dzial", new KeywordAnalyzer());
                org.apache.lucene.search.Query query = parser.parse("INTERNET");
                FullTextQuery ftquery = fSession.createFullTextQuery(query, CenyArtykulyDTO.class);//.setSort(sort);
                log.info("INFO:" + booleanQuery.toString());
                if (exportType == null) {
                    ftquery.setFirstResult(firstResult);
                    ftquery.setMaxResults(maxResult);
                }
//                ftquery.setSort(sort);
                baseResult.setTotalResult(ftquery.getResultSize());
                long startTime = System.currentTimeMillis();
                list = ftquery.list();
                long endTime = System.currentTimeMillis();
                baseResult.setSearchTime(endTime - startTime);
            } catch (ParseException e) {
                throw new RuntimeException("Cannot search with query string", e);
            }
            baseResult.setListResult(list);
            return baseResult;
        }
    }
}

