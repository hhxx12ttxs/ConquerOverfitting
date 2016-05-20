/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waksiu.calcgwt.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;
import org.waksiu.calcgwt.ac.domain.AcMagz;
import org.waksiu.calcgwt.domain.Plany;
import org.waksiu.calcgwt.domain.PremInputVersion2;
import org.waksiu.calcgwt.domain.PremNieobecnosc;
import org.waksiu.calcgwt.domain.PremParametryM;
import org.waksiu.calcgwt.dtos.AcMagzCommand;
import org.waksiu.calcgwt.dtos.BasePremieDto;
import org.waksiu.calcgwt.dtos.BaseResult;
import org.waksiu.calcgwt.dtos.DniDelegacjaDto;
import org.waksiu.calcgwt.dtos.DniPozaDto;
import org.waksiu.calcgwt.dtos.DniRoboczeDto;
import org.waksiu.calcgwt.dtos.PremFormDto;
import org.waksiu.calcgwt.dtos.PremInputCommand;
import org.waksiu.calcgwt.dtos.PremieDto;
import org.waksiu.calcgwt.dtos.PunktyDto;
import org.waksiu.calcgwt.editors.PremFormDtoEditor;
import org.waksiu.calcgwt.editors.PremieDtoContener;
import org.waksiu.calcgwt.editors.PremieDtoListEditor;
import org.waksiu.calcgwt.editors.PunktyDtoContener;
import org.waksiu.calcgwt.editors.PunktyDtoListEditor;
import org.waksiu.calcgwt.security.MyUserDetails;
import org.waksiu.calcgwt.server.service.IPremieService;
import org.waksiu.calcgwt.server.service.IRatyService;
import org.waksiu.calcgwt.server.service.IUserService;
import org.waksiu.calcgwt.server.service.IWriteToFile;

/**
 *
 * @author waksiu
 */
@Controller
@RequestMapping("premie")
public class PremieController {

    @Autowired
    private IPremieService premieService;
    @Autowired
    private IRatyService ratyService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IWriteToFile writeToFile;
    @Autowired
    private Validator validator;

    public void setPremieService(IPremieService premieService) {
        this.premieService = premieService;
    }

    public void setRatyService(IRatyService ratyService) {
        this.ratyService = ratyService;
    }

    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
    private final static Log log = LogFactory.getLog(PremieController.class);

    @RequestMapping(value = "prem.do")
    public String prem(@Valid PremFormDto premFormDto, ModelMap model) {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<AcMagz> listOfMag = new ArrayList<AcMagz>();
        for (AcMagz acMagz : userDetails.getAcMagzList()) {
            if (userDetails.hasRight("ROLE_prem_BOTH_PANEL")) {
                listOfMag.add(acMagz);
            } else {
                if (acMagz.getPanelPremie() != null) {
                    if (acMagz.getPanelPremie().intValue() == 2) {
                        listOfMag.add(acMagz);
                    }
                }
            }
        }
        if (listOfMag.isEmpty()) {
            return "simple-prem-r";
        }
        premFormDto.setSymbolMagList(listOfMag);

        model.addAttribute("acMagzList", listOfMag);
        model.addAttribute("premFormDto", premFormDto);

        if (premFormDto.getSymbolMag() == null) {
            premFormDto.setSymbolMag("ALL");
        }


        Calendar c = Calendar.getInstance();
        c.setTime(premFormDto.getDataOd());
        String year = String.valueOf(c.get(Calendar.YEAR));
        String month = String.valueOf(c.get(Calendar.MONTH) + 1);
        if (month.length() == 1) {
            month = "0" + month;
        }
        model.addAttribute("rok", year);
        model.addAttribute("miesiac", month);

//        int maxResult = 40;
//        int firstResult = 1;
        int orderDir = 1;
        String orderBy = "symbolMag";

//        String exportType = request.getParameter((new ParamEncoder("tableId").encodeParameterName(TableTagParameters.PARAMETER_EXPORTTYPE)));
//        String pageNumber = request.getParameter((new ParamEncoder("tableId").encodeParameterName(TableTagParameters.PARAMETER_PAGE)));
//        String by = request.getParameter((new ParamEncoder("tableId").encodeParameterName(TableTagParameters.PARAMETER_SORT)));
//        String dir = request.getParameter((new ParamEncoder("tableId").encodeParameterName(TableTagParameters.PARAMETER_ORDER)));
//        if (pageNumber != null) {
//            firstResult = Integer.parseInt(pageNumber);
//        }
//        if (by != null) {
//            orderBy = by;
//        }
//        if (dir != null) {
//            orderDir = Integer.parseInt(dir);
//        }

        log.info("orderBy" + orderBy);
        log.info("orderDir" + orderDir);

        HashMap magIloscOsob = new HashMap();
        List<PremParametryM> parametryM = premieService.getParametryM();
        for (PremParametryM premParametryM : parametryM) {
            magIloscOsob.put(premParametryM.getSymbolMag(), premParametryM.getIloscPrac());
        }

        List<DniDelegacjaDto> dniDelegacja = premieService.getDniDelegacja(premFormDto);
        List<DniPozaDto> dniPoza = premieService.getDniPoza(premFormDto);

        List<DniRoboczeDto> dniRoboczeMiesiac = premieService.getDniRoboczeMiesiac(premFormDto);
        List<DniRoboczeDto> dniRoboczeDoTeraz = premieService.getDniRoboczeDoTeraz(premFormDto);
        BaseResult<PremieDto> baseResult = premieService.getPremieByForm(premFormDto, orderBy, orderDir);
        List<PunktyDto> punktyDto = premieService.getPunktyPremie(premFormDto);
        model.addAttribute("punktyDto", punktyDto);
        List<Plany> plany = premieService.getPlanByDate(premFormDto.getDataOd());

        HashMap<String, Double> ratyMagazyn = ratyService.getRaty(premFormDto);

        List<String> symbolAkwList = new ArrayList<String>();
        for (PremieDto pd : baseResult.getListResult()) {
            symbolAkwList.add(pd.getSymbolKtr());
        }
        HashMap nieobecnoscAll = new HashMap();
        List<PremNieobecnosc> nieobecnoscList = premieService.getNieobecnosci(symbolAkwList, year, month);
        for (PremNieobecnosc premNieobecnosc : nieobecnoscList) {
            nieobecnoscAll.put(premNieobecnosc.getPremNieobecnoscPK().getSymbolAkw(), 0);
        }
        for (PremNieobecnosc premNieobecnosc : nieobecnoscList) {
            nieobecnoscAll.put(premNieobecnosc.getPremNieobecnoscPK().getSymbolAkw(), ((Integer) nieobecnoscAll.get(premNieobecnosc.getPremNieobecnoscPK().getSymbolAkw())).intValue() + premNieobecnosc.getIloscDni().intValue());
        }
        HashMap magDniRoboczeMiesiac = new HashMap();
        for (DniRoboczeDto dniRoboczeDto : dniRoboczeMiesiac) {
            magDniRoboczeMiesiac.put(dniRoboczeDto.getSymbolMag(), dniRoboczeDto.getIlosc());
        }
        HashMap obrotMagazyn = new HashMap();

        // Podsumowanie ka?dego magazynu:
        List<BasePremieDto> basePremieList = new ArrayList();
        for (AcMagz acMagz : listOfMag) {
            BasePremieDto basePremieDto = new BasePremieDto();
            basePremieDto.setSymbolMag(acMagz.getSymbolMag());
            for (PremieDto premieDto : baseResult.getListResult()) {
                if (premieDto.getSymbolMag().equals(acMagz.getSymbolMag())) {
                    basePremieDto.getPremieDtoList().add(premieDto);
                }
            }
            if (!basePremieDto.getPremieDtoList().isEmpty()) {
                basePremieList.add(basePremieDto);
            }
        }
        List<PremieDto> premieDtoList = new LinkedList();
        for (BasePremieDto basePremieDto : basePremieList) {
            // Podsumowanie
            PremieDto premieDtoSuma = new PremieDto();
            premieDtoSuma.setSymbolMag("Suma:");
            premieDtoSuma.setSprzedaz(0d);
            premieDtoSuma.setCenaPrzySprzedazy(0d);
            premieDtoSuma.setIloscRat(0);
            premieDtoSuma.setWartoscRat(0d);
            premieDtoSuma.setIloscNka(0);
            premieDtoSuma.setIloscPg(0);
            premieDtoSuma.setWartoscPg(0d);
//            int iloscOsob = 0;
            String symbolMagTemp = "";
            for (PremieDto premieDto : basePremieDto.getPremieDtoList()) {

                //delegacje
                for (DniDelegacjaDto dniDelegacjaDto : dniDelegacja) {
                    if (dniDelegacjaDto.getSymbolMag().equals(premieDto.getSymbolMag()) && dniDelegacjaDto.getSymbolAkw().equals(premieDto.getSymbolKtr())) {
                        premieDto.setDniDelegacja(dniDelegacjaDto.getIloscDni());
                    }
                }
                for (DniPozaDto dniPozaDto : dniPoza) {
                    if (dniPozaDto.getSymbolAkw().equals(premieDto.getSymbolKtr())) {
                        if (premieDto.getSymbolMag().equals(premieDto.getSymbolMagMain())) {
                            premieDto.setDniPoza(dniPozaDto.getIloscDni());
                        }
                    }
                }
                // podsumowanie sprzedaz
                symbolMagTemp = premieDto.getSymbolMag();
                premieDtoSuma.setSprzedaz(premieDtoSuma.getSprzedaz().doubleValue() + premieDto.getSprzedaz().doubleValue());
//                iloscOsob++;

                if (premieDto.getCenaPrzySprzedazy() == null) {
                    premieDto.setCenaPrzySprzedazy(0d);
                }
                premieDtoSuma.setCenaPrzySprzedazy(premieDtoSuma.getCenaPrzySprzedazy().doubleValue() + premieDto.getCenaPrzySprzedazy().doubleValue());
                // podsumowanie rat
                premieDtoSuma.setIloscRat(premieDtoSuma.getIloscRat().intValue() + premieDto.getIloscRat().intValue());
                // podsumowanie rat wartosc
                if (premieDto.getWartoscRat() == null) {
                    premieDto.setWartoscRat(0d);
                }
                premieDtoSuma.setWartoscRat(premieDtoSuma.getWartoscRat().doubleValue() + premieDto.getWartoscRat().doubleValue());
                // suma Nka
                premieDtoSuma.setIloscNka(premieDtoSuma.getIloscNka().intValue() + premieDto.getIloscNka().intValue());
                // suma Pg
                premieDtoSuma.setIloscPg(premieDtoSuma.getIloscPg().intValue() + premieDto.getIloscPg().intValue());
                // podsumowanie pg wartosc
                if (premieDto.getWartoscPg() == null) {
                    premieDto.setWartoscPg(0d);
                }
                premieDtoSuma.setWartoscPg(premieDtoSuma.getWartoscPg().doubleValue() + premieDto.getWartoscPg().doubleValue());
            }
            obrotMagazyn.put(symbolMagTemp, premieDtoSuma.getSprzedaz());
            int iloscOsob = 0;
            if (magIloscOsob.get(symbolMagTemp) != null) {
                iloscOsob = ((Integer) magIloscOsob.get(symbolMagTemp)).intValue();
            }
            // podsumowanie rabat
            double sprzedazSrednia = premieDtoSuma.getCenaPrzySprzedazy().doubleValue() / iloscOsob;
            double cenaPrzySprzedazSrednia = premieDtoSuma.getSprzedaz().doubleValue() / iloscOsob;
            premieDtoSuma.setRabat((sprzedazSrednia - cenaPrzySprzedazSrednia) / sprzedazSrednia * 100);
            // podsumowanie nasycenieRat //              round((sum(r.wart_zak)/1.22)*100/w.sprzedaz,0) as nasycenieRat
            double wartoscRatSrednia = premieDtoSuma.getWartoscRat().doubleValue() / iloscOsob;
            premieDtoSuma.setNasycenieRat(wartoscRatSrednia / sprzedazSrednia * 100);
//                premieDtoSuma.setNasycenieRat(premieDtoSuma.getWartoscRat());

            // suma Nasycenie Pg
            double wartoscPgSrednia = premieDtoSuma.getWartoscPg().doubleValue() / iloscOsob;
            premieDtoSuma.setNasyceniePg(wartoscPgSrednia / sprzedazSrednia * 100);


            basePremieDto.getPremieDtoList().add(premieDtoSuma);
            // Plany
            PremieDto premieDtoPlany = new PremieDto();
            premieDtoPlany.setSymbolMag("Plany:");
            for (PremieDto premieDto : basePremieDto.getPremieDtoList()) {
                for (Plany p : plany) {
                    if (p.getSymbolMag().getSymbolMag().equals(premieDto.getSymbolMag())) {
                        // plany dla sprzedazy
                        if (p.getSprzedaz() != null) {
                            premieDtoPlany.setSprzedaz((double) p.getSprzedaz());
                        }
                        if (p.getRaty() != null) {
                            premieDtoPlany.setNasycenieRat((double) p.getRaty());
                        }
                        if (p.getIloscN() != null) {
                            premieDtoPlany.setIloscNka(p.getIloscN());
                        }
                        if (p.getNasyceniePg() != null) {
                            premieDtoPlany.setNasyceniePg(p.getNasyceniePg());
                        }
                    }
                }
            }
            basePremieDto.getPremieDtoList().add(premieDtoPlany);

            // Prognoza
            PremieDto premieDtoPrognoza = new PremieDto();
            premieDtoPrognoza.setSymbolMag("Prognoza:");
            for (PremieDto premieDto : basePremieDto.getPremieDtoList()) {
                int iloscDniMiesiac = 0;
                int iloscDniDoTeraz = 0;
                for (DniRoboczeDto dni : dniRoboczeMiesiac) {
                    if (dni.getSymbolMag().equals(premieDto.getSymbolMag())) {
                        iloscDniMiesiac = dni.getIlosc();
                        magDniRoboczeMiesiac.put(dni.getSymbolMag(), dni.getIlosc());
                    }
                }
                for (DniRoboczeDto dni : dniRoboczeDoTeraz) {
                    if (dni.getSymbolMag().equals(premieDto.getSymbolMag())) {
                        iloscDniDoTeraz = dni.getIlosc();

                        premieDtoPrognoza.setSprzedaz(premieDtoSuma.getSprzedaz().doubleValue() / iloscDniDoTeraz * iloscDniMiesiac);
                        premieDtoPrognoza.setRabat(premieDtoSuma.getRabat().doubleValue() / iloscDniDoTeraz * iloscDniMiesiac);
                        premieDtoPrognoza.setIloscRat((int) ((double) premieDtoSuma.getIloscRat() / (double) iloscDniDoTeraz * (double) iloscDniMiesiac));
                        premieDtoPrognoza.setNasycenieRat(premieDtoSuma.getNasycenieRat().doubleValue() / iloscDniDoTeraz * iloscDniMiesiac);
                        premieDtoPrognoza.setIloscNka((int) ((double) premieDtoSuma.getIloscNka() / (double) iloscDniDoTeraz * (double) iloscDniMiesiac));
                        premieDtoPrognoza.setIloscPg((int) ((double) premieDtoSuma.getIloscPg() / (double) iloscDniDoTeraz * (double) iloscDniMiesiac));
                        premieDtoPrognoza.setNasyceniePg(premieDtoSuma.getNasyceniePg().doubleValue() / iloscDniDoTeraz * iloscDniMiesiac);
                    }
                }
//                    if (premieDto.getSymbolMag().equals("M00021")) {
//                        premieDtoPrognoza.setSprzedaz((double) iloscDniDoTeraz);
//                    }
            }
            basePremieDto.getPremieDtoList().add(premieDtoPrognoza);

            premieDtoList.addAll(basePremieDto.getPremieDtoList());
        }
        model.addAttribute("premieDtoList", premieDtoList);

        // PREMIE INPUT:
        List<PremInputVersion2> premieInput = premieService.getPremieInput(premFormDto);

        HashMap bazasMag = new HashMap();
        for (PremInputVersion2 premInputVersion2 : premieInput) {
            bazasMag.put(premInputVersion2.getPremInputVersion2PK().getSymbolMag(), 0);
        }

        // obliczenie bazas:
        for (PremInputVersion2 premInputVersion2 : premieInput) {
            try {
                double baza = 0d;
                double dni_robocze = 0d;
                double nieobecnosc_ilosc = 0d;
                if (premInputVersion2.getBaza() != null) {
                    baza = (double) premInputVersion2.getBaza();
                }
                if (magDniRoboczeMiesiac.get(premInputVersion2.getPremInputVersion2PK().getSymbolMag()) != null) {
                    dni_robocze = ((Integer) magDniRoboczeMiesiac.get(premInputVersion2.getPremInputVersion2PK().getSymbolMag())).doubleValue();
                }
                if (nieobecnoscAll.get(premInputVersion2.getPremInputVersion2PK().getSymbolAkw()) != null) {
                    nieobecnosc_ilosc = ((Integer) nieobecnoscAll.get(premInputVersion2.getPremInputVersion2PK().getSymbolAkw())).doubleValue();
                }
                premInputVersion2.setBazas((int) Math.round(baza / dni_robocze * (dni_robocze - nieobecnosc_ilosc)));

                String sm = premInputVersion2.getPremInputVersion2PK().getSymbolMag();
                bazasMag.put(sm, ((Integer) bazasMag.get(sm)).intValue() + premInputVersion2.getBazas().intValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PremInputCommand premInputCommand = new PremInputCommand();
        premInputCommand.setInputList(premieInput);



        // PW
        for (PunktyDto p : punktyDto) {
            double iloscOsob = 0d;
            if (magIloscOsob.get(p.getSymbolMag()) != null) {
                iloscOsob = (Integer) magIloscOsob.get(p.getSymbolMag());
            }
            double obrot = 0;
            obrot = (Double) obrotMagazyn.get(p.getSymbolMag());
            p.setPw(wyliczPw(p.getSymbolMag(), plany, obrot) * iloscOsob);
            p.setSuma(p.getSuma().doubleValue() + p.getPw());

//            double dniRobocze = magDniRoboczeMiesiac.get(p.getSymbolMag();
//            double dniNieobecne = nieobecnoscAll.get
//
//            double pws = (p.getPw() /  * magDniRoboczeMiesiac.get(p.getSymbolMag())
//
////            return (baza / dniRobocze) * (dniRobocze - nieobecnosc - dniPoza);
//
//            wyliczPws(bazasMag, magDniRoboczeMiesiac, nieobecnoscAll, dniPoza);


            // Raty
            for (String symbolMag : ratyMagazyn.keySet()) {
                if (symbolMag.equals(p.getSymbolMag())) {
                    p.setRaty(ratyMagazyn.get(symbolMag));
                }
            }
            if (p.getRaty() == null) {
                p.setRaty(0d);
            }
            p.setSuma(p.getSuma().doubleValue() + p.getRaty());

        }

        // Punkty premiowe na osobe
        List<PunktyDto> punktyDtoPerOsoba = new ArrayList();
        for (PunktyDto p : punktyDto) {
            double iloscOsob = 0d;
            if (magIloscOsob.get(p.getSymbolMag()) != null) {
                iloscOsob = (Integer) magIloscOsob.get(p.getSymbolMag());
            }
            PunktyDto p1 = new PunktyDto();
            p1.setSymbolMag(p.getSymbolMag());
            p1.setDofin(p.getDofin().doubleValue() / iloscOsob);
            p1.setNka(p.getNka().doubleValue() / iloscOsob);
            p1.setPg(p.getPg().doubleValue() / iloscOsob);
            p1.setPremium(p.getPremium().doubleValue() / iloscOsob);
            p1.setPzi(p.getPzi().doubleValue() / iloscOsob);
            p1.setSuma(p.getSuma().doubleValue() / iloscOsob);
            p1.setPw(p.getPw() / iloscOsob);
            p1.setRaty(p.getRaty() / iloscOsob);
            punktyDtoPerOsoba.add(p1);
        }
        model.addAttribute("punktyDtoPerOsoba", punktyDtoPerOsoba);


        // Procent sumy Premii
        for (BasePremieDto basePremieDto : basePremieList) {
            for (PremieDto premieDto : basePremieDto.getPremieDtoList()) {
                for (PunktyDto punkty : punktyDtoPerOsoba) {
                    if (punkty.getSymbolMag().equals(premieDto.getSymbolMag())) {
                        premieDto.setProcentSumy(punkty.getSuma());
                        int iloscDniMiesiac = 0;
                        if (magDniRoboczeMiesiac.get(premieDto.getSymbolMag()) != null) {
                            iloscDniMiesiac = ((Integer) magDniRoboczeMiesiac.get(premieDto.getSymbolMag())).intValue();
                        }

                        if (iloscDniMiesiac != 0) {
                            if (premieDto.getDniDelegacja() != null) {
                                if (premieDto.getDniDelegacja().intValue() > 0) {
                                    int iloscDniDel = premieDto.getDniDelegacja().intValue();
                                    double procentDni = iloscDniDel * 100 / iloscDniMiesiac;
                                    premieDto.setProcentSumy(procentDni / 100 * punkty.getSuma());
                                }
                            }
                            if (premieDto.getDniPoza() != null) {
                                if (premieDto.getDniPoza().intValue() > 0) {
                                    int iloscDniPoza = premieDto.getDniPoza().intValue();
                                    double procentDni = iloscDniPoza * 100 / iloscDniMiesiac;
                                    premieDto.setProcentSumy(punkty.getSuma() - (procentDni / 100 * punkty.getSuma()));
                                }
                            }
                        }
                    }
                }
            }
        }

        // Prognoza premii na osobe
        List<PunktyDto> prognozaPerOsoba = new ArrayList();
        for (PunktyDto p : punktyDto) {
            int iloscDniMiesiac = 0;
            int iloscDniDoTeraz = 0;
            for (DniRoboczeDto dni : dniRoboczeMiesiac) {
                if (dni.getSymbolMag().equals(p.getSymbolMag())) {
                    iloscDniMiesiac = dni.getIlosc();
                }
            }
            for (DniRoboczeDto dni : dniRoboczeDoTeraz) {
                if (dni.getSymbolMag().equals(p.getSymbolMag())) {
                    iloscDniDoTeraz = dni.getIlosc();
                }
            }
            double iloscOsob = 0d;
            if (magIloscOsob.get(p.getSymbolMag()) != null) {
                iloscOsob = (Integer) magIloscOsob.get(p.getSymbolMag());
            }
            PunktyDto p2 = new PunktyDto();
            p2.setSymbolMag(p.getSymbolMag());
            p2.setDofin((p.getDofin().doubleValue() / iloscOsob / iloscDniDoTeraz) * iloscDniMiesiac);
            p2.setNka((p.getNka().doubleValue() / iloscOsob / iloscDniDoTeraz) * iloscDniMiesiac);
            p2.setPg((p.getPg().doubleValue() / iloscOsob / iloscDniDoTeraz) * iloscDniMiesiac);
            p2.setPremium((p.getPremium().doubleValue() / iloscOsob / iloscDniDoTeraz) * iloscDniMiesiac);
            p2.setPzi((p.getPzi().doubleValue() / iloscOsob / iloscDniDoTeraz) * iloscDniMiesiac);
            p2.setSuma((p.getSuma().doubleValue() / iloscOsob / iloscDniDoTeraz) * iloscDniMiesiac);
            p2.setPw((p.getPw().doubleValue() / iloscOsob / iloscDniDoTeraz) * iloscDniMiesiac);
            p2.setRaty((p.getRaty().doubleValue() / iloscOsob / iloscDniDoTeraz) * iloscDniMiesiac);
            prognozaPerOsoba.add(p2);
        }
        model.addAttribute("prognozaPerOsoba", prognozaPerOsoba);

        PunktyDtoContener punktyDtoContener = new PunktyDtoContener();
        punktyDtoContener.setList(punktyDto);
        premInputCommand.setPunktyDtoContener(punktyDtoContener);
        PunktyDtoContener punktyDtoPerOsobaContener = new PunktyDtoContener();
        punktyDtoPerOsobaContener.setList(punktyDtoPerOsoba);
        premInputCommand.setPunktyDtoPerOsobaContener(punktyDtoPerOsobaContener);
        premInputCommand.setPunktyDtoContener(punktyDtoContener);
        PunktyDtoContener prognozaPerOsobaContener = new PunktyDtoContener();
        prognozaPerOsobaContener.setList(prognozaPerOsoba);
        premInputCommand.setPrognozaPerOsobaContener(prognozaPerOsobaContener);
        PremieDtoContener premieDtoContener = new PremieDtoContener();
        premieDtoContener.setList(premieDtoList);
        premInputCommand.setPremieContener(premieDtoContener);
        premInputCommand.setPremFormDto(premFormDto);

        model.addAttribute("premInputCommand", premInputCommand);

//        model.addAttribute("pageSize", baseResult.getListResult().size() < maxResult ? maxResult : baseResult.getListResult().size());
//        model.addAttribute("size", baseResult.getTotalResult());
//        model.addAttribute("time", baseResult.getSearchTime());

        boolean edycja = false;
        boolean komentarze = false;

        Calendar dzis = Calendar.getInstance();
        Calendar cOd = Calendar.getInstance();
        cOd.setTime(premFormDto.getDataOd());
        Calendar miesiacPoprzedni = Calendar.getInstance();
        miesiacPoprzedni.add(Calendar.MONTH, -1);
        Calendar granica = Calendar.getInstance();
        granica.set(Calendar.DAY_OF_MONTH, 06);

        if (dzis.before(granica)) {
            log.info("jest przed 06");
            if (cOd.get(Calendar.MONTH) == miesiacPoprzedni.get(Calendar.MONTH)) {
                log.info("cOd month" + cOd.get(Calendar.MONTH));
                log.info("miesiacPoprzedni" + miesiacPoprzedni.get(Calendar.MONTH));
                log.info("jest ustawiony miesiac poprzedni");
                edycja = true;
            }
        } else {
            if (cOd.get(Calendar.MONTH) == dzis.get(Calendar.MONTH)) {
                log.info("jest za granica i miesiac obecny");
                edycja = true;
            }
            if (cOd.get(Calendar.MONTH) == miesiacPoprzedni.get(Calendar.MONTH)) {
                log.info("jest za granica i miesiac poprzedni - sprzedawcy mog? widziec komentarze");
                komentarze = true;
            }
        }

        if (userDetails.hasRight("ROLE_prem_uznaniowa_edit_v2") && userDetails.hasRight("ROLE_prem_komentarz_edit_v2")) {
            komentarze = true;
            log.info("mo?na edytowa? wi?c wida? komentarze ca?y czas");
        }

        model.addAttribute("edycja", edycja);
        model.addAttribute("komentarze", komentarze);
        premInputCommand.setEdycja(edycja);
        premInputCommand.setKomentarz(komentarze);

        if (premFormDto.isExport()) {
            File file = this.writeToFile.createXlsPremie(premieDtoList, premInputCommand, "/home/template/premie.xls");
            model.addAttribute("file", file);
            return "export-prem";
        } else {
            return "premie-prem";
        }
    }

    private double wyliczPw(String symbolMag, List<Plany> plany, double obrot) {
        double plan_sprzedaz = 0;
        double premia_calosc = 0;
        for (Plany plan : plany) {
            if (plan.getSymbolMag().getSymbolMag().equals(symbolMag)) {
                if (plan.getSprzedaz() != null) {
                    plan_sprzedaz = (double) plan.getSprzedaz();
                }
                if (plan.getPremiaCalosc() != null) {
                    premia_calosc = (double) plan.getPremiaCalosc();
                }
            }
        }
        double pw = obrot / plan_sprzedaz * premia_calosc;
        return pw;
    }

//    private double wyliczPws(HashMap bazas, HashMap magDniRoboczeMiesiac, HashMap nieobecnoscAll, List<DniPozaDto> dniPoza) {
//        return (baza / dniRobocze) * (dniRobocze - nieobecnosc - dniPoza);
//    }
//
//    private double wyliczPwsDelegacja(double baza, int dniRobocze, int nieobecnosc, int dniDelegacja) {
//        return (baza / dniRobocze) * dniDelegacja;
//    }
//
//    function wylicz_pzws($baza,$dni_robocze,$minus,$dni_del) {
//    $bazas=round(($baza/$dni_robocze)*($dni_robocze-$minus-$dni_del));
//    return $bazas;
//}
//function wylicz_pzws_delegacja($baza,$dni_robocze,$dni_del) {
//    $bazas=round(($baza/$dni_robocze)*$dni_del);
//    return $bazas;
//}
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
//        binder.registerCustomEditor(PunktyDto.class, new PunktyDtoEditor());
        binder.registerCustomEditor(PunktyDtoContener.class, new PunktyDtoListEditor());
        binder.registerCustomEditor(PremieDtoContener.class, new PremieDtoListEditor());
        binder.registerCustomEditor(PremFormDto.class, new PremFormDtoEditor());
    }

    @RequestMapping(value = "prem.do", method = RequestMethod.PUT)
    public String update(@ModelAttribute("premInputCommand") PremInputCommand premInputCommand, BindingResult result, SessionStatus status, ModelMap model) {
        for (int i = 0; i < premInputCommand.getInputList().size(); i++) {
            Set<ConstraintViolation<PremInputVersion2>> constraintViolations = validator.validate(premInputCommand.getInputList().get(i), Default.class);
            for (ConstraintViolation<PremInputVersion2> constraintViolation : constraintViolations) {
                String propertyPath = constraintViolation.getPropertyPath().toString();
                String message = constraintViolation.getMessage();
                result.rejectValue("inputList[" + i + "]." + propertyPath, "", message);
            }
        }
        log.info("ROZMIAR:" + premInputCommand.getPremieContener().getList().size());

        model.addAttribute("punktyDto", premInputCommand.getPunktyDtoContener().getList());
        model.addAttribute("punktyDtoPerOsoba", premInputCommand.getPunktyDtoPerOsobaContener().getList());
        model.addAttribute("prognozaPerOsoba", premInputCommand.getPrognozaPerOsobaContener().getList());
        model.addAttribute("premieDtoList", premInputCommand.getPremieContener().getList());

        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<AcMagz> listOfMag = new ArrayList<AcMagz>();
        for (AcMagz acMagz : userDetails.getAcMagzList()) {
            if (userDetails.hasRight("ROLE_prem_BOTH_PANEL")) {
                listOfMag.add(acMagz);
            } else {
                if (acMagz.getPanelPremie() != null) {
                    if (acMagz.getPanelPremie().intValue() == 2) {
                        listOfMag.add(acMagz);
                    }
                }
            }
        }

        PremFormDto premFormDto = premInputCommand.getPremFormDto();
        log.info(premFormDto.getDataOd());
        log.info(premFormDto.getDataDo());
        premFormDto.setSymbolMagList(listOfMag);
        model.addAttribute("premFormDto", premFormDto);
        model.addAttribute("acMagzList", listOfMag);

        Calendar c = Calendar.getInstance();
        c.setTime(premFormDto.getDataOd());
        String year = String.valueOf(c.get(Calendar.YEAR));
        String month = String.valueOf(c.get(Calendar.MONTH) + 1);
        if (month.length() == 1) {
            month = "0" + month;
        }
        model.addAttribute("rok", year);
        model.addAttribute("miesiac", month);

        model.addAttribute("edycja", premInputCommand.isEdycja());
        model.addAttribute("komentarze", premInputCommand.isKomentarz());

        List<PremInputVersion2> list = new ArrayList<PremInputVersion2>();
        for (PremInputVersion2 p : premInputCommand.getInputList()) {
            if (p != null) {
                if (p.getPremInputVersion2PK() != null) {
                    if (p.getPremInputVersion2PK().getSymbolAkw() != null) {
                        list.add(p);
                    }
                }
            }
        }
        if (result.hasErrors()) {
            return "premie-prem";
        } else {
            this.premieService.updatePremInputList(list);
            return "premie-prem";
        }
    }

    @RequestMapping(value = "panel.do")
    public String panel(ModelMap model) {
        List<AcMagz> magzList = (List<AcMagz>) userService.getAcMagzList();
        AcMagzCommand acMagzCommand = new AcMagzCommand();
        acMagzCommand.setAcMagzList(magzList);
        model.addAttribute("acMagzCommand", acMagzCommand);
        return "panel-show";
    }

    @RequestMapping(value = "panel.do", method = RequestMethod.PUT)
    public String update(AcMagzCommand acMagzCommand, BindingResult result, SessionStatus status, ModelMap model) {
        for (int i = 0; i < acMagzCommand.getAcMagzList().size(); i++) {
            Set<ConstraintViolation<AcMagz>> constraintViolations = validator.validate(acMagzCommand.getAcMagzList().get(i), Default.class);
            for (ConstraintViolation<AcMagz> constraintViolation : constraintViolations) {
                String propertyPath = constraintViolation.getPropertyPath().toString();
                String message = constraintViolation.getMessage();
                result.rejectValue("acMagzList[" + i + "]." + propertyPath, "", message);
            }
        }

        if (result.hasErrors()) {
            return "panel-show";
        } else {
            this.userService.updateAcMagzList(acMagzCommand.getAcMagzList());
            return "panel-show-r";
        }
    }
}


