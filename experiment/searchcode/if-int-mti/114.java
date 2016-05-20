package com.mti.shop.webserviceImpl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mti.shop.model.Account;
import com.mti.shop.model.Product;
import com.mti.shop.model.Type;
import com.mti.shop.service.AccountService;
import com.mti.shop.service.ProductService;
import com.mti.shop.webservice.AuthenticationService;

@WebService(endpointInterface = "com.mti.shop.webservice.AuthenticationService", serviceName = "shopService")
@Component(value = "authImpl")
public class AuthenticationServiceImpl implements AuthenticationService {
	@Autowired
	private AccountService accountService;

	@Autowired
	private ProductService productService;

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public Boolean getAuthentication(String login, String pass)
			throws AuthenticationException {
		try {
			Account account = accountService.getAccountByEmail(login);

			if (null != account
					&& account.getCptPassword().equals(encode(pass, "MD5"))) {
				List<GrantedAuthority> lAuth = new ArrayList<GrantedAuthority>();
				for (Type role : account.getRoles()) {
					lAuth.add(new GrantedAuthorityImpl(role.getTypeLabel()));
				}
				UsernamePasswordAuthenticationToken res = new UsernamePasswordAuthenticationToken(
						account.getCptEmail(), account.getCptPassword(), lAuth);
				res.setDetails(account);
				return true;
			} else {
				return false;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new BadCredentialsException("Password incorrect", e);
		}
	}

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
	public List<Product> getMostRecentProduct(Integer index, Integer offset) {
		return productService.getLastProducs(index, offset);
	}
}

