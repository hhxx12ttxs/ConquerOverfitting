package com.mti.shop.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mti.shop.dao.ProductDao;
import com.mti.shop.model.Account;
import com.mti.shop.model.Type;
import com.mti.shop.service.AccountService;

@Component("myAuthProvider")
public class MyAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private AccountService accountService;

	@Autowired
	private ProductDao productDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		try {
			Account account = accountService
					.getAccountByEmail((String) authentication.getPrincipal());
			if (null != account
					&& account.getCptPassword().equals(
							encode(authentication.getCredentials().toString(),
									"MD5"))) {
				List<GrantedAuthority> lAuth = new ArrayList<GrantedAuthority>();
				for (Type role : account.getRoles()) {
					lAuth.add(new GrantedAuthorityImpl(role.getTypeLabel()));
				}
				UsernamePasswordAuthenticationToken res = new UsernamePasswordAuthenticationToken(
						account.getCptEmail(), account.getCptPassword(), lAuth);
				res.setDetails(account);
				SecurityContextHolder.getContext().setAuthentication(res);
				return res;
			} else {
				throw new BadCredentialsException("Password incorrect");
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
	public boolean supports(Class<? extends Object> arg0) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(arg0);
	}

}

