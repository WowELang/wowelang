package org.example.wowelang_backend.security.jwt;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.example.wowelang_backend.common.BaseEntity;
import org.example.wowelang_backend.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class RefreshToken extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "refresh_token_id")
	private Long id;

	@Column(nullable = false, unique = true, length = 64, name = "refresh_token")
	private String refreshToken;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, name = "expire_at")
	private LocalDateTime expireAt;

	public static RefreshToken of(User user, String token, long ttl) {
		return new RefreshToken(
			null,
			token,
			user,
			LocalDateTime.now().plusSeconds(ttl)
		);
	}

	public boolean isExpired() {
		return expireAt.isBefore(LocalDateTime.now());
	}
}
