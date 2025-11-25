package com.market.authentication.model;

import java.sql.Date;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class ResetToken {
	


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String jwtToken;
	private LocalDateTime createdTime;
	
	  @OneToOne
	    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false, unique = true)
	    private Users user;

	public ResetToken() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ResetToken(Long id, String jwtToken, LocalDateTime createdTime, Users user) {
		super();
		this.id = id;
		this.jwtToken = jwtToken;
		this.createdTime = createdTime;
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJwtToken() {
		return jwtToken;
	}

	public void setJwtToken(String jwtToken) {
		this.jwtToken = jwtToken;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	
}
