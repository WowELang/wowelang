package org.example.wowelang_backend.auth.dto;

import java.security.Principal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserPrincipalDTO implements Principal {

	private Long id;

	private String loginId;

	List<String> roles;

	@Override
	public String getName() {
		return loginId;
	}
}
