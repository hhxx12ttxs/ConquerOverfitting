package com.mti.shop.webserviceImpl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mti.shop.model.Account;
import com.mti.shop.model.Categorie;
import com.mti.shop.model.Country;
import com.mti.shop.model.Evaluation;
import com.mti.shop.model.Product;
import com.mti.shop.model.Shop;
import com.mti.shop.service.AccountService;
import com.mti.shop.service.CategorieService;
import com.mti.shop.service.CountryService;
import com.mti.shop.service.EvaluationService;
import com.mti.shop.service.ProductService;
import com.mti.shop.service.ShopService;
import com.mti.shop.tool.GlobalSessionElement;
import com.mti.shop.tool.SessionTool;
import com.mti.shop.webservice.MyWebService;

@WebService(endpointInterface = "com.mti.shop.webservice.MyWebService", serviceName = "shopService")
@Service(value = "webservImpl")

public class MyWebServiceImpl implements MyWebService {
	@Autowired
	private AccountService accountService;

	@Autowired
	private ProductService productService;

	@Autowired
	private CategorieService categorieService;

	@Autowired
	private CountryService countryService;

	@Autowired
	private ShopService shopService;
	
	@Autowired
	private EvaluationService evaluationService;

	@Autowired
	private GlobalSessionElement sessElt;

	

	private static String encode(String password, String algorithm)
			throws NoSuchAlgorithmException {
		byte[] hash = null;
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm);
			hash = md.digest(password.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hash.length; ++i) {
			String hex = Integer.toHexString(hash[i]);
			if (hex.length() == 1) {
				sb.append(0);
				sb.append(hex.charAt(hex.length() - 1));
			} else {
				sb.append(hex.substring(hex.length() - 2));
			}
		}
		return sb.toString();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Product> getMostRecentProduct(Integer index,
			Integer offset) {
		return productService.getLastProducs(index, offset);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Account getCurrentAccount() {
		return SessionTool.getAccount();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Boolean createShop(String name, String desc, String email,
			Long countryId, Long catId, String accLog) {
		try {
			Account acc = accountService.getAccountByEmail(accLog);
			Shop shop = new Shop();
			shop.setCategorie(categorieService.findById(catId));
			shop.setCountry(countryService.findById(countryId));
			shop.setShopName(name);
			shop.setShopEmail(email);
			shop.setShopDesc(desc);
			shopService.addShop(shop, acc);
			return Boolean.TRUE;
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Boolean addProduct(String name, String desc, Double price,
			Date expire, Integer stock, Long cat, Long shopId) {
		try {
			Shop shop = shopService.findById(shopId);
			Categorie categorie = categorieService.findById(cat);
			if (null == categorie || null == shop) {
				return false;
			}
			Product product = new Product();
			product.setCategorie(categorie);
			product.setProdDatePublish(new Date());
			product.setProdDesc(name);
			product.setProdExpire(expire);
			product.setProdName(name);
			product.setProdPrice(price);
			product.setShop(shop);
			product.setProdStock(stock);
			productService.saveOrUpdate(product);

			return Boolean.TRUE;
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Product> findLastProductForShop(Integer index, Integer offset,
			String shopName) {
		return productService.findLastProductForShop(index, offset, shopName);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Product> findExpireSoonProducts(Integer index, Integer offset) {
		return productService.findExpireSoonProducts(index, offset);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Product> findProductByCategorie(Integer index, Integer offset,
			String categorie) {
		return productService.findProductByCategorie(index, offset, categorie);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Product> findProductByCountry(Integer index, Integer offset,
			String country) {
		return productService.findProductByCountry(index, offset, country);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Product> findProductByKey(Integer index, Integer offset,
			String key) {
		return productService.findProductByKey(index, offset, key);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Shop> findBestEvalShop(Integer index, Integer offset) {
		List evals = evaluationService.findBestEvaluatedShop(index, offset);
		List<Shop> res = new ArrayList<Shop>();
		
		for (Object evaluation : evals) {
			Shop shopToAdd = (Shop) ((Object[]) evaluation)[0];
			res.add(shopToAdd);
		}
		
		return res;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Categorie> getAllCategories() {
		return categorieService.findAll();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Country> getAllCountries() {
		return countryService.findAll();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Boolean CreateAccount(String name, String firstname, String phone,
			String addLine1, String addLine2, String email, String pwd,
			String postCode, String cntLabel) {

		try {
			Country cnt = countryService.findByLabel(cntLabel);
			
			Account acc = new Account();
			acc.setCountry(cnt);
			acc.setCptAddressLine1(addLine1);
			acc.setCptAddressLine2(addLine2);
			acc.setCptEmail(email);
			acc.setCptName(name);
			acc.setCptFirstname(firstname);
			acc.setCptPhoneNum(phone);
			acc.setCptPassword(encode(pwd, "MD5"));
			acc.setCptPostalCode(postCode);
			
			accountService.saveOrUpdateAccount(acc);
			return Boolean.TRUE;
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Boolean evaluateAccount(String evaluator, String evaluated,
			Integer note, String comment) {
		try {
			Account evaluatedAcc = accountService.getAccountByEmail(evaluated);
			Account evaluatorAcc = accountService.getAccountByEmail(evaluator);
			Evaluation eval = new Evaluation();
			eval.setEvalNote(note);
			eval.setEvalComment(comment);
			eval.setEvaluated(evaluatedAcc);
			eval.setEvaluator(evaluatorAcc);
			
			evaluationService.saveOrUpdate(eval);
			return Boolean.TRUE;
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Boolean evaluateShop(String evaluator, Long evaluated,
			Integer note, String comment) {
		try {
			Shop evaluatedShop = shopService.findById(evaluated);
			Account evaluatorAcc = accountService.getAccountByEmail(evaluator);
			Evaluation eval = new Evaluation();
			eval.setEvalNote(note);
			eval.setEvalComment(comment);
			eval.setShopEvaluated(evaluatedShop);
			eval.setEvaluator(evaluatorAcc);
			
			evaluationService.saveOrUpdate(eval);
			return Boolean.TRUE;
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Evaluation> findEvaluationForAccount(Integer index,
			Integer offset, String login) {
		return evaluationService.findEvaluationForAccount(index, offset, login);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Evaluation> findEvaluationForShop(Integer index,
			Integer offset, Long shopId) {
		return evaluationService.findEvaluationForShop(index, offset, shopId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Evaluation> findEvaluated(Integer index, Integer offset,
			String login) {
		return evaluationService.findEvaluated(index, offset, login);
	}
}

